package com.minecraft.savagemc.core.bukkit.event.type.server;

import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
@Setter
public class ServerChatEvent extends EventHandler {

    private Player player;
    private final String message;
    private final List<Player> recipients;
    private boolean cancelled = false;

    public ServerChatEvent(Player player, String message, List<Player> recipients) {
        this.player = player;
        this.message = message;
        this.recipients = recipients;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public List<Player> getRecipients() {
        return recipients;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void send(String... message) {
        recipients.forEach(recipient -> recipient.sendMessage(message));
    }

    public void send(BaseComponent... message) {
        recipients.forEach(recipient -> recipient.sendMessage(message));
    }
}
