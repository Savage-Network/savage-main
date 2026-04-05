package com.minecraft.savagemc.core.bukkit.api.npc.type.client;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bukkit.api.npc.Npc;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
public class NpcClient extends Npc {

    private final Player receiver;

    public NpcClient(Player receiver, Location location) {
        this(receiver, location, null);
    }

    public NpcClient(Player receiver, Location location, Property textures) {
        super(location);

        this.receiver = receiver;

        if (textures != null)
            updateTexture(textures);
    }

    @Override
    public void display() {
        super.display();

        Savage.getPlatform().runSync(() -> spawnTo(receiver), 2L);
    }

    @Override
    public void destroy() {
        super.destroy();

        despawnTo(receiver);
    }

    public void respawn() {
        despawnTo(receiver);
        spawnTo(receiver);
    }

    // Métodos adicionais para compatibilidade
    public Player getReceiver() {
        return receiver;
    }

    @Override
    public String getTag() {
        return super.getTag();
    }
}
