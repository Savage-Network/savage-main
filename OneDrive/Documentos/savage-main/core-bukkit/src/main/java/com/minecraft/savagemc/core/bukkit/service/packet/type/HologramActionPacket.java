package com.minecraft.savagemc.core.bukkit.service.packet.type;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bukkit.api.hologram.Hologram;
import com.minecraft.savagemc.core.bukkit.api.hologram.row.HologramRow;
import com.minecraft.savagemc.core.bukkit.api.hologram.touch.Touch;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.savagemc.core.bukkit.manager.list.HologramManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class HologramActionPacket extends PacketAdapter {

    private final HologramManager manager;

    public HologramActionPacket(Plugin plugin, HologramManager manager) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY);

        this.manager = manager;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        try {
            Player player = event.getPlayer();
            PacketContainer packet = event.getPacket();

            int entityId = packet.getIntegers().read(0);
            EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);

            if (action.equals(EnumWrappers.EntityUseAction.ATTACK) || action.equals(EnumWrappers.EntityUseAction.INTERACT_AT)) {

                Hologram hologram = manager.getHolograms().values().stream()
                        .filter(search -> search.isEntityId(entityId))
                        .findFirst()
                        .orElse(null);

                if (hologram != null) {
                    if (hologram instanceof HologramClient && !((HologramClient) hologram).getReceiver().getUniqueId().equals(player.getUniqueId()))
                        return;

                    Touch touch = action.equals(EnumWrappers.EntityUseAction.ATTACK) ? Touch.LEFT : Touch.RIGHT;

                    HologramRow clickableRow = hologram.getRows().stream()
                            .filter(row -> row.getId() == entityId && row.hasTouch())
                            .findFirst()
                            .orElse(null);

                    if (clickableRow != null) {
                        if (clickableRow.getTouch() != null) {
                            clickableRow.getTouch().handle(player, touch);
                        }
                    } else if (hologram.hasTouch() && hologram.getTouch() != null)
                        hologram.getTouch().handle(player, touch);
                }
            }
        } catch (Exception e) {
            Savage.getLogger().log(Level.SEVERE, "Ocorreu um erro com o toque dos hologramas...", e);
        }
    }
}
