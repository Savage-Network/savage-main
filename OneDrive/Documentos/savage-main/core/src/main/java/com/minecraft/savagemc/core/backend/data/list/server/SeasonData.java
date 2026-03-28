package com.minecraft.savagemc.core.backend.data.list.server;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.backend.database.postgres.PostgresDatabase;
import com.minecraft.savagemc.core.season.Season;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeasonData {

    private final PostgresDatabase postgres;

    public SeasonData(PostgresDatabase postgres) {
        this.postgres = postgres;
    }

    public Season getActualSeason() {
        return list().stream().filter(s -> !s.isOver()).findFirst().orElse(null);
    }

    public void save(Season season) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT id FROM seasons WHERE id = ?");
            check.setInt(1, season.getId());
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO seasons (id, data) VALUES (?, ?::jsonb)"
                );
                insert.setInt(1, season.getId());
                insert.setString(2, Savage.GSON.toJson(season));
                insert.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void update(Season season, String field) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE seasons SET data = ?::jsonb WHERE id = ?"
            );
            stmt.setString(1, Savage.GSON.toJson(season));
            stmt.setInt(2, season.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Season of(int id) {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT data FROM seasons WHERE id = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return Savage.GSON.fromJson(rs.getString("data"), Season.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int size() {
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM seasons");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Season> list() {
        List<Season> list = new ArrayList<>();
        try (Connection conn = postgres.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT data FROM seasons");
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(Savage.GSON.fromJson(rs.getString("data"), Season.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
