package com.minecraft.savagemc.core.bungee.listener;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.punishment.Punishment;
import com.minecraft.savagemc.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.savagemc.core.api.reconnect.Reconnect;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.bungee.BungeeCore;
import com.minecraft.savagemc.core.bungee.event.list.account.AccountRefreshEvent;
import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.Rank;
import com.minecraft.savagemc.core.account.context.objects.route.RouteContext;
import com.minecraft.savagemc.core.api.mojang.MojangApi;
import com.minecraft.savagemc.core.backend.data.list.user.AccountData;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountSkinResetMessage;
import com.minecraft.savagemc.core.controller.list.AccountController;
import com.minecraft.savagemc.core.server.Server;
import com.minecraft.savagemc.core.server.flag.ServerFlag;
import com.minecraft.savagemc.core.server.payload.whitelist.ServerWhitelist;
import com.minecraft.savagemc.core.util.list.Validator;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DataListener implements Listener {

    private final BungeeCore bungee = BungeeCore.getInstance();

    private final AccountData data = Savage.getAccountData();
    private final AccountController controller = Savage.getAccountController();

    @EventHandler
    public void onAccountRefresh(AccountRefreshEvent event) {
        Account account = event.getAccount();

        if (Savage.getPlatform().isOnlinePlayer(account.getId()))
            controller.save(account);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPreLogin(PreLoginEvent event) {
        PendingConnection connection = event.getConnection();

        String userName = connection.getName();
        String ip = connection.getAddress().getAddress().getHostAddress();

        event.registerIntent(bungee);

        CompletableFuture.runAsync(() -> handlePreLogin(event, userName, ip, connection)).thenRun(() -> event.completeIntent(bungee));
    }

    private void handlePreLogin(PreLoginEvent event, String userName, String ip, PendingConnection connection) {
        if (exceedsAccountLimit(userName, ip, event) || !isNicknameValid(userName, event) || !verifyPremiumStatus(userName, connection, event))
            return;
    }

    private boolean exceedsAccountLimit(String userName, String ip, PreLoginEvent event) {
        if (data.isExceededAccountLimit(userName, ip)) {
            event.setCancelReason(TextComponent.fromLegacyText(Constant.SERVER_TITLE + "\n\n" +
                    "§cVocê excedeu o limite de contas alternativas.\n" +
                    "§cPara resolver, acesse uma conta existente.\n\n" +
                    "§cEm caso de problemas, contate: §e" + Constant.SERVER_DISCORD));
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    private boolean isNicknameValid(String userName, PreLoginEvent event) {
        if (!Validator.isNickname(userName)) {
            Savage.Savage.Logger().warning("Nome de usuário inválido: " + userName);
            event.setCancelReason(TextComponent.fromLegacyText(Constant.SERVER_TITLE + "\n\n" +
                    "§cNome de usuário inválido ;(\n" +
                    "§cTente trocar e entrar novamente!"));
            event.setCancelled(true);
            return false;
        }
        return true;
    }

    private boolean verifyPremiumStatus(String userName, PendingConnection connection, PreLoginEvent event) {
        MojangApi.ResponseCode responseCode = Savage.MOJANG_API.isPremium(userName);

        connection.setOnlineMode(responseCode.equals(MojangApi.ResponseCode.DONE));

        if (responseCode.equals(MojangApi.ResponseCode.FAIL)) {
            connection.setUniqueId(UUID.nameUUIDFromBytes(("OfflinePlayer:" + userName.toUpperCase()).getBytes(StandardCharsets.UTF_8)));
        }

        if (responseCode.equals(MojangApi.ResponseCode.UNKNOWN)) {
            handleVerificationFailure(userName, event);
            return false;
        }

        return true;
    }

    private void handleVerificationFailure(String userName, PreLoginEvent event) {
        Savage.Savage.Logger().log(Level.WARNING, "Falha na verificação da conta: " + userName);
        event.setCancelReason(TextComponent.fromLegacyText(Constant.SERVER_TITLE + "\n\n" +
                "§cNão foi possível verificar sua conexão ;(\n" +
                "§cTente novamente mais tarde!"));
        event.setCancelled(true);
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        PendingConnection connection = event.getConnection();

        UUID id = connection.getUniqueId();

        String userName = connection.getName();
        String ip = connection.getAddress().getAddress().getHostAddress();

        event.registerIntent(bungee);

        CompletableFuture.runAsync(() -> loadAccountData(event, id, userName, ip)).thenRun(() -> event.completeIntent(bungee));
    }

    private void loadAccountData(LoginEvent event, UUID id, String userName, String ip) {
        try {
            Account account = data.of(id, true);
            if (account == null) account = createNewAccount(event, id, userName);

            if (account == null || !isAccountAllowedToJoin(account, event)) return;

            updateAccountDetails(account, userName, ip, event.getConnection().isOnlineMode());
            controller.save(account);

        } catch (Exception e) {
            controller.remove(id);
            Savage.Savage.Logger().log(Level.WARNING, "Erro ao carregar a conta de " + userName, e);
        }
    }

    private Account createNewAccount(LoginEvent event, UUID id, String userName) {
        Account account = data.save(new Account(id, userName));
        if (account == null) {
            Savage.Savage.Logger().warning("Conta não encontrada para " + userName);
            event.setCancelReason(TextComponent.fromLegacyText(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
            event.setCancelled(true);
        }

        return account;
    }

    private boolean isAccountAllowedToJoin(Account account, LoginEvent event) {
        Punishment punishment = Savage.getPunishmentData().of(account.getIpAddress(), PunishmentCategory.BAN);

        if (punishment != null) {
            data.removeCache(account);

            event.setCancelReason(TextComponent.fromLegacyText(String.format(Constant.BAN_TEMPLATE_MESSAGE,
                    punishment.isTemporary() ? "temporariamente" : "permanentemente",
                    punishment.getReason().getName(),
                    punishment.getId())));

            event.setCancelled(true);
            return false;
        }

        Server local = Savage.getLocalServer();

        ServerWhitelist whitelist = local.getWhitelist();

        if (whitelist.isEnabled() && !whitelist.hasPlayer(account.getName()) && !account.isStaffer()) {
            data.removeCache(account);

            event.setCancelReason(TextComponent.fromLegacyText(Constant.SERVER_TITLE + "\n\n" +
                    "§cServidores indisponíveis no momento.\n" +
                    "§cTente novamente mais tarde!\n\n" +
                    "§cDúvidas? Consulte: §e" + Constant.SERVER_DISCORD));

            event.setCancelled(true);
            return false;
        }

        return true;
    }

    private void updateAccountDetails(Account account, String userName, String ip, boolean isOnlineMode) {
        if (!account.isPremium() && isOnlineMode)
            account.setPremium(true);

        if (account.getRank().getType() == null)
            account.setRank(Rank.builder().build());

        if (account.getIpAddress() == null || !account.getIpAddress().equalsIgnoreCase(ip))
            account.setIpAddress(ip);

        if (!account.getName().equalsIgnoreCase(userName))
            account.setName(userName);

        data.cancelExpiration(account);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer proxied = event.getPlayer();

        CompletableFuture.runAsync(() -> postLoginAccountSetup(proxied));
    }

    private void postLoginAccountSetup(ProxiedPlayer proxied) {
        Account account = controller.of(proxied.getUniqueId());

        if (account == null) {
            proxied.disconnect(TextComponent.fromLegacyText(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
            return;
        }

        account.setOnline(true);
        account.notifyFriends();

        Savage.Savage.Logger().info(account.getName() + " entrou no servidor.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer proxied = event.getPlayer();
        CompletableFuture.runAsync(() -> handlePlayerDisconnect(proxied));
    }

    private void handlePlayerDisconnect(ProxiedPlayer proxied) {
        Account account = controller.of(proxied.getUniqueId());

        if (account == null) return;

        account.setOnline(false);

        if (account.getRoute().isValidArcade()) {
            ArcadeRouteContext route = account.getArcadeRoute();

            if (route.isValid() && route.getArcade().getServer().hasFlag(ServerFlag.RECONNECT)) {
                Savage.getReconnectData().save(new Reconnect(proxied.getUniqueId(), route));
            }
        }

        if (account.isUsingFake()) {
            account.setTag(account.getDefaultTag());
            account.resetFake();
            new AccountSkinResetMessage(account).send();
        }

        account.setRoute(RouteContext.bungee());

        account.updateLastLogin();
        account.notifyFriends("§b" + account.getNickname() + "§e desconectou.");

        Savage.getAccountData().startExpiration(account);
    }
}
