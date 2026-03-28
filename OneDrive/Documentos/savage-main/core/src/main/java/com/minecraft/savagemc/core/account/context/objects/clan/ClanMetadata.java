package com.minecraft.savagemc.core.account.context.objects.clan;

import com.minecraft.savagemc.core.Constant;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClanMetadata {

    private UUID id = Constant.DEFAULT_ID;
    private long lastUpdate = System.currentTimeMillis();
}
