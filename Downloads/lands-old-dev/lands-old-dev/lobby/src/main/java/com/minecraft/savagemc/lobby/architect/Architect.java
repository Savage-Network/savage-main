package com.minecraft.savagemc.lobby.architect;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.account.context.objects.tag.role.TagRole;
import com.minecraft.savagemc.core.account.context.objects.toggle.Toggle;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.map.location.SignedLocation;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.savagemc.core.bukkit.api.npc.action.NpcAction;
import com.minecraft.savagemc.core.bukkit.api.npc.type.client.NpcClient;
import com.minecraft.savagemc.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.savagemc.core.bukkit.menu.account.AccountMenu;
import com.minecraft.savagemc.core.bukkit.menu.account.stats.StatsInfoMenu;
import com.minecraft.savagemc.core.bukkit.menu.server.arcade.mode.bedwars.stats.BedWarsStatsMenu;
import com.minecraft.savagemc.core.bukkit.menu.server.collectible.CollectibleMenu;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.StringUtil;
import com.minecraft.savagemc.core.util.list.WorldUtil;
import com.minecraft.savagemc.lobby.Lobby;
import com.minecraft.savagemc.lobby.menu.arcade.ArcadeSlotMenu;
import com.minecraft.savagemc.lobby.menu.navigation.NavigationMenu;
import com.minecraft.savagemc.lobby.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@IgnoreEvent
public abstract class Architect implements Listener {

    private final String id = StringUtil.generateLetterCode(4) + Savage.getServerId();
    private final ServerType server;

    private final List<SignedLocation> locations = new ArrayList<>();

    private static final List<String> JOIN_MESSAGES = Arrays.asList(
            "entrou no lobby!",
            "se conectou!",
            "chegou chegando!",
            "invadiu o lobby!",
            "marcou presença!",
            "acabou de aterrissar!"
    );

    public void load() {
        /* Limpa o mundo antes de spawnar entidades reais (ex.: armor stand).
         * NPCs/hologramas do core são por pacote/NMS e não dependem desta ordem. */
        WorldUtil.setup(getWorld());

        handleEntities();

        Bukkit.getPluginManager().registerEvents(this, Lobby.getInstance());
    }

    public void unload() {

    }

    public World getWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public void join(Player player) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        handleSidebar(user);
        handleHotbar(player);

        if (account.isVIP()) {
            player.setFlying(true);

            Tag tag = account.getTag();

            if (tag.ordinal() >= Tag.TAG_2026.ordinal() && !tag.isRole(TagRole.STAFF)) {
                String message = JOIN_MESSAGES.get(Savage.RANDOM.nextInt(JOIN_MESSAGES.size()));

                // Enviar apenas para jogadores no mesmo servidor
                player.getServer().getOnlinePlayers().forEach(targetPlayer -> {
                    Account targetAccount = Savage.getAccountController().of(targetPlayer.getUniqueId());
                    if (targetAccount != null) {
                        targetAccount.send(tag.getByPrefix(targetAccount.getTagPrefix()) + account.getNickname() + "§6 " + message);
                    }
                });
            }
        }

        /* Esconder todos os jogadores, caso estejam invisíveis */
        if (!account.getToggle().isVisiblePlayersHub())
            Bukkit.getOnlinePlayers().forEach(player::hidePlayer);

        Location location = getLocation("npc_stats");

