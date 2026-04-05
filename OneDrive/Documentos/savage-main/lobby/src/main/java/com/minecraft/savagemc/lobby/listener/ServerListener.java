package com.minecraft.savagemc.lobby.listener;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.toggle.Toggle;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.event.type.account.AccountClanChangeEvent;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.savagemc.core.bukkit.manager.list.TagManager;
import com.minecraft.savagemc.core.bukkit.user.UserModel;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.lobby.Lobby;
import com.minecraft.savagemc.lobby.architect.Architect;
import com.minecraft.savagemc.lobby.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class ServerListener implements Listener {

    private final Architect architect;

    public ServerListener() {
        this.architect = Lobby.getLoader().getArchitect();
    }

    @EventHandler
    public void onAsyncCounterUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            ServerType realServer = architect.getServer().getRealServerOfLobby();

            if (realServer != null) {
                for (ArcadeCategory arcade : ArcadeCategory.list(category -> category.getServer().equals(realServer))) {
                    HologramServer hologram = BukkitCore.getManager().getHologram().getServer(arcade.getId());

                    if (hologram != null) {
                        int onlinePlayers = Savage.getArcadeData().getOnlinePlayers(arcade);

                        hologram.setText(hologram.getRows().size() - 1, "§e" + Util.formatNumber(onlinePlayers) + " jogando.");
                    }
                }
            }

            // Atualizar players na scoreboard
            for (UserModel user : User.getList()) {
                if (user != null) {
                    Sidebar sidebar = user.getSidebar();

                    if (sidebar != null)
                        sidebar.updateRow("players", "Jogadores: §a" + Util.formatNumber(Savage.getServerData().getOnlinePlayers()));
                }
            }
        }
    }

    @EventHandler
    public void onVoid(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            event.setCancelled(true);

            Location spawn = architect.getLocation("spawn");

            if (spawn != null)
                player.teleport(spawn);
        }
    }

    @EventHandler
    public void onAccountClan(AccountClanChangeEvent event) {
        Account account = event.getAccount();

        User user = (User) User.of(account.getId());

        if (user != null)
            user.getSidebar().updateRow("clan", "Clan: §7" + (account.hasClan() ? account.getClanName() : "Nenhum"));
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        architect.chat(player, event.getMessage());
    }

    @EventHandler
    public void onSlimeJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Material down = player.getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType();

        if (down.isType(Material.SLIME_BLOCK)) {
            player.setVelocity(architect.getSlimeJump(player.getLocation().getDirection()));

            player.playSound(Sound.FIREWORK_LAUNCH, 2, 1);
        }
    }

    @EventHandler
    public void onVisibilityItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Account account = Savage.getAccountController().of(player.getUniqueId());

        if (account == null) return;

        ItemStack hand = event.getItem();

        if (event.hasBlock() && event.getClickedBlock().getType().name().contains("BUTTON"))
            event.setCancelled(true);

        if (event.hasItem() && hand != null) {
            if (hand.getType().equals(Material.INK_SACK)) {
                Toggle toggle = account.getToggle();

                if (account.hasCooldown(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY)) {
                    account.send("§cAguarde " + account.getFormattedCooldown(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY)
                            + " para " + (toggle.isVisiblePlayersHub() ? "esconder" : "mostrar") + " os jogadores novamente.");
                    return;
                }

                toggle.setVisiblePlayersHub(!toggle.isVisiblePlayersHub());
                account.setToggle(toggle);

                player.setItemInHand(Item.of(Material.INK_SACK, toggle.isVisiblePlayersHub() ? 10 : 8,
                        "§fJogadores: " + (toggle.isVisiblePlayersHub() ? "§aON" : "§cOFF")));

                player.updateInventory();

                Bukkit.getOnlinePlayers().forEach(online -> {
                    if (toggle.isVisiblePlayersHub())
                        player.showPlayer(online);
                    else
                        player.hidePlayer(online);
                });

                player.playSound(Sound.CLICK, 1, 1f);

                account.setCooldown(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(5));
            }
        }
    }
}


