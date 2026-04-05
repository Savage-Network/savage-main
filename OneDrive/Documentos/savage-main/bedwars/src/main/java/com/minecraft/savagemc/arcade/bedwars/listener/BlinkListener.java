package com.minecraft.savagemc.arcade.bedwars.listener;

import com.minecraft.savagemc.arcade.bedwars.BedWars;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlinkListener implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 20000; // 20 segundos em milissegundos

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item == null || item.getType() != Material.NETHER_STAR) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        if (!item.getItemMeta().getDisplayName().contains("Blink")) return;

        User user = (User) User.of(player.getUniqueId());
        if (user == null || user.getArena() == null) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        event.setCancelled(true);

        // Verificar cooldown
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (cooldowns.containsKey(playerId)) {
            long timeLeft = (cooldowns.get(playerId) + COOLDOWN_TIME) - currentTime;
            if (timeLeft > 0) {
                player.sendMessage("§cAguarde " + (timeLeft / 1000) + " segundos para usar o Blink novamente!");
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
        }

        // Aplicar cooldown
        cooldowns.put(playerId, currentTime);

        // Teleportar jogador 5 blocos acima
        Location currentLoc = player.getLocation();
        Location targetLoc = currentLoc.clone().add(0, 5, 0);

        // Colocar folha na posição
        Block leafBlock = targetLoc.getBlock();
        leafBlock.setType(Material.LEAVES);
        leafBlock.setData((byte) 0);

        // Teleportar jogador
        player.teleport(targetLoc.add(0, 1, 0));
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.sendMessage("§aBlink ativado! A folha desaparecerá em 10 segundos.");

        // Remover 1 item do stack
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }

        // Remover a folha após 10 segundos
        new BukkitRunnable() {
            @Override
            public void run() {
                if (leafBlock.getType() == Material.LEAVES) {
                    leafBlock.setType(Material.AIR);
                    if (player.isOnline()) {
                        player.playSound(leafBlock.getLocation(), Sound.DIG_GRASS, 1.0f, 1.0f);
                    }
                }
            }
        }.runTaskLater(BukkitCore.getPlugin(BedWars.class), 200L); // 10 segundos = 200 ticks
    }
}
