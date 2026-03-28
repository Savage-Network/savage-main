package com.minecraft.savagemc.core.bukkit.listener.cosmetic;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.collectible.nms.NMSContract;
import com.minecraft.savagemc.core.api.collectible.operator.CollectibleOperator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Sincroniza o estado visual dos collectibles (cores, efeitos) ao entrar no servidor.
 */
public class CosmeticColorSyncListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Account account = Savage.getAccountController().of(player.getUniqueId());

        if (account == null) return;

        // Verifica se NMS está inicializado para evitar erros
        if (NMSContract.getInstance() == null) {
            return;
        }

        // Reaplica collectibles ativos para todos os jogadores online
        for (Player online : player.getServer().getOnlinePlayers()) {
            if (online.equals(player)) continue;

            try {
                CollectibleOperator.list(online).forEach(op -> {
                    if (op.getCollectible() != null)
                        op.handle(op.getCollectible());
                });
            } catch (Exception e) {
                // Ignora erros de collectibles para não kickar jogadores
                Savage.Savage.Logger().warning("Erro ao aplicar collectible para jogador " + online.getName() + ": " + e.getMessage());
            }
        }
    }
}
