package com.minecraft.savagemc.arcade.bedwars.listener;

import com.minecraft.savagemc.arcade.bedwars.arcade.Arcade;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.route.RouteContext;
import com.minecraft.savagemc.core.api.reconnect.Reconnect;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.backend.data.list.server.RouteData;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.UUID;

public class SearchListener implements Listener {

    private final RouteData routeData;

    public SearchListener() {
        this.routeData = Savage.getRouteData();
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        Savage.message("=== ASYNC PRE LOGIN INICIADO ===");
        Savage.message("Jogador: " + event.getName());
        Savage.message("UUID: " + event.getUniqueId());
        Savage.message("Resultado: " + event.getLoginResult());
        
        if (event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) {

            UUID id = event.getUniqueId();

            // Buscando dados da rota
            RouteContext route = routeData.read(id);

            Savage.message("=== DEBUG ROTA ===",
                    "Jogador: " + event.getName() + " (" + id + ")",
                    "Rota encontrada: " + (route != null),
                    "Rota válida: " + (route != null ? route.isValidArcade() : "N/A"),
                    "Rota detalhes: " + (route != null ? route.toString() : "NULL"));

            if (route == null || !route.isValidArcade()) {
                routeData.delete(id);

                Savage.message("Rota: " + route,
                        "Válida: " + (route != null ? route.isValidArcade() : "NÃO ENCONTRADO"));

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

            Arena arena = (Arena) arcade.getRoom(arcadeRoute);

            if (arena == null) {
                Savage.message("Arena não encontrada!");
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.NO_ROOM_AVAILABLE_MESSAGE);
                return;
            }

            Savage.message("Arena encontrada: " + arena.getIdentifier());
            Savage.message("Carregando dados do BedMember...");

            // Carregando dados da conta Arcade
            BedMember member = Savage.getBedWarsData().of(id, true);

            Savage.message("BedMember carregado: " + (member != null));

            if (member == null) {
                Savage.message("Criando novo BedMember...");
                member = Savage.getBedWarsData().save(new BedMember(id, event.getName()));

                if (member == null) {
                    Savage.message("Falha ao criar BedMember!");
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE + " (Ex0002)");
                    return;
                }
                Savage.message("BedMember criado com sucesso!");
            }

            Savage.message("Salvando member no controller...");
            Savage.getMemberController().save(member);
            
            Savage.message("Cancelando expiração...");
            Savage.getBedWarsData().cancelExpiration(id);

            Savage.message("Criando usuário...");
            // Criando usuário
            User user = new User(member, arena, arcadeRoute.getJoin());

            Savage.message("Verificando reconnect...");
            if (user.getAccount().hasReconnect()) {
                Reconnect reconnect = Savage.getReconnectData().of(id);

                if (reconnect != null)
                    user.getContext().setReconnect(reconnect);
            }
            
            Savage.message("=== LOGIN PERMITIDO ===");
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        if (user == null)
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE + " (Ex0003)");
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

        Savage.getBedWarsData().startExpiration(player.getUniqueId());
    }
}
