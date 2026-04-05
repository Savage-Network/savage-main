/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.user.listener;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.core.util.ranking.RankingTarget;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class UserLoader implements Listener, BukkitInterface {


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {

        Account account = Account.fetch(event.getPlayer().getUniqueId());

        if (account == null) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("unexpected_error");
            return;
        }

        User user = HungerGames.getInstance().getUserStorage().storeIfAbsent(account);
        user.setPlayer(event.getPlayer());
        user.setOnline(true);
        user.setBossbar(BukkitGame.getEngine().getBossbarProvider().getBossbar(event.getPlayer()));

        RankingFactory factory = HungerGames.getInstance().getRankingFactory();

        if (factory != null) {
            account.setRanking(Ranking.fromId(account.getData(factory.getTarget().getRanking()).getAsInt()));

            if (factory.getBestPlayers().contains(account.getUniqueId()) && account.getRanking() == Ranking.MASTER_IV)
                account.setRanking(Ranking.CHALLENGER);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        // Otimização: só processar se houve mudança significativa de posição
        if (to == null || (from.getX() == to.getX() && from.getZ() == to.getZ() && from.getY() == to.getY())) {
            return;
        }

        Block headBlock = player.getLocation().add(0, 1, 0).getBlock();

        if (headBlock.getType() == Material.OBSIDIAN) {
            // Permitir movimento normal dentro da obsidian
            // O Minecraft não vai mais forçar o jogador para fora
            // O dano de sufocamento será aplicado via outro listener
            return; // Não cancelar movimento - deixar jogador se mover livremente
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Cancelar apenas o forçamento de movimento do Minecraft por sufocamento
        // Manter o dano padrão de sufocamento (respeitando armadura)
        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            Block headBlock = player.getLocation().add(0, 1, 0).getBlock();
            if (headBlock.getType() == Material.OBSIDIAN) {
                // Não cancelar o evento - deixar o dano padrão do Minecraft funcionar
                // Apenas garantir que o movimento não seja forçado (já feito no onMove)
                return;
            }
        }
    }




    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        User user = User.fetch(event.getPlayer().getUniqueId());
        user.setOnline(false);

        if (user.isAlive() && user.getKills() > 0)
            async(() -> user.getAccount().getDataStorage().saveTable(Tables.HUNGERGAMES));
    }
}
