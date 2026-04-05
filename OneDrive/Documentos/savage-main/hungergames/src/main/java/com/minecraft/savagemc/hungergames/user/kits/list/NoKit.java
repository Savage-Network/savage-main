package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.savagemc.hungergames.HungerGamesPlugin;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import lombok.Getter;

/**
 * Kit padrão (sem kit)
 * Usado quando um jogador não seleciona nenhum kit
 */
@Getter
public class NoKit extends Kit {
    
    public NoKit(HungerGamesPlugin plugin) {
        super(plugin);
        setDisplayName("Nenhum");
        setDescription("Jogue sem nenhuma habilidade especial");
        setIcon(org.bukkit.Material.BARRIER);
        setPrice(0);
        setEnabled(true);
    }
    
    @Override
    public void giveItems(org.bukkit.entity.Player player) {
        // Não dá itens para o kit "Nenhum"
    }
    
    @Override
    public void removeItems(org.bukkit.entity.Player player) {
        // Não há itens para remover
    }
    
    @Override
    public boolean canUse(org.bukkit.entity.Player player) {
        // Todos podem usar o kit "Nenhum"
        return true;
    }
    
    @Override
    public boolean isKitItem(org.bukkit.inventory.ItemStack item) {
        // Kit "Nenhum" não tem itens
        return false;
    }
}
