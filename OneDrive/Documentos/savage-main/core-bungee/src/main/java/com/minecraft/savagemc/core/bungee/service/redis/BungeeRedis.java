package com.minecraft.savagemc.core.bungee.service.redis;

import com.minecraft.savagemc.core.api.party.Party;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.arcade.route.join.Join;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountExecuteCommandMessage;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountGlobalMessage;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.RoomInfoMessage;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.arcade.ArenaStatsMessage;
import com.minecraft.savagemc.core.bungee.BungeeCore;
import com.minecraft.savagemc.core.bungee.event.list.account.AccountRefreshEvent;
import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.route.RouteContext;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountUpdateMessage;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.RouteSearchMessage;
import com.minecraft.savagemc.core.server.Server;
import com.minecraft.savagemc.core.server.payload.ServerPayload;
import com.minecraft.savagemc.core.server.payload.whitelist.ServerWhitelist;
import com.minecraft.savagemc.core.util.list.ReflectionUtil;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class BungeeRedis extends JedisPubSub {

    @Override
    public void onMessage(String channel, String data) {
        JsonObject json = Savage.PARSER.parse(data).getAsJsonObject();

        if (json == null) {
            Savage.getLogger().severe("Não foi possível recolher os dados do canal " + channel + ".");
            return;
        }

        switch (channel.toLowerCase()) {
            case Constant.REDIS_ACCOUNT_UPDATE_CHANNEL: {
                AccountUpdateMessage message = Savage.GSON.fromJson(json, AccountUpdateMessage.class);

                if (message == null) return;

                Account account = message.getAccount();

                if (account == null) return;

                try {
                    Field field = ReflectionUtil.getField(Account.class, message.getField());

                    Object old = field.get(account), value = Savage.GSON.fromJson(message.getValue(), field.getGenericType());

                    field.set(account, value);

                    new AccountRefreshEvent(account, field, old, value).call();
                } catch (Exception e) {
                    account.send("§cNão foi possível atualizar as suas informações!");

                    Savage.getLogger().log(Level.WARNING, "Não foi possível atualizar as informações de " + account.getName(), e);
                }
                break;
            }

            // Recebendo informações de solicitação das salas
            case Constant.REDIS_ROOM_INFO_CHANNEL: {
                RoomInfoMessage message = Savage.GSON.fromJson(json, RoomInfoMessage.class);

                if (message == null) return;

                ProxiedPlayer player = BungeeCore.getInstance().getProxy().getPlayer(message.getRequester());

                if (player == null) return;

                Account account = Savage.getAccountController().of(player.getUniqueId());

                if (account == null) return;

                RoomInfoMessage.InfoType info = message.getInfo();

                String text = info.getMessage();

                if (!info.equals(RoomInfoMessage.InfoType.SENDING_TO))
                    account.send(text);

                if (info.equals(RoomInfoMessage.InfoType.SENDING_TO) && message.isValid()) {
                    ArcadeRouteContext route = ArcadeRouteContext.builder()
                            .arcade(message.getArcade())
                            .slot(message.getSlot())
                            .type(message.getMode())
                            .mapId(message.getMapId())
                            .join(Join.PLAYER)
                            .build();

                    Server server = Savage.getServerData().of(message.getArcade().getServer());

                    route.setServerId(server != null ? server.getId() : 0);

                    Party party = account.getParty();

                    if (party != null) {
                        if (party.isAuthor(account.getId()))
                            route.setLink(party.getMembersId());
                        else {
                            account.send("§cApenas o dono da party pode procurar salas.");
                            return;
                        }
                    }

                    new ArenaSearchMessage(account.getId(), route).send();
                }

                break;
            }

            case Constant.REDIS_ACCOUNT_GLOBAL_MESSAGE_CHANNEL: {
                AccountGlobalMessage message = Savage.GSON.fromJson(json, AccountGlobalMessage.class);

                if (message == null) return;

                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(message.getId());

                if (player != null)
                    for (String text : message.getMessage())
                        player.sendMessage(new TextComponent(text));

                break;
            }

            case Constant.REDIS_ROUTE_SEARCH_CHANNEL: {
                RouteSearchMessage message = Savage.GSON.fromJson(json, RouteSearchMessage.class);

                if (message == null) {
                    Savage.getLogger().info("Mensagem nula!");
                    return;
                }

                Account account = message.getAccount();
                RouteContext route = message.getRoute();

                if (account == null || account.proxiedPlayer() == null) {
                    Savage.getLogger().log(Level.WARNING, "Conexão não efetuada. O usuário não foi encontrado.");
                    return;
                }

                ProxiedPlayer player = account.proxiedPlayer();

                Server server = Savage.getServerData().of(route.getServerType());

                if (server == null || server.getProxyServer() == null) {
                    account.send(String.format(Constant.SERVER_NOT_FOUND_MESSAGE, route.getServerType().getName()));

                    Savage.getLogger().log(Level.WARNING, "Conexão não efetuada. O servidor requisitado não foi encontrado.");
                    return;
                }

                ServerPayload payload = server.getPayload();

                ServerWhitelist whitelist = payload.getWhitelist();

                if (whitelist.isEnabled() && !(account.isStaffer() || whitelist.hasRank(account.getRank().getType()) || whitelist.hasPlayer(player.getName()))) {
                    account.send("§cA sala está aberta somente para jogadores autorizados.");
                    return;
                }

                player.connect(server.getProxyServer());

                Savage.getLogger().info("[SR] " + account.getNickname() + " acessou o servidor " + server.getName() + ".");
                break;
            }

            case Constant.REDIS_ARENA_STATS_CHANNEL: {
                ArenaStatsMessage message = Savage.GSON.fromJson(json, ArenaStatsMessage.class);

                if (message == null) return;

                Account account = Savage.getAccountController().of(message.getSender());

                if (account == null) return;

                ArenaStatsMessage.RequestStatus status = message.getStatus();

                if (status.equals(ArenaStatsMessage.RequestStatus.SENDING) && message.getIdentifier() != null)
                    account.send(String.format(status.getName(), message.getIdentifier()));
                else
                    account.send(status.getName());

                break;
            }

            // Executar comandos
            case Constant.REDIS_ACCOUNT_EXECUTE_COMMAND_CHANNEL: {
                AccountExecuteCommandMessage message = Savage.GSON.fromJson(json, AccountExecuteCommandMessage.class);

                if (message == null) return;

                ProxiedPlayer sender = ProxyServer.getInstance().getPlayer(message.getSender());

                if (sender == null) return;

                String command = message.getCommand().replace("/", "");

                ProxyServer.getInstance().getPluginManager().dispatchCommand(sender, command);
                break;
            }
        }
    }
}

