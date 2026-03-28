package com.minecraft.savagemc.core.bungee.listener;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.List;

public class ChatListener implements Listener {

    @EventHandler
    public void chat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        String message = event.getMessage();

        Account account = Savage.getAccountController().of(player.getUniqueId());

        if (account == null) return;

        if (!event.isCommand()) {
            if (account.getToggle().isStaffChat() && account.isStaffer()) {
                Tag tag = account.getDefaultTag();

                Savage.getAccountController().send(RankType.TRIAL, Constant.STAFF_CHAT_PREFIX + tag.getPrefix() + account.getName() + ": §f"
                        + Util.color(message));

                event.setCancelled(true);
            }

        } else {
            if (!account.isPremium() && account.inServer(ServerType.AUTH) && !isAuthCommand(message))
                event.setCancelled(true);
        }
    }

    protected boolean isAuthCommand(String command) {
        List<String> list = Arrays.asList("/login", "/logar", "/register", "/registrar");

        return list.stream().anyMatch(command::startsWith);
    }
}
