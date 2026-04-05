package com.minecraft.savagemc.core.bukkit.listener.server;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.Rank;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.rank.type.role.RankRole;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.arcade.ArcadeHolder;
import com.minecraft.savagemc.core.arcade.payload.ArcadePayload;
import com.minecraft.savagemc.core.arcade.room.Room;
import com.minecraft.savagemc.core.arcade.room.event.ArenaSearchedEvent;
import com.minecraft.savagemc.core.arcade.room.payload.RoomPayload;
import com.minecraft.savagemc.core.arcade.route.join.Join;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.api.item.book.BookItem;
import com.minecraft.savagemc.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.savagemc.core.bukkit.api.option.ServerOptions;
import com.minecraft.savagemc.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.savagemc.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.savagemc.core.bukkit.api.vanish.Vanish;
import com.minecraft.savagemc.core.bukkit.event.type.account.AccountClanChangeEvent;
import com.minecraft.savagemc.core.bukkit.event.type.account.AccountPrefixTypeEvent;
import com.minecraft.savagemc.core.bukkit.event.type.account.AccountRankUpdateEvent;
import com.minecraft.savagemc.core.bukkit.event.type.account.AccountRefreshEvent;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerCooldownEndEvent;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerSidebarUpdateEvent;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.savagemc.core.bukkit.manager.Manager;
import com.minecraft.savagemc.core.bukkit.manager.list.PermissionManager;
import com.minecraft.savagemc.core.bukkit.manager.list.TagManager;
import com.minecraft.savagemc.core.bukkit.user.UserModel;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.TimeUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.*;

public class UpdateListener implements Listener {

    private final Manager manager;

