/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.game.structure;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.game.Game;
import com.minecraft.savagemc.hungergames.game.list.ClanxClan;
import com.minecraft.savagemc.hungergames.util.arena.FileArena;
import com.minecraft.savagemc.hungergames.util.bo3.BO3;
import com.minecraft.savagemc.hungergames.util.bo3.EmptyBO3;
import com.minecraft.savagemc.hungergames.util.metadata.GameMetadata;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Feast implements BukkitInterface {

    private final HungerGames hungerGames;
    private Pattern[] pattern;
    private boolean spawned, filled;
    private int time;
    private BO3 bo3;
    private Location location;
    private List<Chest> chests = new ArrayList<>();
    private List<Block> blocks = new ArrayList<>();

    public Feast(HungerGames hungerGames, int time) {
        this.hungerGames = hungerGames;
        this.spawned = false;
        this.filled = false;
        this.time = time;
        FileArena fileArena = FileArena.getArena("feast");

        if (fileArena == null) {
            File dir = new File("Savage/hg/structures/feast");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileArena.load(dir);
            fileArena = FileArena.getArena("feast");
        }

        this.bo3 = fileArena.getBO3();
    }

    public Feast countdown() {
        build();
        new BukkitRunnable() {
            public void run() {

                if (getHungerGames().getGame().getRecoveryMode().isEnabled())
                    return;

                if (getTime() > 0) {
                    if (time != 0 && time % 5 == 0 && time <= 15 || time != 0 && time % 30 == 0 || time != 0 && time <= 5)
                        broadcastTime();
                    // Kit Expel foi removido - funcionalidade desativada
                    setTime(getTime() - 1);
                } else {
                    spawn(false);
                    cancel();
                }
            }
        }.runTaskTimer(getHungerGames(), 0L, 20L);
        return this;
    }

    public Feast findLocation(int range) {
        int x = randomize(range), z = randomize(range);

        if (absolute(x) < 80) {
            if (x > 0)
                x += 40;
            else
                x -= 40;
        }

        if (absolute(z) < 80) {
            if (z > 0)
                z += 40;
            else
                z -= 40;
        }

        // Encontra uma área completamente limpa para o feast
        Location bestLocation = findOptimalFeastLocation(x, z);
        setLocation(bestLocation);

        if (!getLocation().getChunk().isLoaded())
            getLocation().getChunk().load();
        return this;
    }

    /**
     * Encontra a localização ótima para o feast, garantindo área limpa e posição elevada
     */
    private Location findOptimalFeastLocation(int x, int z) {
        World world = HungerGames.getInstance().getGame().getWorld();
        Location bestLocation = null;
        int bestScore = Integer.MAX_VALUE;
        int maxAttempts = 50; // Máximo de tentativas para encontrar local ideal

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Gera coordenadas aleatórias dentro do range
            int testX = x + (int) (Math.random() * 20) - 10;
            int testZ = z + (int) (Math.random() * 20) - 10;

            int groundY = world.getHighestBlockYAt(testX, testZ);
            Location testLocation = new Location(world, testX, groundY + 3, testZ); // +3 para ficar mais alto

            // Ajusta altura se muito alta
            if (testLocation.getY() >= 90)
                testLocation.setY(75); // Altura fixa mais alta

            // Verifica se a área está suficientemente limpa
            if (isAreaCleanForFeast(testLocation)) {
                int score = calculateAreaCleanliness(testLocation);
                if (score < bestScore) {
                    bestScore = score;
                    bestLocation = testLocation;
                }
            }
        }

        // Se não encontrou local ideal, usa o melhor disponível e limpa a área
        if (bestLocation == null) {
            int groundY = world.getHighestBlockYAt(x, z);
            bestLocation = new Location(world, x, groundY + 3, z);
            if (bestLocation.getY() >= 90)
                bestLocation.setY(75);
        }

        // Limpa a área antes de retornar
        clearAreaForFeast(bestLocation);
        return bestLocation;
    }

    /**
     * Verifica se uma área está limpa o suficiente para o feast
     */
    private boolean isAreaCleanForFeast(Location center) {
        int radius = 8; // Raio maior para verificar limpeza
        int maxObstacles = 10; // Máximo de obstáculos permitidos

        int obstacleCount = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location checkLocation = center.clone().add(x, 0, z);
                int groundY = center.getWorld().getHighestBlockYAt(checkLocation.getBlockX(), checkLocation.getBlockZ());

                // Verifica blocos sólidos na área do feast
                for (int y = groundY + 1; y <= center.getY() + 10; y++) {
                    Location blockLocation = checkLocation.clone();
                    blockLocation.setY(y);
                    Block block = blockLocation.getBlock();

                    if (block.getType() != Material.AIR &&
                            block.getType() != Material.BEDROCK &&
                            !block.hasMetadata("unbreakable") &&
                            !block.hasMetadata("kit.lumberjack.ignore")) {
                        obstacleCount++;
                        if (obstacleCount > maxObstacles) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Calcula quão "limpa" é uma área baseado na quantidade de blocos sólidos acima do nível do chão
     */
    private int calculateAreaCleanliness(Location center) {
        int score = 0;
        int radius = 8; // Raio maior para verificar a limpeza

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location checkLocation = center.clone().add(x, 0, z);
                int groundY = center.getWorld().getHighestBlockYAt(checkLocation.getBlockX(), checkLocation.getBlockZ());

                // Conta blocos sólidos acima do nível do chão
                for (int y = groundY + 1; y <= center.getY() + 15; y++) {
                    Location blockLocation = checkLocation.clone();
                    blockLocation.setY(y);
                    Block block = blockLocation.getBlock();

                    if (block.getType() != Material.AIR &&
                            block.getType() != Material.BEDROCK &&
                            !block.hasMetadata("unbreakable") &&
                            !block.hasMetadata("kit.lumberjack.ignore")) {
                        score++;
                    }
                }
            }
        }

        return score;
    }

    /**
     * Limpa completamente a área onde o feast será construído
     */
    private void clearAreaForFeast(Location center) {
        int radius = 10; // Raio maior para limpeza
        World world = center.getWorld();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y <= 30; y++) { // Limpa até 30 blocos acima
                    Location blockLocation = center.clone().add(x, y, z);
                    Block block = blockLocation.getBlock();

                    // Remove todos os blocos que não sejam bedrock ou especiais
                    if (block.getType() != Material.AIR &&
                            block.getType() != Material.BEDROCK &&
                            !block.hasMetadata("unbreakable") &&
                            !block.hasMetadata("kit.lumberjack.ignore")) {

                        // Remove o bloco
                        WorldEditAPI.getInstance().setBlock(blockLocation, Material.AIR, (byte) 0);
                    }
                }
            }
        }
    }

    public void spawn(boolean silent) {
        Game game = hungerGames.getGame();

        getLocation().clone().add(0, 1, 0).getBlock().setType(Material.ENCHANTMENT_TABLE);
        getLocation().getWorld().strikeLightningEffect(getLocation());

        if (game.getName() == "Legion") {
            makeChests().forEach(c -> fill(c, 12));
        } else {
            makeChests().forEach(c -> fill(c, 18));
        }

        getBlocks().forEach(block -> {
            block.removeMetadata("unbreakable", getHungerGames());
            block.setMetadata("kit.lumberjack.ignore", new GameMetadata(true));
        });

        if (!silent)
            broadcast("hg.feast.spawned_broadcast");

        this.filled = true;

        if (game instanceof ClanxClan && !game.isDamage())
            game.setDamage(true);
    }

    public void fill(Chest chest, int frequency) {
        for (ItemStack stack : getStacks()) {
            if (Constants.RANDOM.nextInt(100) < frequency)
                chest.getInventory().setItem(Constants.RANDOM.nextInt(27), stack);
        }
        chest.update();
    }

    public Feast build() {
        if (location == null)
            throw new IllegalStateException("Feast location can not be null.");
        if (bo3 instanceof EmptyBO3)
            throw new IllegalStateException("BO3 can not be empty.");

        // Primeiro, limpa todos os blocos acima da área do feast antes de construir
        clearAreaAboveFeast();

        bo3.spawn(location, (location, pattern) -> {

            Block block = location.getBlock();

            if (pattern.getMaterial() == Material.LAPIS_BLOCK) {

                block.setType(Material.AIR);

                for (int i = 0; i < 100; i++) {
                    block = block.getRelative(BlockFace.UP);

                    if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK)
                        WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.AIR, (byte) 0);
                }

                return false;
            } else if (pattern.getMaterial() == Material.GRASS && block.getBiome() == Biome.DESERT) {
                pattern.setMaterial(Material.SAND);
            }

            if (pattern.getMaterial() != Material.AIR) {
                blocks.add(block);
                block.setMetadata("unbreakable", new GameMetadata(0));
            }

            return true;
        });

        this.bo3 = new EmptyBO3();
        this.spawned = true;
        return this;
    }

    /**
     * Limpa todos os blocos acima da área do feast para evitar que árvores, blocos ou outras estruturas
     * fiquem por cima da estrutura do feast
     */
    private void clearAreaAboveFeast() {
        // Define a área do feast (assumindo que é aproximadamente 15x15)
        int feastRadius = 7; // Raio de 7 = área de 15x15
        int clearHeight = 50; // Altura máxima para limpar acima do feast

        Location center = getLocation();

        // Limpa uma área maior que o feast para garantir que nada fique por cima
        for (int x = -feastRadius; x <= feastRadius; x++) {
            for (int z = -feastRadius; z <= feastRadius; z++) {
                Location blockLocation = center.clone().add(x, 0, z);

                // Encontra o bloco mais alto nesta posição
                int highestY = center.getWorld().getHighestBlockYAt(blockLocation.getBlockX(), blockLocation.getBlockZ());

                // Limpa todos os blocos acima do nível do feast até uma altura segura
                for (int y = center.getBlockY() + 1; y <= highestY + clearHeight; y++) {
                    Location clearLocation = blockLocation.clone();
                    clearLocation.setY(y);
                    Block block = clearLocation.getBlock();

                    // Remove apenas blocos que não são bedrock e não são parte de estruturas importantes
                    if (block.getType() != Material.AIR &&
                            block.getType() != Material.BEDROCK &&
                            !block.hasMetadata("unbreakable") &&
                            !block.hasMetadata("kit.lumberjack.ignore")) {

                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }

    protected void broadcastTime() {
        String location = "(" + (int) getLocation().getX() + ", " + (int) getLocation().getY() + ", " + (int) getLocation().getZ() + ")";
        String br = DateUtils.formatTime(Language.PORTUGUESE, getTime()), us = DateUtils.formatTime(Language.ENGLISH, getTime());
        for (Player player : Bukkit.getOnlinePlayers()) {
            Account account = Account.fetch(player.getUniqueId());
            if (account == null)
                continue;
            player.sendMessage(account.getLanguage().translate("hg.feast.spawn_broadcast", location, (account.getLanguage() == Language.PORTUGUESE ? br : us)));
        }
    }

    protected List<Chest> makeChests() {
        List<Chest> chests = new ArrayList<>();
        Location[] locations = {getLocation().clone().add(1, 1, 1), getLocation().clone().add(-1, 1, -1),
                getLocation().clone().add(1, 1, -1), getLocation().clone().add(-1, 1, 1), getLocation().clone().add(-2, 1, 2),
                getLocation().clone().add(-2, 1, -2), getLocation().clone().add(-2, 1, 0), getLocation().clone().add(2, 1, 0),
                getLocation().clone().add(0, 1, -2), getLocation().clone().add(0, 1, 2), getLocation().clone().add(+2, 1, -2),
                getLocation().clone().add(2, 1, 2)};
        for (Location location : locations) {
            location.getBlock().setType(Material.CHEST);
            chests.add((Chest) location.getBlock().getState());
        }
        return chests;
    }

    int random(int i) {
        return Math.max(1, Constants.RANDOM.nextInt(i + 1));
    }

    private Pattern getPattern() {
        return pattern[Constants.RANDOM.nextInt(pattern.length)];
    }

    public ItemStack[] getStacks() {
        Game game = hungerGames.getGame();

        if (game.getName() == "Legion") {
            return new ItemStack[]{
                    new ItemFactory(Material.ENCHANTED_BOOK).addStoredEnchantment(Enchantment.DAMAGE_ALL, 1).getStack(),
                    new ItemFactory(Material.ENCHANTED_BOOK).addStoredEnchantment(Enchantment.FIRE_ASPECT, 1).getStack(),
                    new ItemStack(Material.IRON_HELMET), new ItemFactory(Material.ANVIL).setDurability(1).getStack(),
                    new ItemStack(Material.DIAMOND_CHESTPLATE), new ItemStack(Material.DIAMOND_LEGGINGS),
                    new ItemStack(Material.DIAMOND_BOOTS), new ItemStack(Material.DIAMOND_SWORD),
                    new ItemStack(Material.DIAMOND_PICKAXE), new ItemStack(Material.DIAMOND_AXE),
                    new ItemStack(Material.COOKED_BEEF, random(37)), new ItemStack(Material.FLINT_AND_STEEL),
                    new ItemStack(Material.WATER_BUCKET), new ItemStack(Material.LAVA_BUCKET),
                    new ItemStack(Material.ENDER_PEARL, random(2)), new ItemStack(Material.GOLDEN_APPLE, random(12)),
                    new ItemStack(Material.EXP_BOTTLE, random(12)), new ItemStack(Material.WEB, random(9)),
                    new ItemStack(Material.TNT, random(16)), new ItemStack(Material.POTION, 1, (short) 16418),
                    new ItemStack(Material.POTION, 1, (short) 16424), new ItemStack(Material.POTION, 1, (short) 16420),
                    new ItemStack(Material.POTION, 1, (short) 16428), new ItemStack(Material.POTION, 1, (short) 16426),
                    new ItemStack(Material.POTION, 1, (short) 16417), new ItemStack(Material.POTION, 1, (short) 16419),
                    new ItemStack(Material.POTION, 1, (short) 16421), new ItemStack(Material.WEB, random(3)),
                    new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.COOKED_CHICKEN, random(7)), new ItemStack(Material.MUSHROOM_SOUP, random(12))
            };
        } else {
            return new ItemStack[]{
                    new ItemFactory(Material.ENCHANTED_BOOK).addStoredEnchantment(Enchantment.DAMAGE_ALL, 1).getStack(),
                    new ItemFactory(Material.ENCHANTED_BOOK).addStoredEnchantment(Enchantment.FIRE_ASPECT, 1).getStack(),
                    new ItemStack(Material.DIAMOND_HELMET), new ItemFactory(Material.ANVIL).setDurability(1).getStack(),
                    new ItemStack(Material.DIAMOND_CHESTPLATE), new ItemStack(Material.DIAMOND_LEGGINGS),
                    new ItemStack(Material.DIAMOND_BOOTS), new ItemStack(Material.DIAMOND_SWORD),
                    new ItemStack(Material.DIAMOND_PICKAXE), new ItemStack(Material.DIAMOND_AXE),
                    new ItemStack(Material.COOKED_BEEF, random(37)), new ItemStack(Material.FLINT_AND_STEEL),
                    new ItemStack(Material.WATER_BUCKET), new ItemStack(Material.LAVA_BUCKET),
                    new ItemStack(Material.ENDER_PEARL, random(2)), new ItemStack(Material.GOLDEN_APPLE, random(12)),
                    new ItemStack(Material.EXP_BOTTLE, random(12)), new ItemStack(Material.WEB, random(9)),
                    new ItemStack(Material.TNT, random(16)), new ItemStack(Material.POTION, 1, (short) 16418),
                    new ItemStack(Material.POTION, 1, (short) 16424), new ItemStack(Material.POTION, 1, (short) 16420),
                    new ItemStack(Material.POTION, 1, (short) 16428), new ItemStack(Material.POTION, 1, (short) 16426),
                    new ItemStack(Material.POTION, 1, (short) 16417), new ItemStack(Material.POTION, 1, (short) 16419),
                    new ItemStack(Material.POTION, 1, (short) 16421), new ItemStack(Material.WEB, random(3)),
                    new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.COOKED_CHICKEN, random(7)), new ItemStack(Material.MUSHROOM_SOUP, random(12))
            };
        }
    }
}
