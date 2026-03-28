package com.minecraft.game.command;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.arcade.ArcadeHolder;
import com.minecraft.savagemc.core.arcade.room.map.Map;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommand;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@IgnoreEvent
public class GameAdminCommand extends BukkitCommand {

    public GameAdminCommand() {
        super("game");
    }

    @Override
    public void onCommand(BukkitCommandContext context) {
        Player player = context.getPlayer();
        Account account = context.getAccount();

        if (!account.hasTag(Tag.MANAGER)) {
            account.send("§cVocê não tem permissão para usar este comando.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            sendHelp(account);
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "load": {
                if (args.length < 2) {
                    account.send("§cUso: /game load <nome_do_mapa>");
                    return;
                }

                String mapName = args[1];
                
                account.send("§eCarregando mapa " + mapName + "...");
                account.send("§aVocê pode configurar as posições usando:");
                account.send("§7- /game setpos spawn");
                account.send("§7- /game setpos red_spawn");
                account.send("§7- /game setpos blue_spawn");
                break;
            }

            case "tp": {
                if (args.length < 2) {
                    account.send("§cUso: /game tp <nome_do_mapa>");
                    return;
                }

                String mapName = args[1];
                
                // Buscar o mapa em todos os eventos
                Map foundMap = null;
                for (ArcadeHolder arcade : Savage.getArcadeData().getArcades()) {
                    foundMap = arcade.getMaps().stream()
                            .filter(map -> map.getName().equalsIgnoreCase(mapName))
                            .findFirst()
                            .orElse(null);
                    
                    if (foundMap != null) break;
                }

                if (foundMap == null) {
                    account.send("§cMapa não encontrado: " + mapName);
                    return;
                }

                Location spawn = foundMap.getLocation("spawn");
                
                if (spawn == null) {
                    account.send("§cO mapa não possui spawn configurado.");
                    return;
                }

                player.teleport(spawn);
                account.send("§aTeleportado para o mapa " + mapName + "!");
                break;
            }

            case "setpos": {
                if (args.length < 2) {
                    account.send("§cUso: /game setpos <tipo>");
                    account.send("§cTipos: spawn, red_spawn, blue_spawn");
                    return;
                }

                String posType = args[1].toLowerCase();
                Location loc = player.getLocation();

                account.send("§aPosição " + posType + " definida:");
                account.send("§7X: §f" + loc.getBlockX());
                account.send("§7Y: §f" + loc.getBlockY());
                account.send("§7Z: §f" + loc.getBlockZ());
                account.send("§7Yaw: §f" + String.format("%.1f", loc.getYaw()));
                account.send("§7Pitch: §f" + String.format("%.1f", loc.getPitch()));
                account.send("");
                account.send("§eAdicione ao map.yml:");
                account.send("§7" + posType + ":");
                account.send("§7  x: " + loc.getBlockX());
                account.send("§7  y: " + loc.getBlockY());
                account.send("§7  z: " + loc.getBlockZ());
                account.send("§7  yaw: " + String.format("%.1f", loc.getYaw()));
                account.send("§7  pitch: " + String.format("%.1f", loc.getPitch()));
                break;
            }

            case "listpos": {
                if (args.length < 2) {
                    account.send("§cUso: /game listpos <mapa>");
                    return;
                }

                String mapName = args[1];
                
                Map foundMap = null;
                for (ArcadeHolder arcade : Savage.getArcadeData().getArcades()) {
                    foundMap = arcade.getMaps().stream()
                            .filter(map -> map.getName().equalsIgnoreCase(mapName))
                            .findFirst()
                            .orElse(null);
                    
                    if (foundMap != null) break;
                }

                if (foundMap == null) {
                    account.send("§cMapa não encontrado: " + mapName);
                    return;
                }

                account.send("§6Posições configuradas no mapa " + mapName + ":");
                
                Location spawn = foundMap.getLocation("spawn");
                if (spawn != null) {
                    account.send("§eSpawn: §7X=" + spawn.getBlockX() + " Y=" + spawn.getBlockY() + " Z=" + spawn.getBlockZ());
                }

                Location redSpawn = foundMap.getLocation("red_spawn");
                if (redSpawn != null) {
                    account.send("§cRed Spawn: §7X=" + redSpawn.getBlockX() + " Y=" + redSpawn.getBlockY() + " Z=" + redSpawn.getBlockZ());
                }

                Location blueSpawn = foundMap.getLocation("blue_spawn");
                if (blueSpawn != null) {
                    account.send("§9Blue Spawn: §7X=" + blueSpawn.getBlockX() + " Y=" + blueSpawn.getBlockY() + " Z=" + blueSpawn.getBlockZ());
                }

                if (spawn == null && redSpawn == null && blueSpawn == null) {
                    account.send("§cNenhuma posição configurada.");
                }
                break;
            }

            default: {
                String eventName = action;

                ArcadeHolder arcade = Savage.getArcadeData().getArcades().stream()
                        .filter(arc -> arc.getName().equalsIgnoreCase(eventName))
                        .findFirst()
                        .orElse(null);

                if (arcade == null) {
                    account.send("§cEvento não encontrado: " + eventName);
                    sendHelp(account);
                    return;
                }

                if (args.length == 1) {
                    account.send("§cUso: /game " + eventName + " <configuração>");
                    return;
                }

                String subAction = args[1].toLowerCase();

                switch (subAction) {
                    case "listmaps": {
                        account.send("§aMaps disponíveis para " + arcade.getName() + ":");

                        if (arcade.getMaps().isEmpty()) {
                            account.send("§cNenhum mapa carregado.");
                        } else {
                            arcade.getMaps().forEach(map -> 
                                account.send("§7- §e" + map.getName() + " §7(ID: " + map.getId() + ")")
                            );
                        }
                        break;
                    }

                    case "reload": {
                        account.send("§eRecarregando evento " + arcade.getName() + "...");

                        arcade.unload();

                        if (arcade.load()) {
                            account.send("§aEvento " + arcade.getName() + " recarregado com sucesso!");
                            account.send("§aSalas criadas: §f" + arcade.getRooms().size());
                        } else {
                            account.send("§cErro ao recarregar o evento.");
                        }
                        break;
                    }

                    case "info": {
                        account.send("§6Informações do evento " + arcade.getName() + ":");
                        account.send("§7Categoria: §f" + arcade.getCategory().getName());
                        account.send("§7Salas ativas: §f" + arcade.getRooms().size());
                        account.send("§7Mapas carregados: §f" + arcade.getMaps().size());
                        account.send("§7Min/Max salas: §f" + arcade.getMinRooms() + "/" + arcade.getMaxRooms());
                        break;
                    }

                    default: {
                        account.send("§cConfiguração inválida. Use: listmaps, reload, info");
                        break;
                    }
                }
                break;
            }
        }
    }

    private void sendHelp(Account account) {
        account.send("§6§lCOMANDOS DO GAME:");
        account.send("§e/game load <mapa> §7- Carregar um mapa");
        account.send("§e/game tp <mapa> §7- Teleportar para um mapa");
        account.send("§e/game setpos <tipo> §7- Definir posição (spawn, red_spawn, blue_spawn)");
        account.send("§e/game listpos <mapa> §7- Listar posições configuradas");
        account.send("§e/game <evento> listmaps §7- Listar mapas do evento");
        account.send("§e/game <evento> reload §7- Recarregar evento");
        account.send("§e/game <evento> info §7- Informações do evento");
    }
}
