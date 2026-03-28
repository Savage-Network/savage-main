package com.minecraft.savagemc.core.controller.list;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.skin.Skin;
import com.minecraft.savagemc.core.api.skin.objects.SkinType;
import com.mojang.authlib.properties.Property;

import java.util.UUID;
import java.util.logging.Level;

public class SkinController {

    public static Skin getSkin(UUID id, String displayName, SkinType type) {
        try {
            Property property = Savage.MOJANG_API.getTextures(id);

            return new Skin(type, id, displayName, property.getValue(), property.getSignature());
        } catch (Exception e) {
            Savage.Savage.Logger().log(Level.SEVERE, "Não foi possível carregar a skin " + id, e);
            return null;
        }
    }
}
