package com.minecraft.game.command;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.arcade.ArcadeHolder;
import com.minecraft.savagemc.core.arcade.room.map.Map;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommand;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.event.IgnoreEvent;
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
            account.send("§cUso: /game <evento> <configuração>");
            account.send("§cExemplo: /game sumo setmap <mapa>");
            account.send("§cExemplo: /game sumo listmaps");
            account.send("§cExemplo: /game sumo reload");
            return;
        }

        String eventName = args[0].toLowerCase();

        ArcadeHolder arcade = Savage.getArcadeData().getArcades().stream()
                .filter(arc -> arc.getName().equalsIgnoreCase(eventName))
                .findFirst()
                .orElse(null);

        if (arcade == null) {
            account.send("§cEvento não encontrado: " + eventName);
            return;
        }

        if (args.length == 1) {
            account.send("§cUso: /game " + eventName + " <configuração>");
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
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
    }
}
