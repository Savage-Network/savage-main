package com.minecraft.savagemc.core.bukkit.service.redis.listener;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.Rank;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.api.skin.Skin;
import com.minecraft.savagemc.core.api.skin.library.SkinLibrary;
import com.minecraft.savagemc.core.arcade.ArcadeHolder;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.Room;
import com.minecraft.savagemc.core.arcade.room.event.ArenaSearchedEvent;
import com.minecraft.savagemc.core.arcade.room.map.Map;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.room.type.Type;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.*;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.RoomInfoMessage;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.RoomRequestMessage;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.arcade.ArenaPartyWarpMessage;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.arcade.ArenaStatsMessage;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.option.ServerOptions;
import com.minecraft.savagemc.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.savagemc.core.bukkit.api.vanish.Vanish;
import com.minecraft.savagemc.core.bukkit.event.type.account.*;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerArenaWarpEvent;
import com.minecraft.savagemc.core.bukkit.event.type.server.ServerRedisMessageEvent;
import com.minecraft.savagemc.core.bukkit.manager.list.TagManager;
import com.minecraft.savagemc.core.bukkit.menu.account.friend.*;
import com.minecraft.savagemc.core.bukkit.menu.account.history.HistoryMenu;
import com.minecraft.savagemc.core.bukkit.menu.server.party.PartyMenu;
import com.minecraft.savagemc.core.server.Server;
import com.minecraft.savagemc.core.util.list.ReflectionUtil;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class RedisListener implements Listener {

    @EventHandler
    public void onServerRedisMessage(ServerRedisMessageEvent event) {
        String channel = event.getChannel();

        JsonObject json = event.getJson();

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

            case Constant.REDIS_ARENA_SEARCH_CHANNEL: {
                ArenaSearchMessage message = Savage.GSON.fromJson(json, ArenaSearchMessage.class);

                if (message == null) return;

                ArcadeRouteContext route = message.getRoute();

                if (!route.isValid()) {
                    Savage.getLogger().warning("[AS] Não foi possível buscar a arena para " + message.getSender() + ". Rota inválida!");
                    return;
                }

                ArcadeCategory category = route.getArcade();

                Account account = Savage.getAccountData().of(message.getSender());

                if (account == null) {
                    Savage.getLogger().warning("[AS] Não foi possível buscar a arena para " + message.getSender() + ". Jogador não encontrado!");
                    return;
                }

                List<Server> serverList = Savage.getServerData().getServerList(server -> server.getType().equals(category.getServer()));

                Server server = serverList.stream().findFirst().orElse(null);

                if (server == null) {
                    account.sendGlobal(String.format(Constant.SERVER_NOT_FOUND_MESSAGE, category.getServer().getName()));
                    return;
                }

                if (!server.getType().equals(Savage.getServerType())) return;

                ArcadeHolder arcade = BukkitCore.getManager().getArcade().read(route.getArcade());

                if (arcade == null) {
                    Savage.getLogger().warning("[AS] Não foi possível buscar a arena para " + account.getNickname() + ". Jogo não encontrado!");

                    new ArenaStatsMessage(message.getSender(), ArenaStatsMessage.RequestStatus.NOT_FOUND).send();
                    return;
                }

                Room arena = arcade.findBestArena(account.getId(), route);

                new ArenaStatsMessage(message.getSender(), ArenaStatsMessage.RequestStatus.SEARCHING).send();

                if (arena == null) {
                    Savage.getLogger().warning("[AS] Não foi possível buscar a arena para " + account.getNickname() + ". Arena não encontrada!");

                    new ArenaStatsMessage(message.getSender(), ArenaStatsMessage.RequestStatus.NOT_FOUND).send();
                    return;
                }

                ArenaStatsMessage stats = new ArenaStatsMessage(message.getSender(), ArenaStatsMessage.RequestStatus.SENDING);

                stats.setIdentifier(arena.getIdentifier());
                stats.send();

                new ArenaSearchedEvent(account, arena, route).callEvent();

                break;
            }

            case Constant.REDIS_ARENA_PARTY_LIST_CHANNEL: {
                AccountPartyListMessage message = Savage.GSON.fromJson(json, AccountPartyListMessage.class);

                if (message == null) return;

                Player player = Bukkit.getPlayer(message.getSender());

                if (player != null)
                    new PartyMenu(player).handle();
                break;
            }

            case Constant.REDIS_ARENA_PARTY_WARP_CHANNEL: {
                ArenaPartyWarpMessage message = Savage.GSON.fromJson(json, ArenaPartyWarpMessage.class);

                if (message == null) return;

                Account receiver = message.getReceiver();

                ArcadeRouteContext route = message.getRoute();

                if (!receiver.inServer(route.getArcade().getServer())) {
                    Savage.getLogger().warning("Servidor não é igual.");
                    return;
                }

                ArcadeHolder arcade = BukkitCore.getManager().getArcade().read(route.getArcade());

                if (arcade == null) {
                    Savage.getLogger().warning("Não foi possível encontrar o jogo solicitado.");
                    return;
                }

                Room arena = arcade.findBestArena(receiver.getId(), route);

                if (arena == null) {
                    Savage.getLogger().info("Não foi possível encontrar a arena desejada.");
                    return;
                }

                new PlayerArenaWarpEvent(receiver.player(), arena).call();
                break;
            }

            case Constant.REDIS_ACCOUNT_VANISH_CHANNEL: {
                AccountVanishMessage message = Savage.GSON.fromJson(json, AccountVanishMessage.class);

                if (message == null) return;

                Player player = Bukkit.getPlayer(message.getId());

                if (player != null)
                    Savage.getPlatform().runSync(() -> Vanish.handle(player));

                break;
            }

            case Constant.REDIS_ACCOUNT_RANK_UPDATE_CHANNEL: {
                UUID id = UUID.fromString(json.get("id").getAsString());

                Rank rank = Savage.GSON.fromJson(json.get("rank").getAsString(), Rank.class);

                Account account = Savage.getAccountData().of(id);

                if (account == null) return;

                new AccountRankUpdateEvent(account, rank.getType()).call();
                break;
            }

            case Constant.REDIS_ACCOUNT_FRIEND_MENU_CHANNEL: {
                AccountFriendMessage message = Savage.GSON.fromJson(json, AccountFriendMessage.class);

                if (message == null) return;

                Player sender = Bukkit.getPlayer(message.getSender());

                if (sender == null) return;

                AccountFriendMessage.FriendMenuType type = message.getType();

                switch (type) {
                    case INFO: {
                        new FriendInfoMenu(sender, message.getFriend(), null).handle();
                        break;
                    }
                    case LIST: {
                        new FriendMenu(sender, null).handle();
                        break;
                    }
                }

                break;
            }

            case Constant.REDIS_ACCOUNT_CLAN_CHANGE_CHANNEL: {
                AccountClanChangeMessage message = Savage.GSON.fromJson(json, AccountClanChangeMessage.class);

                if (message == null) return;

                Account account = message.getAccount();

                if (account == null || !account.isOnline()) return;

                new AccountClanChangeEvent(account).call();
                break;
            }

            case Constant.REDIS_ACCOUNT_PUNISHMENTS_CHANNEL: {
                AccountPunishmentsMessage message = Savage.GSON.fromJson(json, AccountPunishmentsMessage.class);

                if (message == null) return;

                Player viewer = Bukkit.getPlayer(message.getViewer());

                if (viewer == null) return;

                Account target = Savage.getAccountData().of(message.getTarget());

                if (target == null) {
                    viewer.sendMessage("§cNão foi possível encontrar os dados do alvo.");
                    return;
                }

                new HistoryMenu(viewer, target, message.getCategory()).handle();
                break;
            }

            case Constant.REDIS_ACCOUNT_NICK_CHANGE_CHANNEL: {
                AccountNickChangeMessage message = Savage.GSON.fromJson(json, AccountNickChangeMessage.class);

                if (message == null) return;

                Account account = message.getAccount();

                Player player = account.player();
                if (player == null) return;

                String nickname = message.getNickname();

                boolean itsNotReset = !account.getName().equalsIgnoreCase(nickname);

                Skin skin = itsNotReset ? SkinLibrary.random() : account.getDefaultProfileSkin();

                account.setSkin(skin);

                /* Enviando Protocolos */
                if (ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE)
                    ProtocolHandler.changePlayerSkin(player, skin, false);

                if (ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE)
                    account.setTag(itsNotReset ? Tag.MEMBER : account.getDefaultTag());

                ProtocolHandler.changePlayerName(player, nickname);

                Savage.getPlatform().runSync(() -> TagManager.updateTag(account), 10);

                account.send(itsNotReset ? "§aAlteração concluída! O seu nome foi alterado." : "§eVocê voltou para o seu nome original.");
                account.sound(Sound.NOTE_PLING);

                break;
            }

            case Constant.REDIS_ACCOUNT_SKIN_RESET_CHANNEL: {
                AccountSkinResetMessage message = Savage.GSON.fromJson(json, AccountSkinResetMessage.class);

                if (message == null) return;

                Account account = message.getAccount();

                if (account == null) return;

                account.setDefaultProfileSkin();

                break;
            }

            // Gerar nova sala no Arcade
            case Constant.REDIS_ROOM_REQUEST_CHANNEL: {
                RoomRequestMessage message = Savage.GSON.fromJson(json, RoomRequestMessage.class);

                if (message == null) return;

                ArcadeCategory arcade = message.getArcade();

                // Servidor de jogos não é o mesmo que foi solicitado
                if (!arcade.getServer().equals(Savage.getServerType())) return;

                ArcadeHolder game = BukkitCore.getManager().getArcade().read(arcade);

                if (game == null) {
                    new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.GAME_NOT_FOUND, null, null, null, null, 0).send();
                    return;
                }

                Slot slot = message.getSlot();

                Map map = game.getMap(message.getMapId());

                if (map == null) {
                    new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.UNKNOWN_ERROR, null, null, null, null, 0).send();
                    return;
                }

                Type mode = message.getType();

                Room room = game.setupRoom(map, slot, mode);

                if (room == null) {
                    new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.UNKNOWN_ERROR, null, null, null, null, 0).send();
                    return;
                }

                new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.DONE, room.getIdentifier(), arcade, slot, mode, map.getId()).send();

                Savage.getPlatform().runSync(() -> {
                    if (room.getWorld() == null) {
                        new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.UNKNOWN_ERROR, null, null, null, null, 0).send();
                        return;
                    }

                    room.getProperties().putAll(message.getProperties());

                    // Configurando sala
                    room.loadConfigurations();

                    new RoomInfoMessage(message.getRequester(), RoomInfoMessage.InfoType.SENDING_TO, room.getIdentifier(), arcade, slot, mode, map.getId()).send();

                }, 20L);

                break;
            }
            
            case Constant.REDIS_REPORT_CHANNEL: {
                com.minecraft.savagemc.core.backend.database.redis.message.types.report.ReportReplayMessage message = 
                    Savage.GSON.fromJson(json, com.minecraft.savagemc.core.backend.database.redis.message.types.report.ReportReplayMessage.class);

                if (message == null) return;

                // Atualizar o report com o replayId
                com.minecraft.savagemc.core.api.report.Report report = Savage.getReportData().of(message.getReportedPlayer());
                if (report != null && message.getReplayId() != null) {
                    report.setReplayId(message.getReplayId());
                }
                
                break;
            }
        }
    }
}
