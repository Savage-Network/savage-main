package com.minecraft.savagemc.core.api.skin.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SkinCategory {

    NONE("Nenhum"),
    CHARACTER("Personagens"),
    FESTIVE("Festivas");

    private final String name;

    public SkinCategory next() {
        return this != FESTIVE ? values()[ordinal() + 1] : NONE;
    }
}
