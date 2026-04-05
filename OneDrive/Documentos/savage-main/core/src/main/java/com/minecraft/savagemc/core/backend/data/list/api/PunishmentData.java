package com.minecraft.savagemc.core.backend.data.list.api;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.punishment.Punishment;
import com.minecraft.savagemc.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.savagemc.core.backend.database.postgres.PostgresDatabase;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class PunishmentData {

    private final PostgresDatabase postgres;

    public PunishmentData(PostgresDatabase postgres) {
        this.postgres = postgres;
    }

    public void save(Punishment punishment) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT id FROM punishments WHERE id = ?");
            check.setString(1, punishment.getId());
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO punishments (id, player, author, data) VALUES (?, ?, ?, ?::jsonb)"
                );
                insert.setString(1, punishment.getId());
                insert.setObject(2, punishment.getPlayer(), Types.OTHER);
                insert.setObject(3, punishment.getAuthor(), Types.OTHER);
                insert.setString(4, Savage.GSON.toJson(punishment));
                insert.executeUpdate();

                Account player = Savage.getAccountData().of(punishment.getPlayer());
                if (player != null)
                    player.addPunishmentToHistory(punishment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Punishment punishment) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM punishments WHERE id = ?");
            stmt.setString(1, punishment.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void update(Punishment punishment, String field) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE punishments SET data = ?::jsonb WHERE id = ?"
            );
            stmt.setString(1, Savage.GSON.toJson(punishment));
            stmt.setString(2, punishment.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean has(UUID player, PunishmentCategory category) {
        return of(player, category) != null;
    }

    public Punishment of(String ip, PunishmentCategory category) {
        Punishment punishment = listAssets(category).stream()
                .filter(p -> p.getIp() != null && p.getIp().equalsIgnoreCase(ip) && p.isValid())
                .findFirst().orElse(null);

        if (punishment != null && punishment.hasExpired()) {
            punishment.revoke(Constant.DEFAULT_ID, "Expirado");
            Account target = Savage.getAccountData().of(punishment.getPlayer(), false);
            if (target != null)
                target.updatePunishment(punishment);
            return null;
        }

        return punishment;
    }

    public Punishment of(UUID player, PunishmentCategory category) {
        return listAssets(category).stream()
                .filter(p -> p.getPlayer().equals(player) && p.isValid())
                .findFirst().orElse(null);
    }

    public List<Punishment> listAssets(PunishmentCategory category) {
        return list(category).stream()
                .filter(p -> p.isValid() && !p.getRevocation().isValid())
                .collect(Collectors.toList());
    }

    public List<Punishment> list() {
        List<Punishment> list = new ArrayList<>();
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT data FROM punishments");
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(Savage.GSON.fromJson(rs.getString("data"), Punishment.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Punishment> list(PunishmentCategory category) {
        return list().stream().filter(p -> p.getCategory().equals(category)).collect(Collectors.toList());
    }
}
