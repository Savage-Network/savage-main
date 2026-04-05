package com.minecraft.savagemc.arcade.bedwars.listener;

import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.util.list.bukkit.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CompactTowerListener implements Listener {

    private final Map<UUID, Long> towerCooldown = new HashMap<>();
    private static final long TOWER_COOLDOWN = 3000L; // 3 segundos

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getItemInHand();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (handItem == null || handItem.getType() != Material.BRICK)
            return;

        if (!handItem.hasItemMeta() || !handItem.getItemMeta().hasDisplayName())
            return;

        if (!"§aTorre Compacta".equals(handItem.getItemMeta().getDisplayName()))
            return;

        // Verificar cooldown
        long now = System.currentTimeMillis();
        if (towerCooldown.containsKey(player.getUniqueId())) {
            long lastUse = towerCooldown.get(player.getUniqueId());
            if (now - lastUse < TOWER_COOLDOWN) {
                long secondsLeft = (TOWER_COOLDOWN - (now - lastUse)) / 1000;
                player.sendMessage("§cVocê precisa esperar " + secondsLeft + "s para usar a torre novamente!");
                event.setCancelled(true);
                return;
            }
        }

        towerCooldown.put(player.getUniqueId(), now);
        event.setCancelled(true);

        // Remover um item do stack
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }

        // Criar a torre compacta
        createCompactTower(player);
    }

    private void createCompactTower(Player player) {
        User user = (User) User.of(player.getUniqueId());
        if (user == null) return;

        Location loc = player.getLocation().getBlock().getLocation();
        int woolColor = ColorUtil.getIdByColor(user.getTeam().getColor());

        // Torre vertical de 10 blocos (1x1)
        List<int[]> structure = new ArrayList<>();
        
        // Base (y=0) - 3x3 para estabilidade
        structure.addAll(Arrays.asList(
            new int[]{0, 0, 0, 1}, new int[]{1, 0, 0, 1}, new int[]{-1, 0, 0, 1},
            new int[]{0, 0, 1, 1}, new int[]{1, 0, 1, 1}, new int[]{-1, 0, 1, 1},
            new int[]{0, 0, -1, 1}, new int[]{1, 0, -1, 1}, new int[]{-1, 0, -1, 1}
        ));
        
        // Torre vertical (y=1-10) - 1x1
        for (int y = 1; y <= 10; y++) {
            structure.add(new int[]{0, y, 0, 1});
        }
        
        // Plataforma no topo (y=11) - 3x3
        structure.addAll(Arrays.asList(
            new int[]{0, 11, 0, 1}, new int[]{1, 11, 0, 1}, new int[]{-1, 11, 0, 1},
            new int[]{0, 11, 1, 1}, new int[]{1, 11, 1, 1}, new int[]{-1, 11, 1, 1},
            new int[]{0, 11, -1, 1}, new int[]{1, 11, -1, 1}, new int[]{-1, 11, -1, 1}
        ));

        // Construir torre com animação (10 blocos por tick)
        new BukkitRunnable() {
            int index = 0;
            
            @Override
            public void run() {
                int placed = 0;
                while (placed < 10 && index < structure.size()) {
                    int[] pos = structure.get(index);
                    Location blockLoc = loc.clone().add(pos[0], pos[1], pos[2]);
                    Block block = blockLoc.getBlock();

                    if (block.getType() == Material.AIR) {
                        block.setType(Material.WOOL);
                        block.setData((byte) woolColor);
                        placed++;
                    }
                    index++;
                }

                if (index >= structure.size()) {
                    cancel();
                }
            }
        }.runTaskTimer(Savage.getJavaPlugin(), 1L, 1L);

        player.sendMessage("§aTorre Compacta criada!");
        player.sendTitle("§a§lTORRE COMPACTA", "§7Torre de 10 blocos criada!");
    }
}
