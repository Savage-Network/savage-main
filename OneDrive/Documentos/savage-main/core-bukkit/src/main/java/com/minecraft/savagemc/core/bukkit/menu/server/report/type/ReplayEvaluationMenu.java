package com.minecraft.savagemc.core.bukkit.menu.server.report.type;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.api.report.Report;
import com.minecraft.savagemc.core.api.report.replay.Replay;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountGlobalMessage;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ReplayEvaluationMenu extends Menu {

    private final Report report;
    private final Replay replay;

    public ReplayEvaluationMenu(Player player, Report report, Replay replay, Menu last) {
        super(player, "Avaliar: " + report.getTargetName(), last, 3);

        this.report = report;
        this.replay = replay;
    }

    @Override
    public void handle() {
        clear();

        // Informações do jogador
        Account target = Savage.getAccountData().of(report.getTarget());
        
        addItem(4, Item.of(Material.SKULL_ITEM, 3, "§e" + report.getTargetName(),
                        "§7Avalie se o jogador",
                        "§7está usando cheats.")
                .skullByBase64(target != null ? target.getSkin().getValue() : ""));

        // Botão XITADO (Cheater)
        addItem(11, Item.of(Material.REDSTONE_BLOCK, "§c§lXITADO",
                        "§7Marcar jogador como",
                        "§7suspeito de cheats.",
                        "",
                        "§7Isso enviará o report",
                        "§7para análise da staff.",
                        "",
                        "§eClique para marcar!")
                .click(event -> {
                    close();
                    sound(MenuSound.DONE);

                    // Envia notificação para toda staff online
                    String message = "§c§l[ANTI-CHEAT] §f" + getPlayer().getName() + 
                            " §cmarcou §f" + report.getTargetName() + " §ccomo XITADO!";
                    
                    // Notifica todos staffers online
                    Savage.getAccountData().list().stream()
                            .filter(Account::isStaffer)
                            .filter(Account::isOnline)
                            .forEach(staffer -> {
                                new AccountGlobalMessage(staffer.getId(), message).send();
                            });

                    getPlayer().sendMessage("§aVocê marcou " + report.getTargetName() + " como XITADO.");
                    getPlayer().sendMessage("§aA staff foi notificada para análise.");
                    
                    // Mantém o report para análise da staff
                }));

        // Botão LEGIT (Não está trapaceando)
        addItem(15, Item.of(Material.EMERALD_BLOCK, "§a§lLEGIT",
                        "§7Marcar jogador como",
                        "§7legítimo (sem cheats).",
                        "",
                        "§7Isso removerá o report",
                        "§7do sistema.",
                        "",
                        "§eClique para marcar!")
                .click(event -> {
                    close();
                    sound(MenuSound.DONE);

                    // Remove o report
                    Savage.getReportData().delete(report);

                    getPlayer().sendMessage("§aVocê marcou " + report.getTargetName() + " como LEGIT.");
                    getPlayer().sendMessage("§aO report foi removido do sistema.");
                    
                    if (isReturnable() && getLast() != null)
                        getLast().handle();
                }));

        if (isReturnable())
            addBackButton();

        display();
    }
}
