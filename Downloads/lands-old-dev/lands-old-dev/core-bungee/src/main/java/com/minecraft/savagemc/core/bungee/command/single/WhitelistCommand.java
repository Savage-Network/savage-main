package com.minecraft.savagemc.core.bungee.command.single;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.backend.data.list.server.ServerData;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.CommandSender;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.server.Server;
import com.minecraft.savagemc.core.server.payload.whitelist.ServerWhitelist;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;
import java.util.Map;

public class WhitelistCommand implements CommandInheritor {

    private final ServerData serverData = Savage.getServerData();

    @Command(name = "whitelist", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelist(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        List<String> subCommands = context.getSubCommands();

        String[] args = context.getArgs();

        if (args.length == 0 || !subCommands.contains(args[0].toLowerCase())) {
            String label = "/" + context.getLabel();

            sender.send("§cUso do " + label + ":",
                    "§c* " + label + " on (servidor)",
                    "§c* " + label + " off (servidor)",
                    "§c* " + label + " status (servidor)",
                    "§c* " + label + " add (servidor) (jogador)",
                    "§c* " + label + " remove (servidor) (jogador)",
                    "§c* " + label + " info (servidor)");
        }
    }

    private Server findServer(String name) {
        // Primeiro tenta buscar nos servidores configurados no BungeeCord
        Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();
        
        // Busca exata primeiro (hub, lobby, etc)
        ServerInfo serverInfo = servers.get(name.toLowerCase());
        if (serverInfo != null) {
            // Tenta encontrar no Redis primeiro
            Server redisServer = serverData.of(s -> s.getNameAndId().equalsIgnoreCase(name) || 
                                              s.getIdentifier().contains(name.toLowerCase()));
            if (redisServer != null) return redisServer;
            
            // Se não encontrar no Redis, cria um servidor virtual baseado no ServerInfo do BungeeCord
            return createVirtualServer(serverInfo, name);
        }
        
        // Busca por nome parcial nos servidores do BungeeCord
        for (Map.Entry<String, ServerInfo> entry : servers.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(name.toLowerCase())) {
                String serverName = entry.getKey();
                ServerInfo info = entry.getValue();
                
                // Tenta encontrar no Redis primeiro
                Server redisServer = serverData.of(s -> s.getNameAndId().equalsIgnoreCase(serverName) || 
                                                  s.getIdentifier().contains(serverName.toLowerCase()));
                if (redisServer != null) return redisServer;
                
                // Se não encontrar no Redis, cria um servidor virtual
                return createVirtualServer(info, serverName);
            }
        }
        
        // Se não encontrar no BungeeCord, tenta busca exata no Redis (hub-1, lobby-2, etc)
        Server server = serverData.findByNameAndId(name);
        if (server != null) return server;
        
        // Por último, tenta por nome parcial no Redis
        return serverData.of(s -> s.getNameAndId().toLowerCase().startsWith(name.toLowerCase()));
    }
    
