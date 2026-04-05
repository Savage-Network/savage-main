package com.minecraft.savagemc.core.bukkit.command.structure;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.command.CommandSender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class BukkitCommandSender implements CommandSender {

    private final org.bukkit.command.CommandSender sender;

    public BukkitCommandSender(org.bukkit.command.CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public UUID getId() {
        return isPlayer() ? ((Player) sender).getUniqueId() : Constant.DEFAULT_ID;
    }

    @Override
    public String getName() {
        return isPlayer() ? sender.getName() : "CONSOLE";
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public void send(String... messages) {
        sender.sendMessage(messages);
    }

    @Override
    public void send(BaseComponent... message) {
        if (!isPlayer()) {
            sender.sendMessage("§cSomente jogadores podem receber mensagens de componentes.");
            return;
        }

        Player player = (Player) sender;

        player.sendMessage(message);
    }
}
