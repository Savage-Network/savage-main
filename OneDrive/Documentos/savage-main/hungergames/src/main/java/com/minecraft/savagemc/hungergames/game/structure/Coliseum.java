package com.minecraft.savagemc.hungergames.game.structure;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.savagemc.hungergames.util.bo3.BO3;
import com.minecraft.savagemc.hungergames.util.bo3.BO3Block;
import org.bukkit.Location;

public class Coliseum {

    private final BO3 bo3;
    //    private final Material gateMaterial;
    private final Location location;
//    private Set<Block> gateBlocks;

    public Coliseum(Location location, BO3 bo3/*, Material gate*/) {
        this.bo3 = bo3;
//        this.gateMaterial = gate;
        this.location = location;
    }

    public void spawn(BO3.BlockHandle blockHandle) {

//        this.gateBlocks = new HashSet<>();

        for (BO3Block binaryBlock : getBO3().getBlocks()) {
            Location block = location.clone().add(binaryBlock.getX(), binaryBlock.getY(), binaryBlock.getZ());
            Pattern pattern = binaryBlock.getPattern();

            if (blockHandle.canPlace(block, pattern)) {
//                if (pattern.getMaterial() == getGateMaterial())
//                    gateBlocks.add(block.getBlock());
//                else
                WorldEditAPI.getInstance().setBlock(block, pattern.getMaterial(), pattern.getData());
            }
        }

        // Placing gate after to avoid it affecting spawn stairs!

//        for (Block block : gateBlocks) {
//            WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.IRON_FENCE, (byte) 0);
//        }
    }

    public BO3 getBO3() {
        return bo3;
    }

//    public Material getGateMaterial() {
//        return gateMaterial;
//    }

//    public Set<Block> getGate() {
//        return gateBlocks;
//    }

    public Location getLocation() {
        return location;
    }
}