    public UpdateListener() {
        this.manager = BukkitCore.getManager();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSearched(ArenaSearchedEvent event) {
        Account account = event.getSender();

        Room arena = event.getArena();

        Join join = event.getRoute().getJoin();

        /* Executar ação de rota para sender */

        account.redirect(arena, join);
    }

    @EventHandler
    public void onAccountClan(AccountClanChangeEvent event) {
        Account account = event.getAccount();

        if (account != null && account.isOnline())
            TagManager.updateTag(account);
    }

    @EventHandler
    public void onServerUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.TICK)) {
            if (event.getTicks() % 6000 == 0) {
                System.gc();

                Savage.message("Limpeza de memória efetuada.");
            }

            /* Cooldown */
            for (UUID uuid : manager.getCooldown().getCooldownMap().keySet()) {
                Player player = Bukkit.getPlayer(uuid);

                if (player != null) {
                    List<Cooldown> list = manager.getCooldown().getCooldownMap().get(uuid);
                    Iterator<Cooldown> iterator = list.iterator();

                    /* Get Cooldown */
                    Cooldown search = null;

                    while (iterator.hasNext()) {
                        Cooldown cooldown = iterator.next();

                        if (!cooldown.expired()) {
                            search = cooldown;
                            continue;
                        }

                        iterator.remove();

                        new PlayerCooldownEndEvent(player, cooldown).call();
                    }

                    /* Display Cooldown */
                    if (search != null && search.isShowBar()) {
                        manager.getCooldown().display(player, search);
                    } else if (list.isEmpty()) {
                        ProtocolHandler.sendBar(player, " ");
                        manager.getCooldown().getCooldownMap().remove(player.getUniqueId());
                    }
                }
            }
        }

        if (event.isType(UpdateType.SECOND)) {
            // Barra de modo admin
            Vanish.getList().forEach(vanish -> {
                Player player = vanish.getPlayer();

                if (player != null)
                    ProtocolHandler.sendBar(player, "§fVocê está no modo §dVANISH");
            });

            // Atualizar contador de jogadores
            Savage.getServerData().updatePlayerCounter(Bukkit.getOnlinePlayers().size());

            for (Sidebar sidebar : manager.getSidebar().list()) {
                if (Savage.getServerData().local().isArcade() || !sidebar.hasRow("players"))
                    continue;

                sidebar.updateRow("players", "Jogadores: §a" + Util.formatNumber(Savage.getServerData().getOnlinePlayers()));
            }

            // Atualizador de Jogos
            for (ArcadeHolder arcade : manager.getArcade().getArcades()) {
                if (arcade == null || arcade.getRooms().isEmpty()) continue;

                ArcadePayload arcadePayload = Savage.getArcadeData().read(arcade.getCategory());

                if (arcadePayload == null) continue;

                int onlinePlayers = arcade.getRooms().stream().mapToInt(room -> room.getPlayers().size()).sum();

                arcadePayload.setOnlinePlayers(onlinePlayers);

                List<RoomPayload> payloadList = new ArrayList<>(arcadePayload.getRooms());

                Set<Room> rooms = new HashSet<>(arcade.getRooms());

                /* Remover salas não existentes */
                payloadList.removeIf(payload -> payload == null
                        || rooms.stream().noneMatch(room -> payload.getIdentifier().equalsIgnoreCase(room.getIdentifier())));

                arcadePayload.setRooms(payloadList);

                /* Criar, ou atualizar as salas */
                for (Room room : rooms) {
                    if (room == null) continue;

                    RoomPayload payload = payloadList.stream()
                            .filter(search -> search.getIdentifier().equalsIgnoreCase(room.getIdentifier()))
                            .findFirst()
                            .orElse(null);

                    // Criar propriedade de sala
                    if (payload == null)
                        payloadList.add(new RoomPayload(room));
                    else {
                        payload.setOnlinePlayers(room.getTotalPlayers());

                        // Atualizar propriedade de sala
                        if (room.getPhase() != payload.getPhase())
                            payload.setPhase(room.getPhase());

                        int indexOf = payloadList.indexOf(payload);

                        if (indexOf >= 0)
                            payloadList.set(indexOf, payload);
                    }
                }

                Savage.getArcadeData().update(arcadePayload);
            }
        }
    }

    @EventHandler
    public void cooldownEnd(PlayerCooldownEndEvent event) {
        Player player = event.getPlayer();

        player.setExp(0);
        player.setLevel(0);
    }

    @EventHandler
    public void onSidebarUpdate(PlayerSidebarUpdateEvent event) {
        Player player = event.getPlayer();

        if (player == null)
            return;

        Sidebar sidebar = manager.getSidebar().of(player);

        if (sidebar == null)
            return;

        sidebar.updateRow(event.getRow());
    }

    @EventHandler
    public void refresh(AccountRefreshEvent event) {
        Account account = event.getAccount();

        if (account == null) return;

        if (Savage.getPlatform().isOnlinePlayer(account.getId())) {
            Savage.getAccountController().save(account);

            UserModel user = UserModel.of(account.getId());

            if (user == null) return;

            user.setAccount(account);

            if (ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE) {
                if (account.getTag() != user.getTag())
                    user.setTag(account.getTag());
            }

            // Atualiza o tab quando context muda (clan tag, tag, medal, etc.)
            String fieldName = event.getField() != null ? event.getField().getName() : "";
            if (fieldName.equals("context") || fieldName.equals("tag")) {
                Player player = account.player();
                if (player != null)
                    Savage.getPlatform().runSync(() -> TagManager.updateTag(account), 2);
            }
        }
    }

    @EventHandler
    public void onRankUpdate(AccountRankUpdateEvent event) {
        Account account = event.getAccount();
        RankType rankType = event.getRank();
        
        // Criar objeto Rank a partir do RankType
        Rank rank = Rank.builder()
                .type(rankType)
                .build();

        Tag tag = Tag.of(rank.getType());

        if (ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE)
            account.setTag(tag);

        Player player = account.player();

        if (player != null) {
            if (rankType.getRole().equals(RankRole.VIP)) {
                TextComponent message = new TextComponent("Sua compra foi processada!\n\n");

                message.addExtra("Você adquiriu o rank: " + rank.getColor() + "Rank " + rank.getName() + "\n\n");
                message.addExtra("Se você estiver com algum problema, contate-nos em:\n");

                TextComponent discordComponent = new TextComponent("§9§lDISCORD");

                discordComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para acessar nosso Discord!")));
                discordComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www." + Constant.SERVER_DISCORD));

                message.addExtra(discordComponent);

                ProtocolHandler.openBook(player, new BookItem().pageComponents(message));
            }

            PermissionManager.loadPermissions(player);
            Savage.getPlatform().runSync(() -> TagManager.updateTag(account, tag), 10);

            account.send(java.util.Arrays.asList(
                    rankType.getRole().equals(RankRole.VIP) ?
                    "§aA sua compra foi processada! O rank " + rank.getColoredName() + "§a foi adicionado em sua conta com duração "
                            + (rank.isPermanent() ? "vitalícia" : "de " + TimeUtil.formatTime(rank.getExpiresAt(), TimeUtil.TimeFormat.SHORT)) + "."
                    : "§aO rank " + rank.getColoredName() + "§a foi adicionado a sua conta."
            ));
            account.sound(Sound.ORB_PICKUP);
        }
    }

    @EventHandler
    public void onPrefixType(AccountPrefixTypeEvent event) {
        Account account = event.getAccount();

        Tag tag = UserModel.of(account.getId()).getTag();

        TagManager.updateTag(account, tag);
    }
}
