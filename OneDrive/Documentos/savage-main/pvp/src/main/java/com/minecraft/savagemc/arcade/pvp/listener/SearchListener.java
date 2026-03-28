package com.minecraft.savagemc.arcade.pvp.listener;

import com.minecraft.savagemc.arcade.pvp.arcade.Arcade;
import com.minecraft.savagemc.arcade.pvp.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.pvp.user.User;
import com.minecraft.savagemc.arcade.pvp.user.factory.UserFactory;
import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.route.RouteContext;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.backend.data.list.server.RouteData;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.member.list.pvp.PvPMember;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.UUID;

public class SearchListener implements Listener {

    private final RouteData routeData;

    public SearchListener() {
        this.routeData = Savage.getRouteData();
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) {

            UUID id = event.getUniqueId();

            // Buscando dados da rota
            RouteContext route = routeData.read(id);

            if (route == null || !route.isValidArcade()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.INVALID_ROUTE_MESSAGE);
                return;
            }

            routeData.delete(id);

            ArcadeRouteContext arcadeRoute = route.getArcade();

            Arcade arcade = (Arcade) BukkitCore.getManager().getArcade().read(arcadeRoute.getArcade());

            if (arcade == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ARCADE_NOT_FOUND_MESSAGE);
                return;
            }

            Arena arena = arcade.findBestArena(id, arcadeRoute);

            if (arena == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.NO_ROOM_AVAILABLE_MESSAGE);
                return;
            }

            // Carregando dados da conta Arcade
            PvPMember member = Savage.getPvpData().of(id, true);

            if (member == null) {
                member = Savage.getPvpData().save(new PvPMember(id, event.getName()));

                if (member == null) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
                    return;
                }
            }

            Savage.getMemberController().save(member);
            Savage.getPvpData().cancelExpiration(id);

            // Instanciando usuário customizado
            UserFactory.create(arcade.getCategory(), member, arena, arcadeRoute.getJoin());
        }
    }

    @EventHandler
    public void login(PlayerLoginEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        if (user == null || user.getArena() == null)
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE + " (Invalid user/arena)");
    }

    @EventHandler
    public void spawn(PlayerSpawnLocationEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        event.setSpawnLocation(user.getArena().getLocation("spawn"));
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user != null)
            user.handle();
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        exit(event.getPlayer());
    }

    @EventHandler
    public void kick(PlayerKickEvent event) {
        exit(event.getPlayer());
    }

    protected void exit(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user != null)
            user.getArena().quit(player);

        Savage.getPvpData().startExpiration(player.getUniqueId());
        Savage.getMemberController().remove(player.getUniqueId(), PvPMember.class);
    }
}
