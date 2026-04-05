package com.minecraft.savagemc.hungergames.game.structure;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public class BonusFeast extends Feast implements BukkitInterface {

    public BonusFeast(HungerGames hungerGames) {
        super(hungerGames, -1);
    }

    @Override
    public ItemStack[] getStacks() {
        return new ItemStack[]{new ItemStack(Material.DIAMOND_HELMET), new ItemStack(Material.DIAMOND_BOOTS), new ItemStack(Material.DIAMOND_SWORD),
                new ItemStack(Material.DIAMOND_PICKAXE), new ItemStack(Material.DIAMOND_PICKAXE), new ItemStack(Material.DIAMOND_PICKAXE), new ItemStack(Material.DIAMOND_AXE),
                new ItemStack(Material.COOKED_BEEF, random(37)), new ItemStack(Material.COOKED_BEEF,
                random(37)), new ItemStack(Material.COOKED_BEEF, random(37)), new ItemStack(Material.FLINT_AND_STEEL),
                new ItemStack(Material.WATER_BUCKET), new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.WATER_BUCKET), new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.WATER_BUCKET), new ItemStack(Material.LAVA_BUCKET),
                new ItemStack(Material.ENDER_PEARL, random(2)), new ItemStack(Material.GOLDEN_APPLE, random(12)), new ItemStack(Material.GOLDEN_APPLE, random(12)),
                new ItemStack(Material.GOLDEN_APPLE, random(12)), new ItemStack(Material.GOLDEN_APPLE, random(12)),
                new ItemStack(Material.EXP_BOTTLE, random(12)), new ItemStack(Material.WEB, random(9)), new ItemStack(Material.WEB, random(9)),
                new ItemStack(Material.TNT, random(16)), new ItemStack(Material.POTION, 1, (short) 16418),
                new ItemStack(Material.POTION, 1, (short) 16424), new ItemStack(Material.POTION, 1, (short) 16420),
                new ItemStack(Material.POTION, 1, (short) 16428), new ItemStack(Material.POTION, 1, (short) 16426),
                new ItemStack(Material.POTION, 1, (short) 16417), new ItemStack(Material.POTION, 1, (short) 16419),
                new ItemStack(Material.POTION, 1, (short) 16421), new ItemStack(Material.WEB, random(3)),
                new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.COOKED_CHICKEN, random(7)), new ItemStack(Material.COOKED_CHICKEN, random(7)), new ItemStack(Material.COOKED_CHICKEN, random(7)),
                new ItemStack(Material.MUSHROOM_SOUP, random(12)), new ItemStack(Material.MUSHROOM_SOUP, random(12))};
    }

    @Override
    public void spawn(boolean silent) {
        super.spawn(true);
        System.out.println("X: " + getLocation().getX() + " Z:" + getLocation().getZ());
        broadcast("hg.bonus_feast.spawned_broadcast");
        getHungerGames().getKitStorage().getKits().stream().filter(c -> c.getKitCategory() == KitCategory.MOVEMENT).forEach(kit -> kit.setActive(false, true));
    }

    public Feast parseLocation(int min, int max) {

        int x = randomize(min, max), z = randomize(min, max);
        int y = HungerGames.getInstance().getGame().getWorld().getHighestBlockYAt(x, z);

        // Ajustar para nascer algumas camadas abaixo do terreno (4 blocos)
        int undergroundOffset = 4; // 4 blocos abaixo da superfície
        Location location = new Location(HungerGames.getInstance().getGame().getWorld(), x, y - undergroundOffset, z);
        
        // Verificar se não ficou muito baixo (mínimo Y=10 para evitar bedrock)
        if (location.getY() < 10)
            location.setY(10);
        
        // Se estava muito alto, ajustar para uma altura mais baixa
        if (y >= 90)
            location.setY(72 - undergroundOffset);

        setLocation(location);
        if (!getLocation().getChunk().isLoaded())
            getLocation().getChunk().load();
        return this;
    }

    @Override
    public void fill(Chest chest, int frequency) {
        super.fill(chest, 12);
    }
}
