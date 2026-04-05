package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Nevasca extends BedDestroyCollectible {

    public Nevasca() {
        super(CollectibleRarity.EPIC, "Nevasca", Item.of(Material.SNOW_BALL), 1729792699944L);

        setLore("§7Uma nevasca congelante!");
        setRanks(RankType.SAVAGE);
    }

    @Override
    public void execute(Location location) {
        Location baseLocation = location.clone();
        int radius = 2;
        Random rand = new Random();

        // Colocar blocos de neve no chão (permanentemente)
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location groundLocation = baseLocation.clone().add(x, -1, z);

                if (rand.nextDouble() < 0.5) {
                    if (groundLocation.getBlock().getType() != Material.AIR 
                        && groundLocation.getBlock().getType() != Material.SNOW_BLOCK) {
                        groundLocation.getBlock().setType(Material.SNOW_BLOCK);
                    }
                }
            }
        }

        // Lançar bolas de neve
        for (int i = 0; i < 5; i++) {
            Vector velocity = new Vector(Math.random() - 0.5, 1, Math.random() - 0.5);
            location.getWorld().spawn(location.clone().add(0, 1, 0), Snowball.class).setVelocity(velocity);
        }

        // Efeitos sonoros e visuais
        Location effectLoc = baseLocation.clone();
        for (int y = 0; y <= 2; y++) {
            Location loc = effectLoc.clone().add(0, y, 0);
            loc.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.SNOW_BLOCK);
        }
        
        // Blocos de neve agora permanecem até o fim da partida
    }
}
