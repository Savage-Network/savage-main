package com.minecraft.savagemc.arcade.duels.arcade.list.combat.skywars;

import com.minecraft.savagemc.arcade.duels.arcade.Arcade;
import com.minecraft.savagemc.arcade.duels.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.duels.arcade.objects.style.SidebarStyle;
import com.minecraft.savagemc.arcade.duels.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.event.ArenaPhaseEvent;
import com.minecraft.savagemc.core.arcade.room.map.location.SignedLocation;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.manager.list.TagManager;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import com.minecraft.savagemc.core.member.context.stats.ArcadeStats;
import com.minecraft.savagemc.core.member.list.duels.DuelMember;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.DateUtil;
import com.minecraft.savagemc.core.util.list.TimeUtil;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@IgnoreEvent
public class SkyWars extends Arcade {

    private final String REFILL_KEY = "refill";
    private final int defaultRefillTime = 90;

    public SkyWars(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.DUELS_SKY_WARS);

        setStyle(SidebarStyle.LATENCY);
    }

    @Override
    public void handleSidebar(User user) {
        Sidebar sidebar = user.getSidebar();

        Arena arena = user.getArena();

        DuelMember member = user.getMember();

        sidebar.clear();
        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + user.getArena().getIdentifier());
        sidebar.blankRow();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            sidebar.addRow("event_title", "§ePróximo evento:");
            sidebar.addRow("event", " Refill: §a" + TimeUtil.time((int) arena.getProperty(REFILL_KEY)));

            sidebar.blankRow();

            handleTeamStyle(arena, sidebar);

            if (user.isPlayer()) {
                ArcadeStats stats = member.getStats(getCategory());

                sidebar.addRow("elo", "Elo: " + member.getElo().getFullName());
                sidebar.addRow("winstreak", "Winstreak: §a" + Util.formatNumber(stats.getWinStreak()));
            } else {
                sidebar.blankRow();
                sidebar.addRow("state", user.isVanish() ? "§cMODO VANISH" : "§eMODO ESPECTADOR");
            }
        } else {
            sidebar.addRow("mode", "Modo: §a" + arena.getModeName());
            sidebar.addRow("map", "Mapa: §a" + arena.getMap().getName());
            sidebar.blankRow();

            if (user.isPlayer())
                arena.createTimer(sidebar);
            else
                sidebar.addRow("state", user.isVanish() ? "§cMODO VANISH" : "§eMODO ESPECTADOR");

            sidebar.blankRow();
            sidebar.addRow("players", "Jogadores: §a" + arena.getMatchUsers().size() + "/" + arena.getMaxPlayers());
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void handleProperties(Arena arena) {
        arena.writeProperty(REFILL_KEY, defaultRefillTime);
    }

    @Override
    public void timer(Arena arena) {
        if (arena.hasProperty(REFILL_KEY) && arena.isPhase(RoomPhase.PLAYING)) {
            final int localTime = (int) arena.getProperty(REFILL_KEY);

            arena.writeProperty(REFILL_KEY, localTime - 1);

            int time = (int) arena.getProperty(REFILL_KEY);

            // Atualizar tempo na scoreboard
            arena.getMatchUsers().forEach(user -> {
                Sidebar sidebar = user.getSidebar();

                if (sidebar != null)
                    sidebar.updateRow("event", " Refill: §a" + TimeUtil.time(time));
            });

            if (time <= 3 && time >= 1) {
                arena.send("§e" + time + "...");

                arena.sound(Sound.NOTE_PLING);
            }

            if (time <= 0) {
                arena.writeProperty(REFILL_KEY, defaultRefillTime);

                handleChests(arena);

                arena.send("§aOs baús foram reabastecidos.");
                arena.sound(Sound.LEVEL_UP);
            }

        }
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (arena.isValid(player)) {
            player.clearInventory();

            BukkitUtil.sendArmor(player, BukkitUtil.ArmorType.IRON);
        } else
            handleDefaultHotbar(player);
    }

    @EventHandler
    public void onStart(ArenaPhaseEvent event) {
        Arena arena = (Arena) event.getArena();

        if (event.isPhase(RoomPhase.PLAYING) && arena.isCategory(ArcadeCategory.DUELS_SKY_WARS))
            handleChests(arena);
    }

    @EventHandler
    public void onChest(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isValid(player) && event.hasBlock()) {
            Arena arena = (Arena) getRoom(player.getWorld());

            Block block = event.getClickedBlock();

            if (arena.isPhase(RoomPhase.PLAYING) && block.getType().equals(Material.CHEST))
                event.setCancelled(false);
        }
    }

    public void handleChests(Arena arena) {
        List<SignedLocation> chests = arena.getSignedLocations(signed -> signed.getName().toLowerCase().contains("chest"));

        if (chests.isEmpty()) {
            Savage.Savage.Logger().warning("Não há baús na arena " + arena.getIdentifier() + " (" + arena.getArcade().getName() + ").");
            return;
        }

        chests.forEach(signed -> {
            Location location = signed.getSynthetic().of(arena.getWorld());

            if (location != null) {
                Block block = location.getBlock();

                if (block.getType().equals(Material.CHEST)) {
                    Chest chest = (Chest) block.getState();

                    if (chest != null) {
                        String[] findNumber = signed.getName().split("_");

                        int id = Integer.parseInt(findNumber[findNumber.length - 1]);

                        fillChest(chest.getBlockInventory(), ChestCategory.fromId(id));
                    }
                }
            }
        });
    }

    public void fillChest(Inventory menu, ChestCategory category) {
        List<ItemStack> contents = category.getContents();

        menu.clear();

        for (ItemStack content : contents) {
            if (content == null || content.isType(Material.AIR) || menu.contains(content)) continue;

            int randomPosition = Savage.RANDOM.nextInt(menu.getSize());

            while (menu.getItem(randomPosition) != null)
                randomPosition = Savage.RANDOM.nextInt(menu.getSize());

            menu.setItem(randomPosition, content);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum ChestCategory {

        PRIMARY(
                Item.of(Material.GOLDEN_APPLE), Item.of(Material.WATER_BUCKET), Item.of(Material.WOOD).amount(38),
                Item.of(Material.EGG).amount(8), Item.of(Material.DIAMOND_SWORD)
        ),
        SECONDARY(
                Item.of(Material.EGG).amount(8), Item.of(Material.EXP_BOTTLE).amount(12), Item.of(Material.DIAMOND_BOOTS),
                Item.of(Material.LAVA_BUCKET), Item.of(Material.WOOD).amount(32), Item.of(Material.POTION, 16385)
        );

        private final List<ItemStack> contents = new ArrayList<>();

        ChestCategory(ItemStack... contents) {
            this.contents.addAll(Arrays.asList(contents));
        }

        public static ChestCategory fromId(int id) {
            return Arrays.stream(values()).filter(category -> (category.ordinal() + 1) == id).findFirst().orElse(null);
        }
    }
}
