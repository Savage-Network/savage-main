package com.minecraft.savagemc.core.bukkit.command.single;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class EventCommand implements CommandInheritor {

    @Command(name = "evento", aliases = {"event"})
    public void execute(BukkitCommandContext context) {
        Account account = context.getAccount();
        Player player = context.getPlayer();
        
        if (account == null) {
            player.sendMessage("§cOcorreu um erro ao processar sua conta.");
            return;
        }
        
        // Conectar ao servidor de eventos direto pelo IP
        try {
            // Usando connectToServer com IP e porta específicos
            player.sendMessage("§aConectando ao servidor de evento...");
            
            // Envia mensagem para o BungeeCord conectar ao servidor específico
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            
            out.writeUTF("Connect");
            out.writeUTF("dev.savagemc.net:25573");
            
            player.sendPluginMessage(Savage.getJavaPlugin(), "BungeeCord", b.toByteArray());
            
        } catch (Exception e) {
            player.sendMessage("§cNão foi possível conectar ao servidor de evento.");
            e.printStackTrace();
        }
    }
}
