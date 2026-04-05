package com.minecraft.savagemc.core.util.list.bukkit;

import com.minecraft.savagemc.core.Savage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BukkitUtil {

    private static final List<Material> RECRAFT_ITEMS = Arrays.asList(Material.MUSHROOM_SOUP, Material.BOWL, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM);

    public static boolean isRecraft(Material material) {
        return RECRAFT_ITEMS.contains(material);
    }

    public static void updateLevelBar(Player player, long endTime, int duration) {
        int timeLeft = (int) (endTime - System.currentTimeMillis());
        float percentLeft = (float) timeLeft / TimeUnit.SECONDS.toMillis(duration);

        player.setExp(percentLeft);
        player.setLevel(timeLeft / 1_000);
    }

    public static void removeItemInHand(Player player) {
        Savage.getPlatform().runSync(() -> player.setItemInHand(null), 1L);
    }

    public static void consumeItem(Player player) {
        ItemStack item = player.getItemInHand();

        int amount = item.getAmount();

        if (item.getAmount() == 1)
            player.getInventory().removeItem(item);
        else
            item.setAmount(amount - 1);

        player.updateInventory();
    }

    public static boolean isInventoryEmpty(Player player) {
        // Verifica o inventário do jogador
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return false; // Encontrou um item que não é null ou AIR, então o inventário não está vazio
            }
        }

        // Verifica o inventário da armadura do jogador
        return isNotUsingArmors(player); // Todos os slots estão vazios
    }

    public static boolean isInventoryFull(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.isType(Material.AIR))
                return false;
        }

        return true;
    }

    public static boolean hasWaterInLocation(Location location, int radius) {
        World world = location.getWorld();

        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    Block block = world.getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);

                    if (block.getType().name().contains("WATER"))
                        return true;
                }
            }
        }

        return false;
    }

    public static boolean isSimilarStack(ItemStack source, ItemStack copy) {
        if (source == null || source.getType().equals(Material.AIR) || copy == null || copy.getType().equals(Material.AIR))
            return false;

        ItemMeta sourceMeta = source.getItemMeta();
        ItemMeta copyMeta = copy.getItemMeta();
        
        String sourceDisplayName = sourceMeta != null ? sourceMeta.getDisplayName() : null;
        String copyDisplayName = copyMeta != null ? copyMeta.getDisplayName() : null;
        
        // Se ambos os displayNames são null, considera iguais
        if (sourceDisplayName == null && copyDisplayName == null) {
            return source.getType().equals(copy.getType());
        }
        
        // Se um é null e outro não, são diferentes
        if (sourceDisplayName == null || copyDisplayName == null) {
            return false;
        }
        
        return sourceDisplayName.equals(copyDisplayName) && source.getType().equals(copy.getType());
    }

    public static boolean isNotUsingArmors(Player player) {
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }

        return true;
    }

    public static int getItemAmount(Player player, Material material) {
        PlayerInventory inventory = player.getInventory();

        int amount = 0;

        for (ItemStack content : inventory.getContents()) {
            if (content != null && content.getType().equals(material))
                amount += content.getAmount();
        }

        return amount;
    }

    public static ItemStack getItemByType(Player player, Material material) {
        PlayerInventory inv = player.getInventory();

        for (ItemStack content : inv.getContents()) {
            if (content != null && content.isType(material))
                return content;
        }

        return null;
    }

    public static void removeItemByType(Player player, Material material) {
        PlayerInventory inventory = player.getInventory();

        for (ItemStack content : inventory.getContents()) {
            if (content != null && content.getType().equals(material)) {
                inventory.remove(content);
            }
        }
    }

    public static void removeItemByAmount(Player player, Material material, int amount) {
        PlayerInventory inventory = player.getInventory();

        if (getItemAmount(player, material) < amount) return;

        int remaining = amount;

        for (ItemStack content : inventory.getContents()) {
            if (content != null && content.getType().equals(material)) {
                int stackAmount = content.getAmount();

                if (stackAmount > remaining) {
                    // Se a pilha atual tem mais do que o necessário, apenas subtraia o necessário
                    content.setAmount(stackAmount - remaining);
                    break;
                } else {
                    // Se a pilha atual tem menos ou igual ao necessário, remova a pilha inteira
                    inventory.remove(content);
                    remaining -= stackAmount;

                    // Se removemos todos os itens necessários, saia do loop
                    if (remaining <= 0) break;
                }
            }
        }
    }

    public static void replaceItemByType(Player player, Material material, ItemStack replaced) {
        PlayerInventory inv = player.getInventory();

        int slot = 0;
        for (ItemStack content : inv.getContents()) {
            if (content != null && content.getType().equals(material)) {
                inv.setItem(slot, replaced);
                break;
            } else if (content == null || content.getType().equals(Material.AIR)) {
                inv.setItem(slot, replaced);
                break;
            }

            slot++;
        }
    }

    public static void enchantItemByType(Player player, EquipmentType type, Enchantment enchantment, int level) {
        PlayerInventory inv = player.getInventory();

        switch (type) {
            case SWORD:
            case AXE:
                enchantWeapons(inv, type, enchantment, level);
                break;
            case ARMOR:
                enchantArmor(inv, enchantment, level);
                break;
        }
    }

    private static void enchantWeapons(PlayerInventory inv, EquipmentType type, Enchantment enchantment, int level) {
        for (ItemStack content : inv.getContents()) {
            if (content == null || content.getType() == Material.AIR) continue;

            if ((type == EquipmentType.SWORD && content.getType().name().endsWith("_SWORD")) ||
                    (type == EquipmentType.AXE && content.getType().name().endsWith("_AXE"))) {
                // Remover encantamento existente e adicionar o novo nível
                content.removeEnchantment(enchantment);
                content.addUnsafeEnchantment(enchantment, level);
            }
        }
    }

    private static void enchantArmor(PlayerInventory inv, Enchantment enchantment, int level) {
        for (ItemStack content : inv.getArmorContents()) {
            if (content != null && content.getType() != Material.AIR) {
                // Remover encantamento existente e adicionar o novo nível
                content.removeEnchantment(enchantment);
                content.addUnsafeEnchantment(enchantment, level);
            }
        }
    }

    public enum EquipmentType {
        ARMOR, SWORD, AXE
    }

    public static boolean hasItemInInventory(Player player, Material material) {
        PlayerInventory inv = player.getInventory();

        for (ItemStack content : inv.getContents()) {
            if (content != null && content.getType().equals(material))
                return true;
        }

        return false;
    }

    public static void removeCommand(JavaPlugin plugin, String... commands) {
        try {
            Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer());

            Field field = commandMap.getClass().getDeclaredField("knownCommands");

            field.setAccessible(true);

            Map<String, Command> knownCommands = (HashMap<String, Command>) field.get(commandMap);

            for (String command : commands) {

                if (knownCommands.containsKey(command)) {

                    knownCommands.remove(command);

                    List<String> aliases = new ArrayList<>();

                    for (String key : knownCommands.keySet()) {
                        if (!key.contains(":"))
                            continue;

                        String substr = key.substring(key.indexOf(":") + 1);

                        if (substr.equalsIgnoreCase(command)) {
                            aliases.add(key);
                        }
                    }

                    for (String alias : aliases) {
                        knownCommands.remove(alias);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendRecraft(Player player) {
        sendRecraft(player, 64);
    }

    public static void sendRecraft(Player player, int amount) {
        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < 36; i++)
            inv.setItem(i, new ItemStack(Material.MUSHROOM_SOUP));

        inv.setItem(13, new ItemStack(Material.BOWL, amount));
        inv.setItem(14, new ItemStack(Material.RED_MUSHROOM, amount));
        inv.setItem(15, new ItemStack(Material.BROWN_MUSHROOM, amount));
    }

    public static void sendLeatherArmor(Player player, Color color) {
        PlayerInventory inv = player.getInventory();

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();

        helmetMeta.setColor(color);
        helmetMeta.spigot().setUnbreakable(true);

        helmetMeta.setLore(Collections.emptyList());

        helmet.setItemMeta(helmetMeta);

        LeatherArmorMeta chestPlateMeta = (LeatherArmorMeta) chestplate.getItemMeta();

        chestPlateMeta.setColor(color);
        chestPlateMeta.spigot().setUnbreakable(true);

        chestPlateMeta.setLore(Collections.emptyList());

        chestplate.setItemMeta(chestPlateMeta);

        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();

        leggingsMeta.setColor(color);
        leggingsMeta.spigot().setUnbreakable(true);

        leggingsMeta.setLore(Collections.emptyList());

        leggings.setItemMeta(leggingsMeta);

        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();

        bootsMeta.setColor(color);
        bootsMeta.spigot().setUnbreakable(true);

        bootsMeta.setLore(Collections.emptyList());

        boots.setItemMeta(bootsMeta);

        inv.setHelmet(helmet);
        inv.setChestplate(chestplate);
        inv.setLeggings(leggings);
        inv.setBoots(boots);
    }

    public static void launchFirework(Location location) {
        Firework firework = location.getWorld().spawn(location, Firework.class);

        FireworkMeta meta = firework.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
                .withColor(Color.RED)
                .withFade(Color.YELLOW)
                .with(FireworkEffect.Type.BURST)
                .withFlicker()
                .withTrail()
                .build();

        meta.addEffect(effect);
        firework.setFireworkMeta(meta);

        //firework.detonate();
    }

    public static void sendArmor(Player player, ArmorType type) {
        PlayerInventory inv = player.getInventory();

        inv.setHelmet(new ItemStack(Material.getMaterial(type.name().toUpperCase() + "_HELMET")));
        inv.setChestplate(new ItemStack(Material.getMaterial(type.name().toUpperCase() + "_CHESTPLATE")));
        inv.setLeggings(new ItemStack(Material.getMaterial(type.name().toUpperCase() + "_LEGGINGS")));
        inv.setBoots(new ItemStack(Material.getMaterial(type.name().toUpperCase() + "_BOOTS")));
    }

    public static void sendArmor(Player player, ArmorType type, Enchantment enchantment) {
        PlayerInventory inv = player.getInventory();

        String name = type.name().toUpperCase();

        ItemStack helmet = new ItemStack(Material.getMaterial(name + "_HELMET"));
        helmet.addEnchantment(enchantment, 1);

        ItemStack chestplate = new ItemStack(Material.getMaterial(name + "_CHESTPLATE"));
        chestplate.addEnchantment(enchantment, 1);

        ItemStack leggings = new ItemStack(Material.getMaterial(name + "_LEGGINGS"));
        leggings.addEnchantment(enchantment, 1);

        ItemStack boots = new ItemStack(Material.getMaterial(name + "_BOOTS"));
        boots.addEnchantment(enchantment, 1);

        inv.setHelmet(helmet);
        inv.setChestplate(chestplate);
        inv.setLeggings(leggings);
        inv.setBoots(boots);
    }

    public enum ArmorType {
        LEATHER, IRON, GOLD, DIAMOND
    }
}
