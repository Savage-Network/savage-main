package com.minecraft.savagemc.auth.listener;

import com.minecraft.savagemc.auth.user.User;
import com.minecraft.savagemc.core.api.skin.Skin;
import com.minecraft.savagemc.core.bukkit.api.protocol.ProtocolHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UserListener implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new User(player.getUniqueId()).handle();

        ProtocolHandler.changePlayerSkin(player, Skin.unknown());
    }
}
