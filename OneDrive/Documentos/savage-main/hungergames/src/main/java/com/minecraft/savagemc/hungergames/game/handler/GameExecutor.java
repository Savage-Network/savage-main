/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.game.handler;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.event.game.GameTimeEvent;
import com.minecraft.savagemc.hungergames.game.Game;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import com.minecraft.savagemc.hungergames.util.constructor.listener.RecurringListener;
import com.minecraft.savagemc.hungergames.util.game.GameStage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@RecurringListener(register = GameStage.WAITING, unregister = GameStage.VICTORY)
public abstract class GameExecutor implements Listener, BukkitInterface, Assistance {

    private Game game;

    public GameExecutor(HungerGames hungerGames) {
        run(() -> this.game = hungerGames.getGame(), 2L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGameTime(GameTimeEvent event) {
        getPlugin().getUserStorage().getUsers().forEach(game::handleSidebar);

        if (game.getRecoveryMode().isEnabled())
            return;

        execute();
    }

    public abstract void execute();

    public int count() {
        return getPlugin().getUserStorage().getAliveUsers().size();
    }

    public void broadcast(String key, GameStage stage, int time) {
        String pt = DateUtils.formatTime(Language.PORTUGUESE, time), us = DateUtils.formatTime(Language.ENGLISH, time);
        for (Player p : Bukkit.getOnlinePlayers()) {
            Account account = Account.fetch(p.getUniqueId());
            if (account == null)
                continue;
            p.sendMessage(account.getLanguage().translate(key, (account.getLanguage()) == Language.PORTUGUESE ? pt : us));

            // Efeito sonoro para contagem regressiva do torneio
            if (stage == GameStage.WAITING && time <= 5) {
                p.playSound(p.getLocation(), Sound.CLICK, 3.5F, 3.5F);
            } else if (stage == GameStage.WAITING && (time % 30 == 0 || (time % 5 == 0 && time <= 15))) {
                // Efeito sonoro "ui.button.click" para mensagens de torneio
                p.playSound(p.getLocation(), Sound.CLICK, 1.0F, 2.0F);
            }
        }
    }

    public void start() {
        game.setTime(game.getVariables().getInvincibility() + 1);
        game.setStage(GameStage.INVINCIBILITY);
//        game.getHungerGames().getKitStorage().register();
        Bukkit.getOnlinePlayers().forEach(player -> {
            game.prepare(player);
            player.sendMessage(Account.fetch(player.getUniqueId()).getLanguage().translate("hg.game.start", count()));
            // Efeito sonoro original mantido
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 3F, 1F);
            // Novo efeito sonoro "entity.ender_dragon.growl" quando a partida começa
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
        });
        getPlugin().getHologramProvider().getHologramListener().unload();
        getPlugin().getNPCProvider().getNpcListener().unload();
        game.getLeaderboardPresets().forEach(leaderboardPreset -> leaderboardPreset.getLeaderboard().destroy());
        game.getWorld().setGameRuleValue("doDaylightCycle", "true");
    }
}
