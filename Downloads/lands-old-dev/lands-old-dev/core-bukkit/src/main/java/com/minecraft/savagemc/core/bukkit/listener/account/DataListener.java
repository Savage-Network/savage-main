package com.minecraft.savagemc.core.bukkit.listener.account;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.route.RouteContext;
import com.minecraft.savagemc.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.savagemc.core.api.skin.Skin;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.backend.data.list.user.AccountData;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.savagemc.core.bukkit.manager.list.PermissionManager;
import com.minecraft.savagemc.core.bukkit.manager.list.TagManager;
import com.minecraft.savagemc.core.bukkit.user.UserModel;
import com.minecraft.savagemc.core.controller.list.AccountController;
import com.minecraft.savagemc.core.util.Util;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DataListener implements Listener {

    private final AccountData data;
    private final AccountController controller;

    public DataListener() {
        this.data = Savage.getAccountData();
        this.controller = Savage.getAccountController();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void load(AsyncPlayerPreLoginEvent event) {
        UUID id = event.getUniqueId();
        String name = event.getName();

        Instant startTime = Instant.now();

        try {
            Account account = data.of(id, true);

            if (account == null) {
                controller.remove(id);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
                return;
            }

            controller.save(account);

            Savage.Savage.Logger().info("[Account] Conta de " + name + " iniciada. (Tempo: " + Util.formatInstant(startTime) + ")");

            event.allow();
        } catch (Exception e) {
            controller.remove(id);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
            Savage.Savage.Logger().log(Level.SEVERE, "Erro ao carregar a conta de " + name + "!", e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void check(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        Account account = controller.of(player.getUniqueId());

        if (account != null) {
            account.setDefaultProfileSkin();

            event.allow();
        } else
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void join(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        player.refresh();

        Account account = controller.of(player.getUniqueId());

        if (account == null) {
            player.kickPlayer(Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
            return;
        }

        if (!account.hasTag(account.getTag()))
            account.setTag(account.getDefaultTag());

        if (account.getSkin().isValid())
            ProtocolHandler.changePlayerSkin(player, account.getSkin(), false);

        if (account.isUsingFake())
            ProtocolHandler.changePlayerName(player, account.getNickname());

        if (!Savage.getServerType().isArcade())
            account.setRoute(RouteContext.builder()
                    .senderId(account.getId())
                    .serverType(Savage.getServerType())
                    .serverId(Savage.getServerId())
                    .serverPort(Savage.getServerPort())
                    .arcade(ArcadeRouteContext.builder().build())
                    .updatedAt(System.currentTimeMillis())
                    .build());

        player.setPlayerListHeaderFooter(new TextComponent(Constant.SERVER_TAB_HEADER), new TextComponent(Constant.SERVER_TAB_FOOTER));

        PermissionManager.loadPermissions(player);

        Savage.Savage.Logger().info(player.getName() + " entrou no servidor.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void quit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        handlePlayerExit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void kick(PlayerKickEvent event) {
        event.setLeaveMessage(null);
        handlePlayerExit(event.getPlayer());
    }

    private void handlePlayerExit(Player player) {
        UserModel.remove(player.getUniqueId());

        PermissionManager.unloadPermissions(player);

        BukkitCore.getManager().getSidebar().remove(player);
        BukkitCore.getManager().getCooldown().resetCooldown(player);

        TagManager.removeTag(player);
        CollectibleOperator.unload(player);

        controller.remove(player.getUniqueId());

        Savage.Savage.Logger().info(player.getName() + " saiu do servidor.");
    }
}
