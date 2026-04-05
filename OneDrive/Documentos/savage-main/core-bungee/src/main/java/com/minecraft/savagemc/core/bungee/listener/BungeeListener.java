package com.minecraft.savagemc.core.bungee.listener;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bungee.event.list.update.UpdateEvent;
import com.minecraft.savagemc.core.server.Server;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.list.StringUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BungeeListener implements Listener {

    private final List<String> descriptions = Arrays.asList(
            "§e§lVENHA JOGAR §b§lBEDWARS COMPETITIVO§e§l!",
            "§a§lCONFIRA AS ATUALIZAÇÕES!",
            "§c§lNOVIDADES: §e§lACESSE E CONFIRA!"
    );

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        CompletableFuture.runAsync(() -> {
            /* Atualizando contador de jogadores */
            Savage.getServerData().updatePlayerCounter(ProxyServer.getInstance().getOnlineCount());

            /* Verificando contas */
            Savage.getAccountController().list().forEach(Account::checkInfos);
        });
    }

    @EventHandler
    public void motd(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();

        String description = descriptions.get(Savage.RANDOM.nextInt(descriptions.size()));

        Server server = Savage.getServerData().local();

        if (server != null && server.getWhitelist().isEnabled())
            description = "§c§lEM MANUTENÇÃO!";

        ping.setDescriptionComponent(new TextComponent(
                // Header
                StringUtil.StringHelper.makeCenteredMotd(Constant.SERVER_TITLE + " §7[1.7-1.21]")
                        // Footer
                        + "\n" + StringUtil.StringHelper.makeCenteredMotd(description)
        ));

        event.setResponse(ping);
    }

    @EventHandler
    public void connect(ServerConnectEvent event) {
        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            ProxiedPlayer proxied = event.getPlayer();

            Account account = Savage.getAccountController().of(proxied.getUniqueId());

            if (account == null) {
                event.setCancelled(true);

                proxied.disconnect(TextComponent.fromLegacyText(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
                return;
            }

            ServerType category = proxied.getPendingConnection().isOnlineMode() ? ServerType.HUB : ServerType.AUTH;

            Server server = Savage.getServerData().of(category);

            if (server == null || server.getProxyServer() == null) {
                event.setCancelled(true);

                proxied.disconnect(TextComponent.fromLegacyText(String.format(Constant.SERVER_NOT_FOUND_MESSAGE, category.getName())));
                return;
            }

            event.setTarget(server.getProxyServer());
        }
    }

    @EventHandler
    public void onServerKick(ServerKickEvent event) {
        ProxiedPlayer player = event.getPlayer();
        
        // Mensagem personalizada quando servidor fecha
        String kickMessage = "§2§lSAVAGE\n\n" +
                            "§cOs servidores foram fechados no momento.\n" +
                            "§eTente entrar novamente mais tarde!\n\n" +
                            "§7Para dúvidas, consulte: §bwww.savagemc.net";
        
        event.setCancelServer(null);
        
        // Tentar conectar ao lobby
        Server lobbyServer = Savage.getServerData().of(ServerType.HUB);
        
        if (lobbyServer != null && lobbyServer.getProxyServer() != null) {
            ServerInfo lobby = lobbyServer.getProxyServer();
            
            // Se o servidor que kickou não é o lobby, tentar conectar ao lobby
            if (event.getKickedFrom() != null && !event.getKickedFrom().equals(lobby)) {
                event.setCancelled(true);
                event.setCancelServer(lobby);
                
                player.sendMessage(TextComponent.fromLegacyText(
                    "§cServidor desconectado!\n" +
                    "§eVocê foi redirecionado ao lobby."
                ));
            } else {
                // Se foi kickado do lobby, desconectar com mensagem
                event.setCancelled(false);
                event.setKickReasonComponent(TextComponent.fromLegacyText(kickMessage));
            }
        } else {
            // Se não há lobby disponível, desconectar com mensagem
            event.setCancelled(false);
            event.setKickReasonComponent(TextComponent.fromLegacyText(kickMessage));
        }
    }
}
