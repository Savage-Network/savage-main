package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.celebrations.Celebration;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import com.minecraft.savagemc.hungergames.util.game.GameStage;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ComemorationCommand implements Assistance, BukkitInterface {

    @Command(name = "commemoration", aliases = {"comemoracao"}, platform = Platform.PLAYER)
    public void handleCommand(final Context<Player> context) {

        if (getGame().getStage() == GameStage.VICTORY) {
            context.sendMessage("§cVocê não pode alterar a sua comemoração de vitória agora.");
            return;
        }

        if (context.argsCount() != 1) {
            context.sendMessage("§cUse /" + context.getLabel() + " <comemoração>");
            return;
        }

        Celebration celebration = getPlugin().getCelebrationStorage().getCelebration(context.getArg(0));

        if (celebration == null) {
            context.sendMessage("§cComemoração de vitória não encontrada.");
            return;
        }

        final User user = User.fetch(context.getUniqueId());

        if (Objects.equals(user.getCelebration().getName(), celebration.getName())) {
            context.sendMessage("§cVocê já está usando essa comemoração de vitória.");
            return;
        }

        if (!user.hasCelebration(celebration)) {
            context.sendMessage("§cVocê não possui essa comemoração de vitória.");
            return;
        }

        context.sendMessage("§eVocê alterou a sua §6comemoração de vitória §epara §b" + celebration.getDisplayName() + "§e.");

        user.setCelebration(celebration);
    }

    @Completer(name = "commemoration")
    public List<String> tabComplete(Context<Player> context) {
        return getPlugin().getCelebrationStorage().getCelebrations().stream().filter(celebration -> startsWith(celebration.getName(), context.getArg(context.argsCount() - 1))).map(celebration -> celebration.getName().toLowerCase()).collect(Collectors.toList());
    }

}
