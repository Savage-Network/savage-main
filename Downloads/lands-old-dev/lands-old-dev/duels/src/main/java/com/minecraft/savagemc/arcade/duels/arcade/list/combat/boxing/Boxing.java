package com.minecraft.savagemc.arcade.duels.arcade.list.combat.boxing;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import com.minecraft.savagemc.arcade.duels.arcade.Arcade;
import com.minecraft.savagemc.arcade.duels.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.savagemc.arcade.duels.user.factory.list.BoxingUser;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

@IgnoreEvent
public class Boxing extends Arcade {

    public Boxing(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_BOXING);

        setStyle(SidebarStyle.POINT);
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (arena.isValid(player)) {
            player.clearInventory();

            player.getInventory().setItem(0, Item.of(Material.DIAMOND_SWORD)
                    .enchantment(Enchantment.DAMAGE_ALL, 1));

            Savage.getPlatform().runSync(() -> player.addPotionEffects(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1)));
        } else
            handleDefaultHotbar(player);
    }

    @Override
    public void handleTeamStyle(Arena arena, Sidebar sidebar) {
        List<TeamPreset> teamList = arena.getTeamList();

        for (TeamPreset team : teamList) {
            if (team == null || team.getPlayers().isEmpty()) continue;

            if (arena.isSlot(Slot.SOLO)) {
                Player player = team.getPlayers().stream().findFirst().orElse(null);

                if (player == null) continue;

                BoxingUser user = (BoxingUser) BoxingUser.of(player.getUniqueId());

                sidebar.addRow(team.getCodeId(), team.getColor() + player.getName() + ": §7" + user.getHits());
            }
        }

        sidebar.blankRow();
    }

    @Override
    public void updateTeamStyle(Arena arena, Sidebar sidebar) {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBoxing(PlayerDamageTargetEvent event) {
        Player player = event.getPlayer(), target = event.getTarget();

        if (!(isValid(player) || isValid(target))) return;

        BoxingUser user = (BoxingUser) BoxingUser.of(player.getUniqueId());

        TeamPreset team = user.getTeam();

        user.setHits(user.getHits() + 1);

        Arena arena = user.getArena();

        // Atualizando hits na scoreboard
        arena.getMatchUsers().forEach(search -> search.getSidebar().updateRow(team.getCodeId(),
                team.getColor() + player.getName() + ": §7" + user.getHits()));

        // Venceu a partida
        if (user.itWon()) {
            arena.setWinner(user.getTeam());

            arena.setPhase(RoomPhase.ENDING);
        }
    }
}
