package com.minecraft.savagemc.core.bukkit.command.single;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class AnunciarCommand implements CommandInheritor {

    @Command(name = "anunciar", aliases = {"announce"}, rank = RankType.ADMIN)
    public void execute(BukkitCommandContext context) {
        Account account = context.getAccount();
        Player player = context.getPlayer();
        
        if (account == null) {
            player.sendMessage("§cOcorreu um erro ao processar sua conta.");
            return;
        }
        
        String[] args = context.getArgs();
        
        if (args.length == 0) {
            player.sendMessage("§cUso: /anunciar <nome do evento>");
            return;
        }
        
        String eventName = String.join(" ", args);
        String serverPrefix = Savage.getServerData().getServerType() == null ? "Savage" : 
                            Savage.getServerData().getServerType().getName();
        
        String announcement = serverPrefix + " &b&l" + eventName.toUpperCase() + " &euse &6/evento &epara jogar!";
        
        // Enviar anúncio para todos os jogadores em todos os servidores via BungeeCord
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            
            out.writeUTF("Message");
            out.writeUTF(announcement);
            
            player.sendPluginMessage(Savage.getJavaPlugin(), "BungeeCord", b.toByteArray());
            
            // Também enviar localmente para garantir que todos recebam
            Savage.getServerData().broadcastMessage(announcement);
            
            player.sendMessage("§aAnúncio do evento '" + eventName + "' enviado para todos os servidores!");
        } catch (Exception e) {
            player.sendMessage("§cErro ao enviar anúncio. Tentando método alternativo...");
            // Fallback: enviar para todos os servidores via sistema normal
            Savage.getServerData().broadcastMessage(announcement);
            player.sendMessage("§aAnúncio do evento '" + eventName + "' enviado (modo alternativo).");
        }
    }
}