    private Server createVirtualServer(ServerInfo serverInfo, String name) {
        try {
            // Cria um servidor virtual para operações de whitelist
            // Usando valores padrão pois não temos acesso direto ao ServerType
            int port = serverInfo.getAddress().getPort();
            int id = 1; // ID padrão
            
            // Cria um Server virtual usando reflection ou método disponível
            java.lang.reflect.Constructor<Server> constructor = Server.class.getDeclaredConstructor(
                int.class, int.class, com.minecraft.savagemc.core.server.type.ServerType.class, 
                com.minecraft.savagemc.core.server.payload.ServerPayload.class
            );
            constructor.setAccessible(true);
            
            // Tenta determinar o ServerType pelo nome
            com.minecraft.savagemc.core.server.type.ServerType serverType = 
                com.minecraft.savagemc.core.server.type.ServerType.valueOf(name.toUpperCase());
            
            com.minecraft.savagemc.core.server.payload.ServerPayload payload = 
                new com.minecraft.savagemc.core.server.payload.ServerPayload(100);
            
            Server virtualServer = constructor.newInstance(id, port, serverType, payload);
            
            // Salva no Redis para persistência
            Savage.getServerData().refresh(virtualServer);
            
            return virtualServer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Command(name = "whitelist.info", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistInfo(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /" + context.getCommandLabel() + " (servidor).");
            return;
        }

        Server server = findServer(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        sender.send("§eInformações sobre manutenção:",
                "§7Servidor: §f" + server.getName(),
                "§7Estado: §f" + (whitelist.isEnabled() ? "Ativo" : "Inativo"),
                "§7Membros na lista: §f" + whitelist.getPlayers().size());
    }

    @Command(name = "whitelist.status", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistStatus(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /" + context.getCommandLabel() + " (servidor).");
            return;
        }

        Server server = findServer(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        whitelist.setEnabled(!whitelist.isEnabled());

        sender.send(whitelist.isEnabled()
                ? "§aA whitelist do servidor §e\"" + server.getNameAndId() + "\"§a foi ativada."
                : "§cA whitelist do servidor §e\"" + server.getNameAndId() + "\"§c foi desativada.");

        server.refresh();
    }

    @Command(name = "whitelist.on", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistOn(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /" + context.getCommandLabel() + " (servidor).");
            return;
        }

        Server server = findServer(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        if (whitelist.isEnabled()) {
            sender.send("§cA whitelist do servidor §e\"" + server.getNameAndId() + "\"§c já está ativada.");
            return;
        }

        whitelist.setEnabled(true);

        sender.send("§aA whitelist do servidor §e\"" + server.getNameAndId() + "\"§a foi ativada.");

        server.refresh();
    }

    @Command(name = "whitelist.off", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistOff(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /" + context.getCommandLabel() + " (servidor).");
            return;
        }

        Server server = findServer(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        if (!whitelist.isEnabled()) {
            sender.send("§cA whitelist do servidor §e\"" + server.getNameAndId() + "\"§c já está desativada.");
            return;
        }

        whitelist.setEnabled(false);

        sender.send("§cA whitelist do servidor §e\"" + server.getNameAndId() + "\"§c foi desativada.");

        server.refresh();
    }

    @Command(name = "whitelist.add", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistAdd(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length < 2) {
            sender.send("§cUso: /" + context.getCommandLabel() + " (servidor) (jogador).");
            return;
        }

        Server server = findServer(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        if (!whitelist.isEnabled()) {
            sender.send("§cO servidor solicitado não está em manutenção.");
            return;
        }

        String playerName = args[1].toLowerCase();

        if (whitelist.hasPlayer(playerName)) {
            sender.send("§cO jogador " + playerName + " já está na lista da manutenção.");
            return;
        }

        whitelist.getPlayers().add(playerName);
        server.refresh();

        sender.send("§aO jogador " + playerName + " foi adicionado na manutenção.");
    }

    @Command(name = "whitelist.remove", rank = RankType.MODPLUS, onlyPlayer = false)
    public void whitelistRemove(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length < 2) {
            sender.send("§cUso: /" + context.getCommandLabel() + " (servidor) (jogador).");
            return;
        }

        Server server = findServer(args[0]);

        if (server == null) {
            sender.send("§cO servidor solicitado não foi encontrado.");
            return;
        }

        ServerWhitelist whitelist = server.getWhitelist();

        if (!whitelist.isEnabled()) {
            sender.send("§cO servidor solicitado não está em manutenção.");
            return;
        }

        String playerName = args[1].toLowerCase();

        if (!whitelist.hasPlayer(playerName)) {
            sender.send("§cO jogador " + playerName + " não está na lista da manutenção.");
            return;
        }

        whitelist.getPlayers().remove(playerName);
        server.refresh();

        sender.send("§eO jogador " + playerName + " foi removido da manutenção.");
    }
}
