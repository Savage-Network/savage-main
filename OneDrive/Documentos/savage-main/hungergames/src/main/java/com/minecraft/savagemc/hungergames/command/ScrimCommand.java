package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class ScrimCommand implements BukkitInterface, Assistance, VariableStorage {

    @Command(name = "checkclean", aliases = {"clean", "c"}, platform = Platform.PLAYER, usage = "{label} <target>")
    public void handleCommand(Context<Player> context, Player target) {
        Player sender = context.getSender();

        if (target == null || !sender.canSee(target)) {
            context.info("target.not_found");
            return;
        }

        if (sender == target) {
            context.sendMessage("§cVocê não precisa se cleanar :(");
            return;
        }

        final User user = getUser(target.getUniqueId());

        if (System.currentTimeMillis() < user.getScrimSettings().getCleanTime()) {
            context.sendMessage("§cVocê não pode entrar em combate com " + target.getName() + ".");
            return;
        }

        context.sendMessage("§aVocê pode entrar em combate com " + target.getName() + ".");
    }

    @Command(name = "compass", aliases = {"bussola"}, platform = Platform.PLAYER)
    public void handleCompass(Context<Player> context) {

        final Player player = context.getSender();

        if (!getGame().hasStarted()) {
            player.sendMessage("§cVocê só pode receber uma bússola quando o torneio começar.");
            return;
        }

        ItemStack itemStack = new ItemFactory(Material.COMPASS).setDescription("§7Encontrar jogadores").setName("§aBússola").getStack();

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(itemStack);
            player.sendMessage("§aVocê recebeu uma bússola.");
        } else {
            player.sendMessage("§cO seu inventário está cheio, a bússola foi dropada no chão!");
            player.getWorld().dropItem(player.getLocation().clone().add(0, 0.1, 0), itemStack);
        }
    }

    @Completer(name = "checkclean")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }

}
