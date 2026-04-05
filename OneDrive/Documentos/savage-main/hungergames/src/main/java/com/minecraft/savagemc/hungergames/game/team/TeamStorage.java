package com.minecraft.savagemc.hungergames.game.team;

import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class TeamStorage implements VariableStorage {

    private final List<Team> teams;
    private final HungerGames hungerGames;
    @Variable(name = "team.player_limit")
    public int maxSlots = 8;

    public TeamStorage(HungerGames hungerGames, Team... teams) {
        this.hungerGames = hungerGames;
        this.teams = new ArrayList<>(Arrays.asList(teams));
        loadVariables();
    }

    public int teamCount() {
        return teams.size();
    }

    public Team getTeam(String name) {
        return teams.stream().filter(team -> team.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Team getTeam(int id) {
        return teams.stream().filter(team -> team.getId() == id).findFirst().orElse(null);
    }

    public void register(Team team) {

        if (teamCount() == 7)
            throw new IllegalStateException("Não é possível registrar mais de 7 times.");

        if (getTeam(team.getId()) != null)
            throw new IllegalStateException("Internal Exception: Já há um time com o index '" + team.getId() + "'");

        this.teams.add(team);
    }

    public void delete(Team team) {

        for (User user : team.getMembers())
            user.setTeam(null);

        team.getMembers().clear();
        this.teams.remove(team);

        for (int i = 0; i < teams.size(); i++) { // Readjusting teams ids.
            teams.get(i).setId(i);
        }
    }

    public Team next(int index) {

        Team next = getTeam(index + 1);

        if (next == null && index != 0)
            next = getTeam(0);

        return next;
    }

}
