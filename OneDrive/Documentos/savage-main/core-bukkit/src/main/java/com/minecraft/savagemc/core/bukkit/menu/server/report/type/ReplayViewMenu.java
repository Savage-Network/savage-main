package com.minecraft.savagemc.core.bukkit.menu.server.report.type;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.api.report.Report;
import com.minecraft.savagemc.core.api.report.replay.Replay;
import com.minecraft.savagemc.core.api.report.replay.ReplayRecorder;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountExecuteCommandMessage;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ReplayViewMenu extends Menu {

    private final Report report;
    private final Replay replay;

    public ReplayViewMenu(Player player, Report report, Menu last) {
        super(player, "Replay: " + report.getTargetName(), last, 3);

        this.report = report;
        this.replay = report.getReplayId() != null ? ReplayRecorder.getReplay(report.getReplayId()) : null;
    }

    @Override
    public void handle() {
        clear();

        if (replay == null) {
            addItem(13, Item.of(Material.BARRIER, "§cReplay não disponível",
                    "§7Este report não possui",
                    "§7replay gravado."));
            
            if (isReturnable())
                addBackButton();
            
            display();
            return;
        }

        // Informações do replay
        addItem(4, Item.of(Material.PAPER, "§eInformações do Replay",
                "§7Jogador: §f" + report.getTargetName(),
                "§7Modo: §f" + replay.getGameMode(),
                "§7Arena: §f" + replay.getArenaName(),
                "§7Duração: §f" + replay.getDuration() + "s"));

        // Botão para assistir replay
        addItem(11, Item.of(Material.EYE_OF_ENDER, "§aClique para ver a partida",
                        "§7Você será teleportado",
                        "§7para assistir o replay.",
                        "",
                        "§eClique para assistir!")
                .click(event -> {
                    close();
                    sound(MenuSound.DONE);

                    // Teleporta para modo espectador do replay
                    getPlayer().sendMessage("§aIniciando replay...");
                    // TODO: Implementar sistema de reprodução do replay
                    // Por enquanto, apenas mostra o menu de avaliação
                    new ReplayEvaluationMenu(getPlayer(), report, replay, this).handle();
                }));

        // Botão de avaliação direta
        addItem(15, Item.of(Material.REDSTONE_COMPARATOR, "§eAvaliar sem assistir",
                        "§7Pule direto para a",
                        "§7avaliação do jogador.",
                        "",
                        "§eClique para avaliar!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new ReplayEvaluationMenu(getPlayer(), report, replay, this).handle();
                }));

        if (isReturnable())
            addBackButton();

        display();
    }
}
