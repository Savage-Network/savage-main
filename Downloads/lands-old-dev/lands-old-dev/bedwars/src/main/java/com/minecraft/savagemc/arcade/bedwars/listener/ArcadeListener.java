package com.minecraft.savagemc.arcade.bedwars.listener;

import com.minecraft.savagemc.arcade.bedwars.arcade.Arcade;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.enums.TopType;
import com.minecraft.savagemc.arcade.bedwars.event.PlayerBedBreakEvent;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.party.Party;
import com.minecraft.savagemc.core.arcade.ArcadeHolder;
import com.minecraft.savagemc.core.arcade.room.event.ArenaSearchedEvent;
import com.minecraft.savagemc.core.arcade.room.map.rollback.RollbackBlock;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.option.ServerOptions;
import com.minecraft.savagemc.core.bukkit.event.type.account.AccountVanishLogEvent;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerArenaWarpEvent;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.savagemc.core.bukkit.manager.Manager;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list.Random;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import com.minecraft.savagemc.core.util.list.bukkit.ColorUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.github.paperspigot.Title;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArcadeListener implements Listener {

    private final Manager manager = BukkitCore.getManager();

    private final List<Material> ALLOWED_DROP_ITEMS = Arrays.asList(
            Material.BOW, Material.ARROW, Material.GOLDEN_APPLE, Material.ENDER_PEARL,
            Material.EGG, Material.FIREBALL, Material.MAGMA_CREAM, Material.STICK
    );

    private final List<String> BLOCKED_CHEST_ITEMS = Arrays.asList(
            "SHEARS", "_AXE", "_PICKAXE", "WOOD_SWORD"
    );

    @EventHandler
    public void onSyncUpdate(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (ArcadeHolder holder : manager.getArcade().getArcades()) {
                if (!(holder instanceof Arcade)) continue;

                Arcade arcade = (Arcade) holder;

                List<Arena> arenaList = arcade.getRooms().stream().map(room -> (Arena) room).collect(Collectors.toList());

                arcade.checkAndGenerateArenas();

                arenaList.forEach(Arena::handlePulse);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onArenaSearched(ArenaSearchedEvent event) {
        Arena arena = (Arena) event.getArena();

        User user = (User) User.of(event.getSender().getId());

        if (user == null) return;

        Player player = user.getPlayer();

        // Já está no servidor, apenas enviar para a sala
        if (player != null) {
            event.setCancelled(true);

            ArcadeRouteContext route = ArcadeRouteContext.builder()
                    .arcade(arena.getArcade().getCategory())
                    .roomId(arena.getId())
                    .mapId(arena.getMap().getId())
                    .slot(arena.getSlot())
                    .serverId(Savage.getServerId())
                    .arenaIdentifier(arena.getIdentifier())
                    .state(ArcadeState.ALIVE)
                    .maxPlayers(arena.getMaxPlayers())
                    .join(event.getRoute().getJoin())
                    .build();

            Savage.getPlatform().runSync(() -> {
                Arena current = user.getArena();

                current.quit(player);

                user.setArena(arena);

                arena.join(player);

                // Ver questão da party
                Party party = user.getAccount().getParty();

                if (party != null && party.isAuthor(player.getUniqueId()))
                    party.redirect(route);
            });
        }
    }

    @EventHandler
    public void onPartyWarp(PlayerArenaWarpEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user != null && user.isPlayer()) {
            Arena current = user.getArena(), arena = (Arena) event.getArena();

            Savage.getPlatform().runSync(() -> {
                current.quit(player);

                user.setArena(arena);

                arena.join(player);
            });
        }
    }

    @EventHandler
    public void onChestInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Arena arena = user.getArena();

        Team team = user.getTeam();

        Block clicked = event.getClickedBlock();

        if (event.hasBlock()) {

            if (!user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
                event.setCancelled(true);
                return;
            }

            if (clicked.getType().equals(Material.CHEST) && !team.inIsland(clicked.getLocation())) {
                Team chestTeam = arena.getTeamList().stream().filter(search -> search.inIsland(clicked.getLocation()))
                        .findFirst().orElse(null);

                if (chestTeam != null && !chestTeam.isDead()) {
                    event.setCancelled(true);

                    player.sendMessage("§cVocê não pode abrir o baú de outro time enquanto ele está vivo.");
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            event.setCancelled(true);
            return;
        }

        ItemStack stack = event.getItemDrop().getItemStack();

        if (stack == null || (stack.isType(Material.AIR) || stack.isType(Material.WOOD_SWORD))) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();

        if (!arena.isPhase(RoomPhase.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        if (stack.isOre() || stack.getType().isBlock()) {
            event.setCancelled(false);
            return;
        }

        /* Dropou espada de ferro, pedra, etc, volta a sword de madeira pro inventário */
        if (stack.getType().name().contains("_SWORD") && !stack.getType().equals(Material.WOOD_SWORD)) {
            event.setCancelled(false);

            if (!BukkitUtil.hasItemInInventory(player, Material.WOOD_SWORD))
                player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));

            return;
        }

        event.setCancelled(!ALLOWED_DROP_ITEMS.contains(stack.getType()));
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack hand = player.getItemInHand();

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && event.hasBlock() && ServerOptions.NON_INTERACTIVE_BLOCKS.contains(event.getClickedBlock().getType())) {

            if (!player.isSneaking() || hand == null || !hand.getType().isBlock())
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockChestItems(InventoryClickEvent event) {
        // Bloquear interação com armadura
        if (event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.PLAYER)
                && event.getSlot() >= 36 && event.getSlot() <= 39) {
            event.setCancelled(true);
            return;
        }

        // Bloquear interações com itens específicos no baú
        Inventory top = event.getView().getTopInventory();

        if (top.getSize() == 27 && top.getType().name().contains("CHEST")) {
            ItemStack item = event.getAction().equals(InventoryAction.HOTBAR_SWAP)
                    ? event.getWhoClicked().getInventory().getItem(event.getHotbarButton())
                    : event.getCurrentItem();

            if (item != null && (BLOCKED_CHEST_ITEMS.stream().anyMatch(name -> item.getType().name().contains(name))
                    || item.isSimilar(BedWarsItem.UTIL_TRACKING.getStack())))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arena arena = user.getArena();

        Arena.ChatType type = arena.isSlot(Slot.SOLO) || !arena.isPhase(RoomPhase.PLAYING) || !user.isPlayer()
                ? Arena.ChatType.GLOBAL
                : Arena.ChatType.TEAM;

        arena.chat(type, user, event.getMessage());
    }

    @EventHandler
    public void onVanishLeave(AccountVanishLogEvent event) {
        Account account = event.getAccount();

        if (event.isState(AccountVanishLogEvent.VanishState.LEAVE)) {
            User user = (User) User.of(account.getId());

            if (user != null) {
                Arena arena = user.getArena();

                if (arena.isPreGame())
                    arena.getTeamList().stream()
                            .filter(search -> !search.isFull())
                            .findFirst().ifPresent(user::setTeam);
            }
        }
    }

    @EventHandler
    public void onSeeds(ItemSpawnEvent event) {
        ItemStack stack = event.getEntity().getItemStack();

        if (stack != null && stack.isType(Material.SEEDS))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();

        Block block = event.getBlockPlaced();

        if (arena.isProtectedArea(block.getLocation())) {
            event.setCancelled(true);

            player.sendMessage("§cVocê não pode colocar blocos aqui.");
            return;
        }

        event.setCancelled(!arena.isPhase(RoomPhase.PLAYING));

        if (!arena.isReversible(block))
            arena.addRollBack(block, RollbackBlock.RollbackType.PLACE_BLOCK);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();

        if (!arena.isPhase(RoomPhase.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        Team team = user.getTeam();

        Block block = event.getBlock();

        boolean isBed = block.getType().name().contains("BED");

        if (isBed && team.isYourBed(block.getLocation())) {
            event.setCancelled(true);

            player.sendMessage("§cVocê não pode quebrar a sua cama.");
            return;
        }

        if (block.getType().name().contains("GRASS") && !block.getType().equals(Material.GRASS)) {
            event.setCancelled(true);

            block.setType(Material.AIR);
            return;
        }

        if (!arena.isReversible(block) && !isBed) {
            event.setCancelled(true);

            player.sendMessage("§cVocê não pode quebrar este bloco.");
            return;
        }

        if (!event.isCancelled()) {

            if (isBed) {
                Team bedTeam = arena.getTeam(block.getLocation());

                if (bedTeam != null)
                    new PlayerBedBreakEvent(player, arena, team, bedTeam, block.getLocation()).call();
                else {
                    event.setCancelled(true);

                    player.sendMessage("§cNão foi possível quebrar a cama. Contate um administrador!");
                }
            }
        }
    }

    private final List<String> bedBreakMessages = Arrays.asList(
            "foi completamente obliterada por", "foi destruída por", "foi transformada em cinzas por", "foi desmontada por"
    );

    @EventHandler
    public void onPlayerBedBreak(PlayerBedBreakEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arena arena = event.getArena();
        Team team = event.getTeam(), bedTeam = event.getBedTeam();

        String message = bedBreakMessages.get(Savage.RANDOM.nextInt(bedBreakMessages.size()));

        BedMember member = user.getMember();

        user.setBedDestruction();
        arena.getArcade().updateSidebar(user);

        arena.verifyTop(TopType.BED_DESTRUCTION, player.getUniqueId(), user.getBedDestruction());

        arena.send("",
                "§f§lCAMA DESTRUÍDA §f» §7A " + bedTeam.getColor() + "Cama do " + bedTeam.getName()
                        + "§7 " + message + " " + team.getColor() + player.getName() + "§7.",
                "");

        // Renderizar animação

        BedDestroyCollectible destroy = (BedDestroyCollectible) member.getActiveCollectible(BedCollectibleType.BED_DESTRUCTION);

        if (destroy != null) {
            if (!destroy.isNotEqual(Random.class)) {
                destroy = (BedDestroyCollectible) member.getRandomCollectible(BedCollectibleType.BED_DESTRUCTION);

                if (destroy == null)
                    destroy = (BedDestroyCollectible) member.getCollectible(BedCollectibleType.BED_DESTRUCTION, "Nenhuma");
            }

            destroy.execute(event.getLocation());
        }

        Savage.getPlatform().runSync(() -> arena.getUsers().forEach(arena::updateSidebarTeams), 2L);

        bedTeam.getPlayers().forEach(target -> target.sendTitle(new Title("§c§lCAMA DESTRUÍDA", "§7Você não vai mais renascer!", 0, 40, 90)));

        arena.sound(Sound.ENDERDRAGON_GROWL, 13, 1);
    }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE) || !user.getArena().isPhase(RoomPhase.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        Item item = event.getItem();

        ItemStack stack = item.getItemStack();

        // Item é um minério
        if (stack.isOre()) {
            ItemMeta meta = stack.getItemMeta();

            if (meta != null && meta.hasDisplayName()) {
                meta.setDisplayName(null);
                stack.setItemMeta(meta);
            }

            return;
        }

        if (stack.getType().name().contains("_SWORD") && !stack.getType().equals(Material.WOOD_SWORD)) {
            if (BukkitUtil.hasItemInInventory(player, Material.WOOD_SWORD))
                player.getInventory().remove(Material.WOOD_SWORD);

            return;
        }

        if (stack.isType(Material.WOOL) || stack.isType(Material.STAINED_CLAY)) {
            Team team = user.getTeam();

            int hexColor = ColorUtil.getIdByColor(team.getColor());

            if (stack.getDurability() != hexColor)
                stack.setDurability((short) hexColor);
        }
    }
}
