package com.minecraft.arcade.eventos.schematic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Lê arquivo {@code .schematic} no formato McEdit clássico (NBT + gzip opcional): Root "Schematic" ou raiz direto.
 */
final class MceditSchematicLoader {

    /** Ordem dos blocos: Y primeiro, depois Z, depois X (compatível McEdit). */
    private final short width;
    private final short height;
    private final short length;
    private final byte[] blocks;
    private final byte[] data;
    /** Nibbles opcionais para IDs &gt; 255 (altura do ID em 12 bits). */
    private final byte[] addBlocks;

    private MceditSchematicLoader(short width, short height, short length, byte[] blocks, byte[] data, byte[] addBlocks) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.blocks = blocks;
        this.data = data;
        this.addBlocks = addBlocks;
    }

    static MceditSchematicLoader fromFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis, 8192)) {
            bis.mark(8);
            NbtLite.Compound root = NbtLite.readRootCompound(bis);
            return fromCompound(root);
        }
    }

    private static MceditSchematicLoader fromCompound(NbtLite.Compound root) throws IOException {
        NbtLite.Compound schematic = root.getCompound("Schematic");
        NbtLite.Compound payload = schematic != null ? schematic : root;

        short w = payload.getShort("Width", (short) 0);
        short h = payload.getShort("Height", (short) 0);
        short l = payload.getShort("Length", (short) 0);

        byte[] blk = payload.getByteArray("Blocks");
        byte[] dat = payload.getByteArray("Data");
        byte[] add = payload.getByteArray("AddBlocks");

        if (w <= 0 || h <= 0 || l <= 0 || blk == null || dat == null) {
            throw new IOException("Schematic incompleta: Width/Height/Length/Blocks/Data");
        }
        int vol = uns(w) * uns(h) * uns(l);
        if (blk.length < vol || dat.length < vol) {
            throw new IOException("Buffers Blocks/Data menores que o volume declarado.");
        }

        /*
         * Alguns exports usam "WEOffset" irrelevante; WeOrigin opcional ignorado — cola sempre na origem Bukkit pedida.
         */
        return new MceditSchematicLoader(w, h, l, blk, dat, add);
    }

    private static int uns(short u) {
        return u & 0xFFFF;
    }

    int volume() {
        return uns(width) * uns(height) * uns(length);
    }

    int blockAt(int x, int y, int z) {
        if (x < 0 || x >= uns(width) || y < 0 || y >= uns(height) || z < 0 || z >= uns(length)) {
            return -1;
        }
        int index = indexYzx(y, z, x);
        int id = blocks[index] & 0xFF;
        if (addBlocks != null && index / 2 < addBlocks.length) {
            byte n = addBlocks[index / 2];
            int nibble = (index % 2) == 0 ? (n & 0x0F) : ((n >>> 4) & 0x0F);
            id |= nibble << 8;
        }
        return id;
    }

    int metaAt(int x, int y, int z) {
        if (x < 0 || x >= uns(width) || y < 0 || y >= uns(height) || z < 0 || z >= uns(length)) {
            return 0;
        }
        int index = indexYzx(y, z, x);
        return data[index] & 0xFF;
    }

    int indexYzx(int y, int z, int x) {
        return (y * uns(length) + z) * uns(width) + x;
    }

    short getWidth() {
        return width;
    }

    short getHeight() {
        return height;
    }

    short getLength() {
        return length;
    }
}
