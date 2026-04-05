package com.minecraft.savagemc.arcade.bedwars.arcade.list.normal;

import com.minecraft.savagemc.arcade.bedwars.arcade.Arcade;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.event.Event;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.event.phase.EventPhase;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.type.Type;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.manager.list.TagManager;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.DateUtil;
import com.minecraft.savagemc.core.util.list.TimeUtil;

public class Trio extends Arcade {

    public Trio(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.BEDWARS_TRIO);
    }

    @Override
    public void handleSidebar(User user) {
        Arena arena = user.getArena();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle((arena.isType(Type.CASUAL) ? "§6" : "§5") + "§lBED WARS");

        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + arena.getIdentifier());
        sidebar.blankRow();

        // Renderizar scoreboard quando o jogo iniciou
        if (arena.isPhase(RoomPhase.PLAYING)) {
            Event event = arena.getEvent();

            if (event != null && event.isNotPhase(EventPhase.SUDDEN_DEATH)) {
                sidebar.addRow("next_event", "§ePróximo evento:");
                sidebar.addRow("event", " " + event.getPhase().getName() + " em §a" + TimeUtil.time(event.getTime()));

                sidebar.blankRow();
            }

            arena.renderSidebarTeams(user);

        } else {
            // Renderizar scoreboard quando o jogo ainda não iniciou

            sidebar.addRow("map", "Mapa: §a" + arena.getMap().getName());
            sidebar.addRow("mode", "Modo: §a" + arena.getArcade().getCategory().getName());

            sidebar.blankRow();
            arena.renderSidebarTime(sidebar);

            sidebar.addRow("players", "Jogadores: §a" + arena.getTotalPlayers() + "/" + arena.getMaxPlayers());
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        
        // Atualizar tag com o time do jogador no BedWars
        com.minecraft.savagemc.arcade.bedwars.structure.team.Team team = user.getTeam();
        String teamSuffix = "";
        
        if (team != null) {
            // Pegar a primeira letra do nome do time
            String teamInitial = team.getName().substring(0, 1).toUpperCase();
            // Formato: [A] para Azul, [V] para Vermelho, etc
            teamSuffix = " " + team.getColor() + "[" + teamInitial + "]";
            
            // Se tiver clan, adicionar também
            if (user.getAccount().hasClan() && user.getAccount().getToggle().isAllowClanTag()) {
                teamSuffix += " " + user.getAccount().getClanTag();
            }
        }
        
        TagManager.updateTagWithSuffix(user.getAccount(), user.getAccount().getTag(), teamSuffix);
    }

    @Override
    public void updateSidebar(User user) {
        // Método vazio - não atualiza mais as estatísticas na sidebar
    }
}