        /* Renderizar NPC de suas estatísticas */
        if (location != null) {
            CompletableFuture.runAsync(() -> {
                NpcClient stats = BukkitCore.getManager().getNpc().spawnClient(player, location,
                        account.getSkin().getValue(), account.getSkin().getSignature());

                stats.setContact(true);
                stats.setAction((target, action) -> {
                    Account other = Savage.getAccountController().of(target.getUniqueId());

                    if (other != null) {
                        ServerType server = getServer().getRealServerOfLobby();

                        if (server == null) return;

                        if (server.equals(ServerType.BEDWARS))
                            new BedWarsStatsMenu(target, null).handle();
                        else
                            new StatsInfoMenu(target, other, server, null).handle();
                    }
                });

                stats.display();
            });
        }
    }

    public void quit(Player player) {
    }

    public abstract void handleSidebar(User user);

    public void chat(Player player, String message) {

    }

    public void handleEntities() {
    }

    public Vector getSlimeJump(Vector direction) {
        return new Vector(0, 0, 0);
    }

    public Location getLocation(String name) {
        return locations.stream()
                .filter(location -> location.getName().equalsIgnoreCase(name))
                .map(location -> location.getSynthetic().of(Bukkit.getWorlds().get(0)))
                .findFirst()
                .orElse(null);
    }

    public List<SignedLocation> getLocationsByName(String name) {
        return locations.stream()
                .filter(location -> location.getName().startsWith(name))
                .collect(Collectors.toList());
    }

    public List<Location> getLocationsByKey(String... names) {
        List<Location> list = new ArrayList<>();

        for (String name : names) {
            Location location = getLocation(name);

            if (location != null)
                list.add(location);
        }

        return list;
    }

    public void handleHotbar(Player player) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();
        Toggle toggle = account.getToggle();

        PlayerInventory inv = player.getInventory();

        inv.clear();
        inv.setArmorContents(null);

        inv.setItem(0, Item.of(Material.COMPASS, "§aNavegação")
                .interact(event -> new NavigationMenu(event.getPlayer(), NavigationMenu.NavigationType.SERVER).handle()));

        inv.setItem(1, Item.of(Material.SKULL_ITEM, 3, "§aMinha conta")
                .skullByBase64(account.getSkin().getValue())
                .interact(event -> new AccountMenu(event.getPlayer()).handle()));

        inv.setItem(4, Item.of(Material.CHEST, "§aColecionáveis")
                .interact(event -> new CollectibleMenu(event.getPlayer()).handle()));

        inv.setItem(7, Item.of(Material.INK_SACK, toggle.isVisiblePlayersHub() ? 10 : 8,
                "§fJogadores: " + (toggle.isVisiblePlayersHub() ? "§aON" : "§cOFF")));

        inv.setItem(8, Item.of(Material.ACACIA_DOOR_ITEM, "§aSelecionar sala")
                .interact(event -> new NavigationMenu(event.getPlayer(), NavigationMenu.NavigationType.ROOM).handle()));
    }

    public void handleStatsNpc(Location location) {
        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer("stats_" + getServer().name().toLowerCase(), location);

        hologram.setText(Arrays.asList(
                "§6§lSUAS ESTATÍSTICAS",
                "§eClique para ver"
        ));
    }

    public NpcServer handleNpc(String name, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(name.toLowerCase(), location);

        hologram.setText(Arrays.asList(
                "§6§l" + name.toUpperCase(),
                "§e" + Util.formatNumber(0) + " jogando."
        ));

        return npc;
    }

    public void handleServerNpc(ServerType server, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setAction((player, action) -> {
            User user = (User) User.of(player.getUniqueId());

            if (user != null)
                user.getAccount().redirect(server);
        });

        npc.setContact(true);

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(server.name().toLowerCase(), location);

        hologram.setText(Arrays.asList(
                "§6§l" + server.getName().toUpperCase(),
                "§e" + Util.formatNumber(Savage.getServerData().getOnlinePlayers(server)) + " jogando."
        ));
    }

    public void handleCommandNpc(String hologramId, Location location, String value, String signature, List<String> hologramLines, String command) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setContact(true);
        npc.setAction((player, action) -> player.performCommand(command));

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(hologramId, location);
        hologram.setText(hologramLines);
    }

    /**
     * Armor stand como no comando /summon: braços, sem base, couro (bota/calça/peitoral) e cabeça
     * SkullOwner "Cash Register" (textura do Value que passaste).
     */
    public void handleShopCashRegisterStand(Location location) {
        if (location == null || location.getWorld() == null) return;

        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setGravity(false);
        stand.setVisible(true);
        stand.setSmall(false);
        stand.setArms(true);
        stand.setBasePlate(false);

        Item helmet = Item.of(Material.SKULL_ITEM, 3).skullByBase64(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZmYjQ0MzFlODZlNjIxYTgxMjcxZTlhOGQ1OTQ0YTI5MjRjOGM4MmE4NDE5NjY4ZWE4MWZlNzc5ZWE5In19fQ==")
                .name("Cash Register");

        stand.getEquipment().setHelmet(helmet);
        stand.getEquipment().setChestplate(Item.of(Material.LEATHER_CHESTPLATE));
        stand.getEquipment().setLeggings(Item.of(Material.LEATHER_LEGGINGS));
        stand.getEquipment().setBoots(Item.of(Material.LEATHER_BOOTS));
    }

    /**
     * Cria blocos pequenos girando ao redor do NPC da loja
     * Bloco de diamante e esmeralda girando em torno do NPC
     */
    public void handleShopRotatingBlocks(Location npcLocation) {
        if (npcLocation == null || npcLocation.getWorld() == null) return;

        // Bloco de diamante (na frente)
        ArmorStand diamondStand = npcLocation.getWorld().spawn(npcLocation.clone().add(1, 1.5, 0), ArmorStand.class);
        diamondStand.setGravity(false);
        diamondStand.setVisible(false);
        diamondStand.setSmall(true);
        diamondStand.setBasePlate(false);
        diamondStand.setMarker(true);
        diamondStand.getEquipment().setHelmet(Item.of(Material.DIAMOND_BLOCK));
        diamondStand.setCustomName("shop_diamond");
        diamondStand.setCustomNameVisible(false);

        // Bloco de esmeralda (atrás)
        ArmorStand emeraldStand = npcLocation.getWorld().spawn(npcLocation.clone().add(-1, 1.5, 0), ArmorStand.class);
        emeraldStand.setGravity(false);
        emeraldStand.setVisible(false);
        emeraldStand.setSmall(true);
        emeraldStand.setBasePlate(false);
        emeraldStand.setMarker(true);
        emeraldStand.getEquipment().setHelmet(Item.of(Material.EMERALD_BLOCK));
        emeraldStand.setCustomName("shop_emerald");
        emeraldStand.setCustomNameVisible(false);

        // Task para rotacionar os blocos
        Bukkit.getScheduler().runTaskTimer(Lobby.getInstance(), () -> {
            if (!diamondStand.isValid() || !emeraldStand.isValid()) return;

            double angle = (System.currentTimeMillis() % 3600) / 3600.0 * 2 * Math.PI;
            double radius = 1.0;

            // Diamante girando
            double diamondX = npcLocation.getX() + Math.cos(angle) * radius;
            double diamondZ = npcLocation.getZ() + Math.sin(angle) * radius;
            diamondStand.teleport(new Location(npcLocation.getWorld(), diamondX, npcLocation.getY() + 1.5, diamondZ));

            // Esmeralda girando (oposto ao diamante)
            double emeraldX = npcLocation.getX() + Math.cos(angle + Math.PI) * radius;
            double emeraldZ = npcLocation.getZ() + Math.sin(angle + Math.PI) * radius;
            emeraldStand.teleport(new Location(npcLocation.getWorld(), emeraldX, npcLocation.getY() + 1.5, emeraldZ));

        }, 0L, 1L);
    }

    public void handleArcadeNpc(ArcadeCategory arcade, Location location, NpcAction action, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setContact(true);
        npc.setAction(action);

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(arcade.getId(), location);

        hologram.setText(Arrays.asList(
                "§6§l" + (arcade.equals(ArcadeCategory.NONE) ? "§k?????" : arcade.getName().toUpperCase()),
                "§e" + Util.formatNumber(arcade.getPlayingNow()) + " jogando."
        ));
    }

    public void handleArcadeNpc(ArcadeCategory arcade, Location location, String value, String signature) {
        handleArcadeNpc(arcade, location, (player, action) -> new ArcadeSlotMenu(player, null, arcade).handle(), value, signature);
    }
}
