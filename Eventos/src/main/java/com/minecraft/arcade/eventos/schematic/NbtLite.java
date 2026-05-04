package com.minecraft.arcade.eventos.schematic;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Leitor NBT mínimo apenas para schematic McEdit gzip: Compound, Short, ByteArray.
 * Implementação proprietária (sem WorldEdit nem libs externas).
 */
final class NbtLite {

    private static final int TAG_END = 0;
    private static final int TAG_BYTE = 1;
    private static final int TAG_SHORT = 2;
    private static final int TAG_INT = 3;
    private static final int TAG_BYTE_ARRAY = 7;

    private NbtLite() {
    }

    static Compound readRootCompound(InputStream raw) throws IOException {
        InputStream in = peekIsGzip(raw) ? new GZIPInputStream(raw) : raw;
        return readNamedCompound(new DataInputStream(in));
    }

    private static boolean peekIsGzip(InputStream raw) throws IOException {
        raw.mark(2);
        int a = raw.read();
        int b = raw.read();
        raw.reset();
        return a == 0x1f && b == 0x8b;
    }

    private static Compound readNamedCompound(DataInput in) throws IOException {
        byte type = in.readByte();
        if (type != 10) {
            throw new IOException("Raiz esperada TAG_Compound (10), obtido " + type);
        }
        readUtf(in); /* nome ignorado na raiz do schematic */
        return readCompoundPayload(in);
    }

    static Compound readCompoundPayload(DataInput in) throws IOException {
        Map<String, Object> map = new HashMap<>();
        for (;;) {
            byte type = in.readByte();
            if (type == TAG_END) {
                break;
            }
            String name = readUtf(in);
            Object value = readPayload(in, type);
            map.put(name, value);
        }
        return new Compound(map);
    }

    private static Object readPayload(DataInput in, byte type) throws IOException {
        switch (type) {
            case TAG_BYTE:
                return in.readByte();
            case TAG_SHORT:
                return in.readShort();
            case TAG_INT:
                return in.readInt();
            case TAG_BYTE_ARRAY:
                int len = in.readInt();
                byte[] buf = new byte[len];
                in.readFully(buf);
                return buf;
            case 10:
                return readCompoundPayload(in);
            default:
                skipPayload(in, type);
                return null;
        }
    }

    private static void skipPayload(DataInput in, byte type) throws IOException {
        switch (type) {
            case TAG_BYTE:
                in.skipBytes(1);
                break;
            case TAG_SHORT:
                in.skipBytes(2);
                break;
            case TAG_INT:
            case TAG_FLOAT:
                in.skipBytes(4);
                break;
            case TAG_LONG:
            case TAG_DOUBLE:
                in.skipBytes(8);
                break;
            case 8: {
                int ls = in.readUnsignedShort();
                in.skipBytes(ls);
                break;
            }
            case TAG_BYTE_ARRAY: {
                int nbytes = in.readInt();
                in.skipBytes(nbytes);
                break;
            }
            case 9:
                byte inner = in.readByte();
                int listLen = in.readInt();
                for (int i = 0; i < listLen; i++) {
                    skipPayload(in, inner);
                }
                break;
            case 10:
                skipCompound(in);
                break;
            case 11: {
                int intLen = in.readInt();
                in.skipBytes(4 * intLen);
                break;
            }
            default:
                throw new IOException("Tag NBT não suportado para ignorar: " + type);
        }
    }

    private static void skipCompound(DataInput in) throws IOException {
        for (;;) {
            byte t = in.readByte();
            if (t == TAG_END) return;
            readUtf(in);
            skipPayload(in, t);
        }
    }

    private static String readUtf(DataInput in) throws IOException {
        int utfLen = in.readUnsignedShort();
        byte[] chars = new byte[utfLen];
        in.readFully(chars);
        return new String(chars, StandardCharsets.UTF_8);
    }

    static final class Compound {
        final Map<String, Object> map;

        Compound(Map<String, Object> map) {
            this.map = map;
        }

        short getShort(String k, short def) {
            Object v = map.get(k);
            if (v instanceof Short) return (Short) v;
            if (v instanceof Integer) return ((Integer) v).shortValue();
            if (v instanceof Byte) return ((Byte) v).shortValue();
            return def;
        }

        byte[] getByteArray(String k) {
            Object v = map.get(k);
            return v instanceof byte[] ? (byte[]) v : null;
        }

        Compound getCompound(String k) {
            Object v = map.get(k);
            return v instanceof Compound ? (Compound) v : null;
        }
    }
}
