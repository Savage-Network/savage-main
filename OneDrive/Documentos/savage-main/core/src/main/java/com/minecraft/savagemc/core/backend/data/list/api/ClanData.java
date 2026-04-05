package com.minecraft.savagemc.core.backend.data.list.api;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.clan.request.ClanRequest;
import com.minecraft.savagemc.core.api.clan.Clan;
import com.minecraft.savagemc.core.backend.database.postgres.PostgresDatabase;
import com.minecraft.savagemc.core.backend.database.redis.RedisDatabase;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class ClanData {

    private final PostgresDatabase postgres;
    private final RedisDatabase redis;

    private static final String CLAN_KEY_FORMAT = "clan:";
    private static final String CLAN_REQUEST_KEY_FORMAT = "clan-request:";

    public ClanData(PostgresDatabase postgres, RedisDatabase redis) {
        this.postgres = postgres;
        this.redis = redis;
    }

    public void save(Clan clan) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT id FROM clans WHERE id = ?::uuid");
            check.setObject(1, clan.getId(), java.sql.Types.OTHER);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO clans (id, name, tag, data) VALUES (?, ?, ?, ?::jsonb)"
                );
                insert.setObject(1, clan.getId(), java.sql.Types.OTHER);
                insert.setString(2, clan.getName());
                insert.setString(3, clan.getTag());
                insert.setString(4, Savage.GSON.toJson(clan));
                insert.executeUpdate();

                redis.save(CLAN_KEY_FORMAT + clan.getId(), clan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Clan of(UUID id, boolean storeInRedis) {
        Clan clan = redis.load(CLAN_KEY_FORMAT + id, Clan.class);

        if (clan == null) {
            try (Connection conn = postgres.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT data FROM clans WHERE id = ?::uuid");
                stmt.setObject(1, id, java.sql.Types.OTHER);
                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                    clan = Savage.GSON.fromJson(rs.getString("data"), Clan.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (clan != null && storeInRedis)
                redis.save(CLAN_KEY_FORMAT + id, clan);
        }

        return clan;
    }

    public Clan of(UUID id) {
        return of(id, false);
    }

    public Clan getByName(String name) {
        Clan clan = listInRedis().stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);

        if (clan == null) {
            try (Connection conn = postgres.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT data FROM clans WHERE LOWER(name) = LOWER(?)");
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                    clan = Savage.GSON.fromJson(rs.getString("data"), Clan.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return clan;
    }

    public Clan getByTag(String tag) {
        Clan clan = listInRedis().stream().filter(c -> c.getTag().equalsIgnoreCase(tag)).findFirst().orElse(null);

        if (clan == null) {
            try (Connection conn = postgres.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT data FROM clans WHERE LOWER(tag) = LOWER(?)");
                stmt.setString(1, tag);
                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                    clan = Savage.GSON.fromJson(rs.getString("data"), Clan.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return clan;
    }

    public void update(Clan clan, String field) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE clans SET data = ?::jsonb, name = ?, tag = ? WHERE id = ?::uuid"
            );
            stmt.setString(1, Savage.GSON.toJson(clan));
            stmt.setString(2, clan.getName());
            stmt.setString(3, clan.getTag());
            stmt.setObject(4, clan.getId(), java.sql.Types.OTHER);
            stmt.executeUpdate();
            redis.update(CLAN_KEY_FORMAT + clan.getId(), clan);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Clan clan) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM clans WHERE id = ?::uuid");
            stmt.setObject(1, clan.getId(), java.sql.Types.OTHER);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        redis.delete(CLAN_KEY_FORMAT + clan.getId());
    }

    public List<Clan> listInRedis() {
        return redis.loadAll(CLAN_KEY_FORMAT, Clan.class);
    }

    public List<Clan> list() {
        List<Clan> list = new ArrayList<>();
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT data FROM clans");
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(Savage.GSON.fromJson(rs.getString("data"), Clan.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /* Clan Request (Redis only) */
    public List<ClanRequest> getRequests() {
        return redis.loadAll(CLAN_REQUEST_KEY_FORMAT, ClanRequest.class);
    }

    public boolean hasRequest(Clan clan, UUID receiver) {
        return getRequests().stream().anyMatch(r -> r.getId().equals(clan.getId()) && r.getReceiver().equals(receiver));
    }

    public ClanRequest getRequest(UUID author, UUID receiver) {
        return getRequests().stream()
                .filter(r -> r.getAuthor().equals(author) && r.getReceiver().equals(receiver))
                .findFirst().orElse(null);
    }

    public void addRequest(ClanRequest request) {
        redis.save(CLAN_REQUEST_KEY_FORMAT + request.getIdentifier(), request, 300);
    }

    public void removeRequest(ClanRequest request) {
        redis.delete(CLAN_REQUEST_KEY_FORMAT + request.getIdentifier());
    }
}
