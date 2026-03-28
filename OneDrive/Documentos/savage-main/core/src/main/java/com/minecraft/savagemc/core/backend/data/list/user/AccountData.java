package com.minecraft.savagemc.core.backend.data.list.user;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.backend.data.template.UserTemplate;
import com.minecraft.savagemc.core.backend.database.postgres.PostgresDatabase;
import com.minecraft.savagemc.core.backend.database.redis.RedisDatabase;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountUpdateMessage;
import com.minecraft.savagemc.core.controller.list.AccountController;
import com.minecraft.savagemc.core.util.list.JsonUtil;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class AccountData implements UserTemplate<Account> {

    private final PostgresDatabase postgres;
    private final RedisDatabase redis;
    private final AccountController controller;

    private static final String ACCOUNT_KEY_FORMAT = "account:";

    public AccountData(PostgresDatabase postgres, RedisDatabase redis) {
        this.postgres = postgres;
        this.redis = redis;
        this.controller = Savage.getAccountController();
    }

    @Override
    public Account save(Account account) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT id FROM accounts WHERE id = ?::uuid");
            check.setString(1, account.getId().toString());
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                String json = Savage.GSON.toJson(account);
                PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO accounts (id, name, ip, data) VALUES (?::uuid, ?, ?, ?::jsonb)"
                );
                insert.setString(1, account.getId().toString());
                insert.setString(2, account.getName());
                insert.setString(3, account.getContext() != null ? account.getContext().getIpAddress() : null);
                insert.setString(4, json);
                insert.executeUpdate();

                redis.save(ACCOUNT_KEY_FORMAT + account.getId(), account);
                controller.save(account);
            } else {
                PreparedStatement sel = conn.prepareStatement("SELECT data FROM accounts WHERE id = ?::uuid");
                sel.setString(1, account.getId().toString());
                ResultSet data = sel.executeQuery();
                if (data.next())
                    account = Savage.GSON.fromJson(data.getString("data"), Account.class);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return account;
    }

    @Override
    public void delete(Account account) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM accounts WHERE id = ?::uuid");
            stmt.setString(1, account.getId().toString());
            stmt.executeUpdate();
            redis.delete(ACCOUNT_KEY_FORMAT + account.getId());
            controller.remove(account.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearCache() {
        controller.getCache().clear();
        redis.removeAll(ACCOUNT_KEY_FORMAT);
    }

    @Override
    public void update(Account account, String field) {
        try (Connection conn = postgres.getConnection()) {
            String json = Savage.GSON.toJson(account);
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE accounts SET data = ?::jsonb, name = ?, ip = ? WHERE id = ?::uuid"
            );
            stmt.setString(1, json);
            stmt.setString(2, account.getName());
            stmt.setString(3, account.getContext() != null ? account.getContext().getIpAddress() : null);
            stmt.setString(4, account.getId().toString());
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                redis.update(ACCOUNT_KEY_FORMAT + account.getId(), account);
                JsonObject tree = JsonUtil.jsonTree(account);
                new AccountUpdateMessage(account, field, tree.get(field)).send();
            } else {
                Savage.Savage.Logger().severe("[Account-Update] Conta de " + account.getName() + " não encontrada no banco.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Savage.Savage.Logger().severe("[Account-Update] Erro ao atualizar campo \"" + field + "\" de " + account.getName());
        }
    }

    @Override
    public void startExpiration(Account account) {
        controller.remove(account.getId());
        redis.cache(ACCOUNT_KEY_FORMAT + account.getId(), 80);
    }

    public void removeCache(Account account) {
        controller.remove(account.getId());
        redis.delete(ACCOUNT_KEY_FORMAT + account.getId());
    }

    @Override
    public void cancelExpiration(Account account) {
        redis.persist(ACCOUNT_KEY_FORMAT + account.getId());
    }

    @Override
    public Account of(UUID id, boolean storeInRedis) {
        Account account = controller.of(id);

        if (account == null) {
            account = redis.load(ACCOUNT_KEY_FORMAT + id, Account.class);

            if (account == null) {
                try (Connection conn = postgres.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("SELECT data FROM accounts WHERE id = ?::uuid");
                    stmt.setString(1, id.toString());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next())
                        account = Savage.GSON.fromJson(rs.getString("data"), Account.class);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (storeInRedis && account != null)
            redis.save(ACCOUNT_KEY_FORMAT + account.getId(), account);

        return account;
    }

    @Override
    public Account of(String name) {
        Account account = controller.of(name);

        if (account == null) {
            try (Connection conn = postgres.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT data FROM accounts WHERE LOWER(name) = LOWER(?)");
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                    account = Savage.GSON.fromJson(rs.getString("data"), Account.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return account;
    }

    public List<Account> getAlts(String ipAddress) {
        List<Account> alts = new ArrayList<>();
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT data FROM accounts WHERE ip = ?");
            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                alts.add(Savage.GSON.fromJson(rs.getString("data"), Account.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alts;
    }

    public boolean isExceededAccountLimit(String name, String ipAddress) {
        final int accountLimit = 3;
        int altSize = getAlts(ipAddress).size();

        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM accounts WHERE LOWER(name) = LOWER(?)");
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return !rs.next() && altSize >= accountLimit;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Account> list() {
        List<Account> list = new ArrayList<>();
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT data FROM accounts");
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(Savage.GSON.fromJson(rs.getString("data"), Account.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void bulkWrite(List<?> modelList) {
        // No-op: bulk updates handled via individual update() calls
    }

    @Override
    public Collection<Account> ranking(String field, int limit) {
        // Ranking via JSONB field ordering
        List<Account> list = new ArrayList<>();
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT data FROM accounts ORDER BY (data->?)::numeric DESC NULLS LAST LIMIT ?"
            );
            stmt.setString(1, field);
            stmt.setInt(2, limit * 2);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(Savage.GSON.fromJson(rs.getString("data"), Account.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
