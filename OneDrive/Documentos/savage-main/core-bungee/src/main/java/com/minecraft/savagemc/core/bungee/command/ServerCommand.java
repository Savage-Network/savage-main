package com.minecraft.savagemc.core.bungee.command;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.bungee.service.server.PterodactylService;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.CommandSender;
import com.minecraft.savagemc.core.command.annotation.Command;

public class ServerCommand implements CommandInheritor {

    private final PterodactylService pterodactyl = new PterodactylService();

    @Command(name = "servidor", aliases = {"server", "sv"}, rank = RankType.ADMIN, onlyPlayer = false)
    public void handle(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();

        if (args.length < 2) {
            sender.send("§cUso: /servidor (ligar/desligar/reiniciar/status) (servidor)");
            sender.send("§cServidores disponíveis: proxy, lobby, lobbybw, lobbyhg, bedwars, duels, pvp, auth");
            return;
        }

        String action = args[0].toLowerCase();
        String serverName = args[1];

        if (!pterodactyl.isValidServer(serverName)) {
            sender.send("§cServidor não encontrado: " + serverName);
            sender.send("§cServidores disponíveis: " + String.join(", ", pterodactyl.getAvailableServers().keySet()));
            return;
        }

        switch (action) {
            case "ligar", "start" -> {
                sender.send("§eIniciando servidor " + serverName + "...");
                pterodactyl.startServer(serverName).thenAccept(success -> {
                    if (success) {
                        sender.send("§aServidor " + serverName + " iniciado com sucesso!");
                    } else {
                        sender.send("§cFalha ao iniciar servidor " + serverName + ".");
                    }
                });
            }
            case "desligar", "stop", "parar" -> {
                sender.send("§eDesligando servidor " + serverName + "...");
                pterodactyl.stopServer(serverName).thenAccept(success -> {
                    if (success) {
                        sender.send("§aServidor " + serverName + " desligado com sucesso!");
                    } else {
                        sender.send("§cFalha ao desligar servidor " + serverName + ".");
                    }
                });
            }
            case "reiniciar", "restart" -> {
                sender.send("§eReiniciando servidor " + serverName + "...");
                pterodactyl.restartServer(serverName).thenAccept(success -> {
                    if (success) {
                        sender.send("§aServidor " + serverName + " reiniciado com sucesso!");
                    } else {
                        sender.send("§cFalha ao reiniciar servidor " + serverName + ".");
                    }
                });
            }
            case "status" -> {
                sender.send("§eVerificando status do servidor " + serverName + "...");
                pterodactyl.getServerStatus(serverName).thenAccept(status -> {
                    String color = switch (status) {
                        case "ONLINE" -> "§a";
                        case "OFFLINE" -> "§c";
                        case "STARTING", "STOPPING" -> "§e";
                        default -> "§7";
                    };
                    sender.send("§7Status do servidor §f" + serverName + "§7: " + color + status);
                });
            }
            default -> {
                sender.send("§cAção inválida: " + action);
                sender.send("§cAções disponíveis: ligar, desligar, reiniciar, status");
            }
        }
    }
}
