package com.minecraft.savagemc.arcade.bedwars.listener;

import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ChestDepositListener implements Listener {

    private static final Map<Material, String> ITEM_TRANSLATIONS = new HashMap<>();

    static {
        // Blocos
        ITEM_TRANSLATIONS.put(Material.WOOL, "Lã");
        ITEM_TRANSLATIONS.put(Material.STAINED_CLAY, "Argila");
        ITEM_TRANSLATIONS.put(Material.HARD_CLAY, "Argila Endurecida");
        ITEM_TRANSLATIONS.put(Material.WOOD, "Madeira");
        ITEM_TRANSLATIONS.put(Material.ENDER_STONE, "Pedra do End");
        ITEM_TRANSLATIONS.put(Material.OBSIDIAN, "Obsidiana");
        ITEM_TRANSLATIONS.put(Material.GLASS, "Vidro");
        ITEM_TRANSLATIONS.put(Material.LADDER, "Escada");
        ITEM_TRANSLATIONS.put(Material.SANDSTONE, "Arenito");
        ITEM_TRANSLATIONS.put(Material.STONE, "Pedra");
        ITEM_TRANSLATIONS.put(Material.COBBLESTONE, "Pedregulho");
        
        // Ferramentas
        ITEM_TRANSLATIONS.put(Material.WOOD_PICKAXE, "Picareta de Madeira");
        ITEM_TRANSLATIONS.put(Material.STONE_PICKAXE, "Picareta de Pedra");
        ITEM_TRANSLATIONS.put(Material.IRON_PICKAXE, "Picareta de Ferro");
        ITEM_TRANSLATIONS.put(Material.DIAMOND_PICKAXE, "Picareta de Diamante");
        ITEM_TRANSLATIONS.put(Material.WOOD_AXE, "Machado de Madeira");
        ITEM_TRANSLATIONS.put(Material.STONE_AXE, "Machado de Pedra");
        ITEM_TRANSLATIONS.put(Material.IRON_AXE, "Machado de Ferro");
        ITEM_TRANSLATIONS.put(Material.DIAMOND_AXE, "Machado de Diamante");
        ITEM_TRANSLATIONS.put(Material.SHEARS, "Tesoura");
        
        // Armaduras
        ITEM_TRANSLATIONS.put(Material.LEATHER_BOOTS, "Botas de Couro");
        ITEM_TRANSLATIONS.put(Material.CHAINMAIL_BOOTS, "Botas de Cota de Malha");
        ITEM_TRANSLATIONS.put(Material.IRON_BOOTS, "Botas de Ferro");
        ITEM_TRANSLATIONS.put(Material.DIAMOND_BOOTS, "Botas de Diamante");
        ITEM_TRANSLATIONS.put(Material.GOLD_BOOTS, "Botas de Ouro");
        ITEM_TRANSLATIONS.put(Material.LEATHER_LEGGINGS, "Calças de Couro");
        ITEM_TRANSLATIONS.put(Material.CHAINMAIL_LEGGINGS, "Calças de Cota de Malha");
        ITEM_TRANSLATIONS.put(Material.IRON_LEGGINGS, "Calças de Ferro");
        ITEM_TRANSLATIONS.put(Material.DIAMOND_LEGGINGS, "Calças de Diamante");
        ITEM_TRANSLATIONS.put(Material.GOLD_LEGGINGS, "Calças de Ouro");
        ITEM_TRANSLATIONS.put(Material.LEATHER_CHESTPLATE, "Peitoral de Couro");
        ITEM_TRANSLATIONS.put(Material.CHAINMAIL_CHESTPLATE, "Peitoral de Cota de Malha");
        ITEM_TRANSLATIONS.put(Material.IRON_CHESTPLATE, "Peitoral de Ferro");
        ITEM_TRANSLATIONS.put(Material.DIAMOND_CHESTPLATE, "Peitoral de Diamante");
        ITEM_TRANSLATIONS.put(Material.GOLD_CHESTPLATE, "Peitoral de Ouro");
        ITEM_TRANSLATIONS.put(Material.LEATHER_HELMET, "Capacete de Couro");
        ITEM_TRANSLATIONS.put(Material.CHAINMAIL_HELMET, "Capacete de Cota de Malha");
        ITEM_TRANSLATIONS.put(Material.IRON_HELMET, "Capacete de Ferro");
        ITEM_TRANSLATIONS.put(Material.DIAMOND_HELMET, "Capacete de Diamante");
        ITEM_TRANSLATIONS.put(Material.GOLD_HELMET, "Capacete de Ouro");
        
        // Arco e flechas
        ITEM_TRANSLATIONS.put(Material.BOW, "Arco");
        ITEM_TRANSLATIONS.put(Material.ARROW, "Flecha");
        
        // Comida
        ITEM_TRANSLATIONS.put(Material.GOLDEN_APPLE, "Maçã Dourada");
        ITEM_TRANSLATIONS.put(Material.COOKED_BEEF, "Bife Cozido");
        ITEM_TRANSLATIONS.put(Material.BREAD, "Pão");
        ITEM_TRANSLATIONS.put(Material.APPLE, "Maçã");
        
        // Itens especiais
        ITEM_TRANSLATIONS.put(Material.FIREBALL, "Bola de Fogo");
        ITEM_TRANSLATIONS.put(Material.TNT, "TNT");
        ITEM_TRANSLATIONS.put(Material.ENDER_PEARL, "Pérola do End");
        ITEM_TRANSLATIONS.put(Material.WATER_BUCKET, "Balde de Água");
        ITEM_TRANSLATIONS.put(Material.POTION, "Poção");
        ITEM_TRANSLATIONS.put(Material.SNOW_BALL, "Bola de Neve");
        ITEM_TRANSLATIONS.put(Material.EGG, "Ovo");
        ITEM_TRANSLATIONS.put(Material.FISHING_ROD, "Vara de Pesca");
        ITEM_TRANSLATIONS.put(Material.WEB, "Teia");
        ITEM_TRANSLATIONS.put(Material.MONSTER_EGG, "Ovo Gerador");
        ITEM_TRANSLATIONS.put(Material.COMPASS, "Bússola");
        ITEM_TRANSLATIONS.put(Material.NETHER_STAR, "Blink");
        
        // Minérios
        ITEM_TRANSLATIONS.put(Material.IRON_INGOT, "Ferro");
        ITEM_TRANSLATIONS.put(Material.GOLD_INGOT, "Ouro");
        ITEM_TRANSLATIONS.put(Material.DIAMOND, "Diamante");
        ITEM_TRANSLATIONS.put(Material.EMERALD, "Esmeralda");
    }

    @EventHandler
    public void onChestInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        if (block == null || (block.getType() != Material.CHEST && block.getType() != Material.ENDER_CHEST)) return;
        
        User user = (User) User.of(player.getUniqueId());
        
        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;
        
        Arena arena = user.getArena();
        Team team = user.getTeam();
        
        if (team == null) return;
        
        // Para baús normais, verificar se está na ilha do time
        // Para baús do fim, permitir depositar em qualquer base
        if (block.getType() == Material.CHEST && !team.inIsland(block.getLocation())) {
            return;
        }
        
        ItemStack itemInHand = player.getItemInHand();
        
        // Clique esquerdo = depositar
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (itemInHand == null || itemInHand.getType() == Material.AIR) return;
            
            // Não permitir depositar espadas
            if (isSword(itemInHand.getType())) {
                player.sendMessage("§cVocê não pode depositar espadas!");
                return;
            }
            
            event.setCancelled(true);
            
            Inventory chestInventory;
            
            if (block.getType() == Material.ENDER_CHEST) {
                chestInventory = player.getEnderChest();
            } else {
                Chest chest = (Chest) block.getState();
                chestInventory = chest.getInventory();
            }
            
            // Adicionar item ao baú
            HashMap<Integer, ItemStack> leftover = chestInventory.addItem(itemInHand);
            
            if (leftover.isEmpty()) {
                // Todo o item foi depositado
                player.setItemInHand(null);
                
                String itemName = getItemName(itemInHand);
                int amount = itemInHand.getAmount();
                
                player.sendMessage("§eVocê depositou §a" + itemName + " §a(" + amount + "x) §eem seu baú.");
            } else {
                // Baú cheio ou parcialmente cheio
                ItemStack remaining = leftover.get(0);
                player.setItemInHand(remaining);
                
                int deposited = itemInHand.getAmount() - remaining.getAmount();
                
                if (deposited > 0) {
                    String itemName = getItemName(itemInHand);
                    player.sendMessage("§eVocê depositou §a" + itemName + " §a(" + deposited + "x) §eem seu baú.");
                    player.sendMessage("§cBaú cheio! §e" + remaining.getAmount() + "x §crestantes.");
                } else {
                    player.sendMessage("§cBaú cheio!");
                }
            }
        }
        // Clique direito = abrir baú normalmente
    }
    
    private boolean isSword(Material material) {
        return material == Material.WOOD_SWORD || 
               material == Material.STONE_SWORD || 
               material == Material.IRON_SWORD || 
               material == Material.DIAMOND_SWORD ||
               material == Material.GOLD_SWORD;
    }
    
    private String getItemName(ItemStack item) {
        String translation = ITEM_TRANSLATIONS.get(item.getType());
        
        if (translation != null) {
            return translation;
        }
        
        // Fallback para nome em inglês formatado
        String name = item.getType().name().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}
