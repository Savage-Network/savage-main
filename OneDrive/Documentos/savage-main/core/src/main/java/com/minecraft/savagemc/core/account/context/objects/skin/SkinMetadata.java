package com.minecraft.savagemc.core.account.context.objects.skin;

import com.minecraft.savagemc.core.api.skin.Skin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkinMetadata {

    private Skin skin = Skin.empty(), last = Skin.empty();

    private long updatedAt = System.currentTimeMillis();
}
