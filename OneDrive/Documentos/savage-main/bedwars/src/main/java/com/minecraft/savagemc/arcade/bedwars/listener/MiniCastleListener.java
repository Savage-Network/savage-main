package com.minecraft.savagemc.arcade.bedwars.listener;

import com.minecraft.savagemc.arcade.bedwars.BedWars;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.util.list.bukkit.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MiniCastleListener implements Listener {

    private final Map<UUID, Long> castleCooldown = new HashMap<>();
    private static final long CASTLE_COOLDOWN = 5000L; // 5 segundos

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getItemInHand();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (handItem == null || handItem.getType() != Material.CAULDRON)
            return;

        if (!handItem.hasItemMeta() || !handItem.getItemMeta().hasDisplayName())
            return;

        if (!"§aMini Castelo".equals(handItem.getItemMeta().getDisplayName()))
            return;

        // Verificar cooldown
        long now = System.currentTimeMillis();
        if (castleCooldown.containsKey(player.getUniqueId())) {
            long lastUse = castleCooldown.get(player.getUniqueId());
            if (now - lastUse < CASTLE_COOLDOWN) {
                long secondsLeft = (CASTLE_COOLDOWN - (now - lastUse)) / 1000;
                player.sendMessage("§cVocê precisa esperar " + secondsLeft + "s para usar o castelo novamente!");
                event.setCancelled(true);
                return;
            }
        }

        castleCooldown.put(player.getUniqueId(), now);
        event.setCancelled(true);

        // Remover um item do stack
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }

        // Criar o mini castelo
        createMiniCastle(player);
    }

    private void createMiniCastle(Player player) {
        User user = (User) User.of(player.getUniqueId());
        if (user == null) return;

        Location loc = player.getLocation().getBlock().getLocation();
        int woolColor = ColorUtil.getIdByColor(user.getTeam().getColor());

        // Determinar direção do jogador
        BlockFace direction = getCardinalDirection(player);

        // Obter estrutura baseada na direção
        List<int[]> structure = getTowerStructure(direction);

        // Construir torre com animação (5 blocos por tick)
        new BukkitRunnable() {
            int index = 0;
            
            @Override
            public void run() {
                int placed = 0;
                while (placed < 5 && index < structure.size()) {
                    int[] pos = structure.get(index);
                    Location blockLoc = loc.clone().add(pos[0], pos[1], pos[2]);
                    Block block = blockLoc.getBlock();

                    if (block.getType() == Material.AIR) {
                        if (pos[3] == 1) { // Wool
                            block.setType(Material.WOOL);
                            block.setData((byte) woolColor);
                        } else if (pos[3] == 2) { // Ladder
                            block.setType(Material.LADDER);
                            block.setData((byte) getDirectionData(direction));
                        }
                        placed++;
                    }
                    index++;
                }

                if (index >= structure.size()) {
                    cancel();
                }
            }
        }.runTaskTimer(Savage.getJavaPlugin(), 1L, 1L);

        player.sendMessage("§aMini Castelo criado!");
    }

    private BlockFace getCardinalDirection(Player player) {
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) yaw += 360;

        if (yaw >= 315 || yaw < 45) return BlockFace.SOUTH;
        else if (yaw < 135) return BlockFace.WEST;
        else if (yaw < 225) return BlockFace.NORTH;
        else return BlockFace.EAST;
    }

    private byte getDirectionData(BlockFace direction) {
        switch (direction) {
            case NORTH: return 2;
            case SOUTH: return 3;
            case WEST: return 4;
            case EAST: return 5;
            default: return 2;
        }
    }

    private List<int[]> getTowerStructure(BlockFace direction) {
        // Estrutura: [x, y, z, tipo] onde tipo: 1=wool, 2=ladder
        List<int[]> structure = new ArrayList<>();

        switch (direction) {
            case NORTH:
                // Base (y=0)
                structure.addAll(Arrays.asList(
                    new int[]{0, 0, 0, 1}, new int[]{1, 0, 0, 1}, new int[]{-1, 0, 0, 1},
                    new int[]{0, 0, 1, 1}, new int[]{1, 0, 1, 1}, new int[]{-1, 0, 1, 1},
                    new int[]{0, 0, -1, 1}, new int[]{1, 0, -1, 1}, new int[]{-1, 0, -1, 1}
                ));
                // Paredes (y=1-6)
                for (int y = 1; y <= 6; y++) {
                    structure.addAll(Arrays.asList(
                        new int[]{1, y, 1, 1}, new int[]{-1, y, 1, 1},
                        new int[]{1, y, -1, 1}, new int[]{-1, y, -1, 1}
                    ));
                }
                // Escada central
                for (int y = 1; y <= 7; y++) {
                    structure.add(new int[]{0, y, 0, 2});
                }
                // Topo (y=7)
                structure.addAll(Arrays.asList(
                    new int[]{0, 7, 1, 1}, new int[]{1, 7, 1, 1}, new int[]{-1, 7, 1, 1},
                    new int[]{0, 7, -1, 1}, new int[]{1, 7, -1, 1}, new int[]{-1, 7, -1, 1},
                    new int[]{0, 7, 2, 1}, new int[]{0, 7, -2, 1}, new int[]{2, 7, 0, 1}, new int[]{-2, 7, 0, 1}
                ));
                break;

            case SOUTH:
                // Base (y=0)
                structure.addAll(Arrays.asList(
                    new int[]{0, 0, 0, 1}, new int[]{1, 0, 0, 1}, new int[]{-1, 0, 0, 1},
                    new int[]{0, 0, 1, 1}, new int[]{1, 0, 1, 1}, new int[]{-1, 0, 1, 1},
                    new int[]{0, 0, -1, 1}, new int[]{1, 0, -1, 1}, new int[]{-1, 0, -1, 1}
                ));
                // Paredes (y=1-6)
                for (int y = 1; y <= 6; y++) {
                    structure.addAll(Arrays.asList(
                        new int[]{1, y, 1, 1}, new int[]{-1, y, 1, 1},
                        new int[]{1, y, -1, 1}, new int[]{-1, y, -1, 1}
                    ));
                }
                // Escada central
                for (int y = 1; y <= 7; y++) {
                    structure.add(new int[]{0, y, 0, 2});
                }
                // Topo (y=7)
                structure.addAll(Arrays.asList(
                    new int[]{0, 7, 1, 1}, new int[]{1, 7, 1, 1}, new int[]{-1, 7, 1, 1},
                    new int[]{0, 7, -1, 1}, new int[]{1, 7, -1, 1}, new int[]{-1, 7, -1, 1},
                    new int[]{0, 7, 2, 1}, new int[]{0, 7, -2, 1}, new int[]{2, 7, 0, 1}, new int[]{-2, 7, 0, 1}
                ));
                break;

            case EAST:
                // Base (y=0)
                structure.addAll(Arrays.asList(
                    new int[]{0, 0, 0, 1}, new int[]{0, 0, 1, 1}, new int[]{0, 0, -1, 1},
                    new int[]{1, 0, 0, 1}, new int[]{1, 0, 1, 1}, new int[]{1, 0, -1, 1},
                    new int[]{-1, 0, 0, 1}, new int[]{-1, 0, 1, 1}, new int[]{-1, 0, -1, 1}
                ));
                // Paredes (y=1-6)
                for (int y = 1; y <= 6; y++) {
                    structure.addAll(Arrays.asList(
                        new int[]{1, y, 1, 1}, new int[]{1, y, -1, 1},
                        new int[]{-1, y, 1, 1}, new int[]{-1, y, -1, 1}
                    ));
                }
                // Escada central
                for (int y = 1; y <= 7; y++) {
                    structure.add(new int[]{0, y, 0, 2});
                }
                // Topo (y=7)
                structure.addAll(Arrays.asList(
                    new int[]{1, 7, 0, 1}, new int[]{1, 7, 1, 1}, new int[]{1, 7, -1, 1},
                    new int[]{-1, 7, 0, 1}, new int[]{-1, 7, 1, 1}, new int[]{-1, 7, -1, 1},
                    new int[]{2, 7, 0, 1}, new int[]{-2, 7, 0, 1}, new int[]{0, 7, 2, 1}, new int[]{0, 7, -2, 1}
                ));
                break;

            case WEST:
                // Base (y=0)
                structure.addAll(Arrays.asList(
                    new int[]{0, 0, 0, 1}, new int[]{0, 0, 1, 1}, new int[]{0, 0, -1, 1},
                    new int[]{1, 0, 0, 1}, new int[]{1, 0, 1, 1}, new int[]{1, 0, -1, 1},
                    new int[]{-1, 0, 0, 1}, new int[]{-1, 0, 1, 1}, new int[]{-1, 0, -1, 1}
                ));
                // Paredes (y=1-6)
                for (int y = 1; y <= 6; y++) {
                    structure.addAll(Arrays.asList(
                        new int[]{1, y, 1, 1}, new int[]{1, y, -1, 1},
                        new int[]{-1, y, 1, 1}, new int[]{-1, y, -1, 1}
                    ));
                }
                // Escada central
                for (int y = 1; y <= 7; y++) {
                    structure.add(new int[]{0, y, 0, 2});
                }
                // Topo (y=7)
                structure.addAll(Arrays.asList(
                    new int[]{1, 7, 0, 1}, new int[]{1, 7, 1, 1}, new int[]{1, 7, -1, 1},
                    new int[]{-1, 7, 0, 1}, new int[]{-1, 7, 1, 1}, new int[]{-1, 7, -1, 1},
                    new int[]{2, 7, 0, 1}, new int[]{-2, 7, 0, 1}, new int[]{0, 7, 2, 1}, new int[]{0, 7, -2, 1}
                ));
                break;
        }

        return structure;
    }
}
