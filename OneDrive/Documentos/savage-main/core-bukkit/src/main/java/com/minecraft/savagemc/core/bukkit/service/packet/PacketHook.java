package com.minecraft.savagemc.core.bukkit.service.packet;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.service.packet.type.CommandListPacket;
import com.minecraft.savagemc.core.bukkit.service.packet.type.HologramActionPacket;
import com.minecraft.savagemc.core.bukkit.service.packet.type.NpcActionPacket;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

public class PacketHook {

    private final Plugin plugin;

    private final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    public PacketHook(Plugin plugin) {
        this.plugin = plugin;
    }

    public void handle() {
        if (manager == null) {
            Savage.getLogger().warning("ProtocolLibrary não encontrado! PacketHook desativado.");
            return;
        }
        
        manager.addPacketListener(new NpcActionPacket(plugin, BukkitCore.getManager().getNpc()));
        manager.addPacketListener(new HologramActionPacket(plugin, BukkitCore.getManager().getHologram()));

        manager.addPacketListener(new CommandListPacket(plugin));
    }
}
