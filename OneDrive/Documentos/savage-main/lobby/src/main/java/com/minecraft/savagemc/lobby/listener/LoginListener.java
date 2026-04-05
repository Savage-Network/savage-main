package com.minecraft.savagemc.lobby.listener;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.reconnect.Reconnect;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.lobby.Lobby;
import com.minecraft.savagemc.lobby.user.User;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class LoginListener implements Listener {

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
            return;
        }

        new User(event.getUniqueId());
    }

    @EventHandler
    public void spawn(PlayerSpawnLocationEvent event) {
        Account account = Savage.getAccountController().of(event.getPlayer().getUniqueId());

        Location spawn = Lobby.getLoader().getArchitect().getLocation("spawn");

        if (spawn != null && account.isVIP())
            spawn = spawn.clone().add(0, 3.5, 0);

        event.setSpawnLocation(spawn != null ? spawn : event.getPlayer().getWorld().getSpawnLocation());
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) {
            player.kickPlayer(Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
            return;
        }

        user.handle();
        
        // Adicionar speed 2 sem mostrar o efeito
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED, 
                Integer.MAX_VALUE, 
                1, // Level 2 (0 = level 1, 1 = level 2)
                true, // Ambient (sem partículas visíveis)
                false // Sem mostrar ícone
        ));

        /* Tem reconexão */
        Reconnect reconnect = Savage.getReconnectData().of(player.getUniqueId());

        if (reconnect != null) {
            ArcadeRouteContext route = reconnect.getRoute();

            player.sendMessage(new String[]{
                    "",
                    "§c§lVocê desconectou de uma partida de " + route.getArcade().getFullName(),
                    "§c§lem andamento. Use /reconectar para voltar a jogar.",
                    ""
            });

            player.playSound(Sound.NOTE_BASS);
        }

        Account account = user.getAccount();

        account.title("§6§l" + Savage.getServerType().getName().toUpperCase(), "§eBem-vindo(a)!");
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
        Lobby.getLoader().getArchitect().quit(player);
    }
}


