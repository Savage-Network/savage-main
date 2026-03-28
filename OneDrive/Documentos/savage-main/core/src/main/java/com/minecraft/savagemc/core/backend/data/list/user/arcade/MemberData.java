package com.minecraft.savagemc.core.backend.data.list.user.arcade;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.backend.database.postgres.PostgresDatabase;
import com.minecraft.savagemc.core.backend.database.redis.RedisDatabase;
import com.minecraft.savagemc.core.controller.list.MemberController;
import com.minecraft.savagemc.core.member.Member;
import com.google.gson.JsonObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class MemberData<T extends Member> {

    private final PostgresDatabase postgres;
    private final RedisDatabase redis;
    private final MemberController controller;

    private final Type type;
    private final Class<T> generic;
    private final String tableName;
    private final String MEMBER_KEY_FORMAT;

    protected static final List<String> MEMBER_TABLE_NAMES = Arrays.asList(
            "members_" + Constant.MONGODB_MEMBER_DUELS_COLLECTION,
            "members_" + Constant.MONGODB_MEMBER_BEDWARS_COLLECTION,
            "members_" + Constant.MONGODB_MEMBER_PVP_COLLECTION
    );

    @SuppressWarnings("unchecked")
    public MemberData(PostgresDatabase postgres, RedisDatabase redis, String collectionName) {
        this.postgres = postgres;
        this.redis = redis;
        this.controller = Savage.getMemberController();
        this.tableName = "members_" + collectionName;
        this.type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.generic = (Class<T>) type;
        this.MEMBER_KEY_FORMAT = "member:" + collectionName + ":";
    }

    public static List<Member> listAll(UUID id) {
        PostgresDatabase postgres = Savage.getPostgres();
        List<Member> members = new ArrayList<>();

        for (String table : MEMBER_TABLE_NAMES) {
            try (Connection conn = postgres.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT data FROM " + table + " WHERE id = ?");
                stmt.setObject(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                    members.add(Savage.GSON.fromJson(rs.getString("data"), Member.class));
                else
                    System.out.println("Não encontrou o usuário esperado da tabela " + table + "!");
            } catch (Exception e) {
                System.err.println("Erro ao acessar a tabela " + table + "!");
                e.printStackTrace();
            }
        }

        return members;
    }

    public T save(T member) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT id FROM " + tableName + " WHERE id = ?");
            check.setObject(1, member.getId());
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO " + tableName + " (id, data) VALUES (?, ?::jsonb)"
                );
                insert.setObject(1, member.getId());
                insert.setString(2, Savage.GSON.toJson(member));
                insert.executeUpdate();

                redis.save(MEMBER_KEY_FORMAT + member.getId(), member);
                controller.save(member);
            } else {
                PreparedStatement sel = conn.prepareStatement("SELECT data FROM " + tableName + " WHERE id = ?");
                sel.setObject(1, member.getId());
                ResultSet data = sel.executeQuery();
                if (data.next())
                    member = Savage.GSON.fromJson(data.getString("data"), type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return member;
    }

    public T of(UUID id, boolean storeInRedis) {
        Member member = controller.of(id, generic);

        if (member == null) {
            member = redis.load(MEMBER_KEY_FORMAT + id, generic);

            if (member == null) {
                try (Connection conn = postgres.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("SELECT data FROM " + tableName + " WHERE id = ?");
                    stmt.setObject(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next())
                        member = Savage.GSON.fromJson(rs.getString("data"), generic);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (storeInRedis && member != null)
            redis.save(MEMBER_KEY_FORMAT + member.getId(), member);

        return generic.cast(member);
    }

    public T of(UUID id) {
        return of(id, false);
    }

    public synchronized void update(T member, String field) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE " + tableName + " SET data = ?::jsonb WHERE id = ?"
            );
            stmt.setString(1, Savage.GSON.toJson(member));
            stmt.setObject(2, member.getId());
            stmt.executeUpdate();
            redis.update(MEMBER_KEY_FORMAT + member.getId(), member);
        } catch (Exception e) {
            e.printStackTrace();
            Savage.Savage.Logger().severe("[Member-Update] Erro ao atualizar campo \"" + field + "\" de " + member.getName());
        }
    }

    public void startExpiration(UUID id) {
        controller.remove(id, generic);
        redis.cache(MEMBER_KEY_FORMAT + id, 300);
    }

    public void cancelExpiration(UUID id) {
        redis.persist(MEMBER_KEY_FORMAT + id);
    }

    public List<T> list() {
        List<T> list = new ArrayList<>();
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT data FROM " + tableName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(Savage.GSON.fromJson(rs.getString("data"), generic));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void bulkWrite(List<?> modelList) {
        // No-op: handled via individual update() calls
    }

    public Collection<T> ranking(String field, int limit) {
        List<T> list = new ArrayList<>();
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT data FROM " + tableName + " ORDER BY (data->?)::numeric DESC NULLS LAST LIMIT ?"
            );
            stmt.setString(1, field);
            stmt.setInt(2, limit * 2);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(Savage.GSON.fromJson(rs.getString("data"), generic));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
