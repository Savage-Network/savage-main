package com.minecraft.savagemc.core.bungee.command.single;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.backend.data.list.server.ServerData;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.CommandSender;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.server.Server;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;
import java.util.Map;

public class ServersCommand implements CommandInheritor {

    private final ServerData serverData = Savage.getServerData();

    @Command(name = "servers", rank = RankType.MODPLUS, onlyPlayer = false)
    public void servers(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        
        sender.send("§e=== Servidores no BungeeCord ===");
        Map<String, ServerInfo> bungeeServers = ProxyServer.getInstance().getServers();
        for (Map.Entry<String, ServerInfo> entry : bungeeServers.entrySet()) {
            sender.send("§7- §f" + entry.getKey() + " §7(" + entry.getValue().getAddress().toString() + ")");
        }
        
        sender.send("");
        sender.send("§e=== Servidores no Redis ===");
        List<Server> redisServers = serverData.getServerList();
        if (redisServers.isEmpty()) {
            sender.send("§cNenhum servidor encontrado no Redis.");
        } else {
            for (Server server : redisServers) {
                sender.send("§7- §f" + server.getNameAndId() + " §7(" + server.getIdentifier() + ")");
            }
        }
    }
}
