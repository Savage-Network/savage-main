package com.minecraft.savagemc.hungergames.util.bo3;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import org.bukkit.Material;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class BO3Reader {

    private final File file;

    public BO3Reader(File file) {
        this.file = file;
    }

    public BO3 read() {

        BO3 bo3 = new BO3();

        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();

                if (line.startsWith("#")) /* Ignoring file comments */
                    continue;

                if (line.startsWith("hide();")) {
                    bo3.setHidden(true);
                    continue;
                }

                if (line.startsWith("Block(")) {
                    line = line.substring("Block(".length(), line.length() - 1);

                    String[] arr = line.split(",");
                    int x = Integer.parseInt(arr[0]);
                    int y = Integer.parseInt(arr[1]);
                    int z = Integer.parseInt(arr[2]);

                    String rawMats = arr[3];

                    Material material;
                    byte data = 0;

                    if (rawMats.contains(":")) {
                        String[] mats = arr[3].split(":");
                        material = getMaterialFromString(mats[0]);
                        if (material == null) {
                            continue; // Pula se não conseguir converter
                        }
                        data = Byte.parseByte(mats[1]);
                    } else {
                        material = getMaterialFromString(rawMats);
                        if (material == null) {
                            continue; // Pula se não conseguir converter
                        }
                    }
                    bo3.addBlock(new BO3Block(x, y, z, Pattern.of(material, data)));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new EmptyBO3();
        }
        return bo3;
    }

    private static Material getMaterialFromString(String materialString) {
        try {
            // Tenta converter diretamente
            return Material.valueOf(materialString);
        } catch (IllegalArgumentException e) {
            // Se falhar, tenta converter ID numérico antigo
            try {
                int id = Integer.parseInt(materialString);
                return convertOldMaterialId(id);
            } catch (NumberFormatException ex) {
                // Se não for número, retorna null
                return null;
            }
        }
    }

    private static Material convertOldMaterialId(int id) {
        // Converte IDs antigos para materiais válidos
        switch (id) {
            case 0: return Material.AIR;
            case 1: return Material.STONE;
            case 2: return Material.GRASS;
            case 3: return Material.DIRT;
            case 4: return Material.COBBLESTONE;
            case 5: return Material.WOOD;
            case 6: return Material.SAPLING;
            case 7: return Material.BEDROCK;
            case 8: return Material.WATER;
            case 9: return Material.STATIONARY_WATER;
            case 10: return Material.LAVA;
            case 11: return Material.STATIONARY_LAVA;
            case 12: return Material.SAND;
            case 13: return Material.GRAVEL;
            case 14: return Material.GOLD_ORE;
            case 15: return Material.IRON_ORE;
            case 16: return Material.COAL_ORE;
            case 17: return Material.LOG;
            case 18: return Material.LEAVES;
            case 19: return Material.SPONGE;
            case 20: return Material.GLASS;
            case 21: return Material.LAPIS_ORE;
            case 22: return Material.LAPIS_BLOCK;
            case 23: return Material.DISPENSER;
            case 24: return Material.SANDSTONE;
            case 25: return Material.NOTE_BLOCK;
            case 26: return Material.BED_BLOCK;
            case 27: return Material.POWERED_RAIL;
            case 28: return Material.DETECTOR_RAIL;
            case 29: return Material.PISTON_STICKY_BASE;
            case 30: return Material.WEB;
            case 31: return Material.LONG_GRASS;
            case 32: return Material.DEAD_BUSH;
            case 33: return Material.PISTON_BASE;
            case 34: return Material.PISTON_EXTENSION;
            case 35: return Material.WOOL;
            case 36: return Material.PISTON_MOVING_PIECE;
            case 37: return Material.YELLOW_FLOWER;
            case 38: return Material.RED_ROSE;
            case 39: return Material.BROWN_MUSHROOM;
            case 40: return Material.RED_MUSHROOM;
            case 41: return Material.GOLD_BLOCK;
            case 42: return Material.IRON_BLOCK;
            case 43: return Material.DOUBLE_STEP;
            case 44: return Material.STEP;
            case 45: return Material.BRICK;
            case 46: return Material.TNT;
            case 47: return Material.BOOKSHELF;
            case 48: return Material.MOSSY_COBBLESTONE;
            case 49: return Material.OBSIDIAN;
            case 50: return Material.TORCH;
            case 51: return Material.FIRE;
            case 52: return Material.MOB_SPAWNER;
            case 53: return Material.WOOD_STAIRS;
            case 54: return Material.CHEST;
            case 55: return Material.REDSTONE_WIRE;
            case 56: return Material.DIAMOND_ORE;
            case 57: return Material.DIAMOND_BLOCK;
            case 58: return Material.WORKBENCH;
            case 59: return Material.CROPS;
            case 60: return Material.SOIL;
            case 61: return Material.FURNACE;
            case 62: return Material.BURNING_FURNACE;
            case 63: return Material.SIGN_POST;
            case 64: return Material.WOODEN_DOOR;
            case 65: return Material.LADDER;
            case 66: return Material.RAILS;
            case 67: return Material.COBBLESTONE_STAIRS;
            case 68: return Material.WALL_SIGN;
            case 69: return Material.LEVER;
            case 70: return Material.STONE_PLATE;
            case 71: return Material.IRON_DOOR_BLOCK;
            case 72: return Material.WOOD_PLATE;
            case 73: return Material.REDSTONE_ORE;
            case 74: return Material.GLOWING_REDSTONE_ORE;
            case 75: return Material.REDSTONE_TORCH_OFF;
            case 76: return Material.REDSTONE_TORCH_ON;
            case 77: return Material.STONE_BUTTON;
            case 78: return Material.SNOW;
            case 79: return Material.ICE;
            case 80: return Material.SNOW_BLOCK;
            case 81: return Material.CACTUS;
            case 82: return Material.CLAY;
            case 83: return Material.SUGAR_CANE_BLOCK;
            case 84: return Material.JUKEBOX;
            case 85: return Material.FENCE;
            case 86: return Material.PUMPKIN;
            case 87: return Material.NETHERRACK;
            case 88: return Material.SOUL_SAND;
            case 89: return Material.GLOWSTONE;
            case 90: return Material.PORTAL;
            case 91: return Material.JACK_O_LANTERN;
            case 92: return Material.CAKE_BLOCK;
            case 93: return Material.DIODE_BLOCK_OFF;
            case 94: return Material.DIODE_BLOCK_ON;
            case 96: return Material.TRAP_DOOR;
            case 97: return Material.MONSTER_EGGS;
            case 98: return Material.SMOOTH_BRICK;
            case 99: return Material.HUGE_MUSHROOM_1;
            case 100: return Material.HUGE_MUSHROOM_2;
            case 101: return Material.IRON_FENCE;
            case 102: return Material.THIN_GLASS;
            case 103: return Material.MELON_BLOCK;
            case 104: return Material.PUMPKIN_STEM;
            case 105: return Material.MELON_STEM;
            case 106: return Material.VINE;
            case 107: return Material.FENCE_GATE;
            case 108: return Material.BRICK_STAIRS;
            case 109: return Material.SMOOTH_STAIRS;
            case 110: return Material.MYCEL;
            case 111: return Material.WATER_LILY;
            case 112: return Material.NETHER_BRICK;
            case 113: return Material.NETHER_FENCE;
            case 114: return Material.NETHER_BRICK_STAIRS;
            case 115: return Material.NETHER_WARTS;
            case 116: return Material.ENCHANTMENT_TABLE;
            case 117: return Material.BREWING_STAND;
            case 118: return Material.CAULDRON;
            case 119: return Material.ENDER_PORTAL;
            case 120: return Material.ENDER_PORTAL_FRAME;
            case 121: return Material.ENDER_STONE;
            case 122: return Material.DRAGON_EGG;
            case 123: return Material.REDSTONE_LAMP_OFF;
            case 124: return Material.REDSTONE_LAMP_ON;
            case 125: return Material.WOOD_DOUBLE_STEP;
            case 126: return Material.WOOD_STEP;
            case 127: return Material.COCOA;
            case 128: return Material.SANDSTONE_STAIRS;
            case 129: return Material.EMERALD_ORE;
            case 130: return Material.ENDER_CHEST;
            case 131: return Material.TRIPWIRE_HOOK;
            case 132: return Material.TRIPWIRE;
            case 133: return Material.EMERALD_BLOCK;
            case 134: return Material.SPRUCE_WOOD_STAIRS;
            case 135: return Material.BIRCH_WOOD_STAIRS;
            case 136: return Material.JUNGLE_WOOD_STAIRS;
            case 137: return Material.COMMAND;
            case 138: return Material.BEACON;
            case 139: return Material.COBBLE_WALL;
            case 140: return Material.FLOWER_POT;
            case 141: return Material.CARROT;
            case 142: return Material.POTATO;
            case 143: return Material.WOOD_BUTTON;
            case 144: return Material.SKULL;
            case 145: return Material.ANVIL;
            case 146: return Material.TRAPPED_CHEST;
            case 147: return Material.GOLD_PLATE;
            case 148: return Material.IRON_PLATE;
            case 149: return Material.REDSTONE_COMPARATOR_OFF;
            case 150: return Material.REDSTONE_COMPARATOR_ON;
            case 151: return Material.DAYLIGHT_DETECTOR;
            case 152: return Material.REDSTONE_BLOCK;
            case 153: return Material.QUARTZ_ORE;
            case 154: return Material.HOPPER;
            case 155: return Material.QUARTZ_BLOCK;
            case 156: return Material.QUARTZ_STAIRS;
            case 157: return Material.ACTIVATOR_RAIL;
            case 158: return Material.DROPPER;
            case 159: return Material.STAINED_CLAY;
            case 160: return Material.STAINED_GLASS_PANE;
            case 161: return Material.LEAVES_2;
            case 162: return Material.LOG_2;
            case 163: return Material.ACACIA_STAIRS;
            case 164: return Material.DARK_OAK_STAIRS;
            case 165: return Material.SLIME_BLOCK;
            case 166: return Material.BARRIER;
            case 167: return Material.IRON_TRAPDOOR;
            case 168: return Material.PRISMARINE;
            case 169: return Material.SEA_LANTERN;
            case 170: return Material.HAY_BLOCK;
            case 171: return Material.CARPET;
            case 172: return Material.HARD_CLAY;
            case 173: return Material.COAL_BLOCK;
            case 174: return Material.PACKED_ICE;
            case 175: return Material.DOUBLE_PLANT;
            case 176: return Material.STANDING_BANNER;
            case 177: return Material.WALL_BANNER;
            case 178: return Material.DAYLIGHT_DETECTOR_INVERTED;
            case 179: return Material.RED_SANDSTONE;
            case 180: return Material.RED_SANDSTONE_STAIRS;
            case 181: return Material.DOUBLE_STONE_SLAB2;
            case 182: return Material.STONE_SLAB2;
            case 183: return Material.SPRUCE_FENCE_GATE;
            case 184: return Material.BIRCH_FENCE_GATE;
            case 185: return Material.JUNGLE_FENCE_GATE;
            case 186: return Material.DARK_OAK_FENCE_GATE;
            case 187: return Material.ACACIA_FENCE_GATE;
            case 188: return Material.SPRUCE_FENCE;
            case 189: return Material.BIRCH_FENCE;
            case 190: return Material.JUNGLE_FENCE;
            case 191: return Material.DARK_OAK_FENCE;
            case 192: return Material.ACACIA_FENCE;
            case 193: return Material.SPRUCE_DOOR;
            case 194: return Material.BIRCH_DOOR;
            case 195: return Material.JUNGLE_DOOR;
            case 196: return Material.ACACIA_DOOR;
            case 197: return Material.DARK_OAK_DOOR;
            default: return Material.AIR; // Material padrão para IDs não mapeados
        }
    }
}
