package com.minecraft.savagemc.arcade.bedwars.listener;

import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.util.list.bukkit.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PopTowerListener implements Listener {

    private final Map<UUID, Long> lastPopTime = new HashMap<>();
    private static final long POP_COOLDOWN = 500L; // 0.5 segundos entre ativações

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Verificar se o jogador está caindo
        Vector velocity = player.getVelocity();
        if (velocity.getY() >= -0.5) return; // Não está caindo rápido o suficiente
        
        // Verificar se tem PopTower no inventário
        ItemStack popTower = null;
        int slot = -1;
        
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.BLAZE_ROD 
                    && item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    && "§aPopTower".equals(item.getItemMeta().getDisplayName())) {
                popTower = item;
                slot = i;
                break;
            }
        }
        
        if (popTower == null) return;
        
        // Verificar cooldown
        long now = System.currentTimeMillis();
        if (lastPopTime.containsKey(player.getUniqueId())) {
            long last = lastPopTime.get(player.getUniqueId());
            if (now - last < POP_COOLDOWN) return;
        }
        
        // Verificar se há espaço abaixo (não criar torre se já tem blocos)
        Location playerLoc = player.getLocation();
        Block blockBelow = playerLoc.clone().subtract(0, 1, 0).getBlock();
        
        if (blockBelow.getType() != Material.AIR) return;
        
        // Criar torre instantânea
        User user = (User) User.of(player.getUniqueId());
        if (user == null) return;
        
        int woolColor = ColorUtil.getIdByColor(user.getTeam().getColor());
        Location baseLoc = playerLoc.clone().subtract(0, 1, 0).getBlock().getLocation();
        
        // Torre de 5 blocos (1x1)
        for (int y = 0; y < 5; y++) {
            Block block = baseLoc.clone().add(0, -y, 0).getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(Material.WOOL);
                block.setData((byte) woolColor);
            }
        }
        
        // Remover item
        if (popTower.getAmount() > 1) {
            popTower.setAmount(popTower.getAmount() - 1);
        } else {
            player.getInventory().setItem(slot, null);
        }
        
        lastPopTime.put(player.getUniqueId(), now);
        player.sendMessage("§aPopTower ativada!");
    }
}
