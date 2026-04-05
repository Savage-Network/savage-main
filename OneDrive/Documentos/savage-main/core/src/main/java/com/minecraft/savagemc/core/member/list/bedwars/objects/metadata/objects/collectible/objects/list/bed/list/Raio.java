package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LightningStrike;

public class Raio extends BedDestroyCollectible {

    public Raio() {
        super(CollectibleRarity.MYTHICAL, "Raio", Item.of(Material.BLAZE_ROD), 1729792699944L);

        setLore("§7Um raio devastador!");
        setRanks(RankType.BETA);
    }

    @Override
    public void execute(Location location) {
        Location baseLocation = location.clone();
        
        // Invocar raio sem causar dano
        LightningStrike lightning = baseLocation.getWorld().strikeLightningEffect(baseLocation);
        
        // Nota: strikeLightningEffect não causa dano aos jogadores
        // Se quiser o raio visual com som mas sem dano, use strikeLightningEffect
        // Se quiser o raio real mas sem dano aos jogadores, seria necessário um listener
    }
}
