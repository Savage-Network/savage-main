package com.minecraft.arcade.eventos.commands;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.managers.EventState;
import com.minecraft.arcade.eventos.utils.EventUiTheme;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Abre o menu de espectador (teleportar para jogadores / ir ao lobby).
 * Usado quando o jogador morreu e não segura mais a bússola (invisível).
 */
public class EspectadorCommand implements CommandExecutor {

    private final Eventos plugin;

    public EspectadorCommand(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(EventUiTheme.CHAT_PREFIX + "§cApenas jogadores podem usar este comando.");
            return true;
        }
        Player p = (Player) sender;
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) {
            p.sendMessage(EventUiTheme.CHAT_PREFIX + "§7Nenhum evento em andamento.");
            return true;
        }
        if (!plugin.getEventManager().isEspectador(p.getUniqueId())) {
            p.sendMessage(EventUiTheme.CHAT_PREFIX + "§7Você não está no modo espectador.");
            return true;
        }
        plugin.getSpectatorMenuListener().openMenu(p);
        return true;
    }
}
