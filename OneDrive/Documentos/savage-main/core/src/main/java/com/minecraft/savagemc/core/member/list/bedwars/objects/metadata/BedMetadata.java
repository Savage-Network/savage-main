package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata;

import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.share.ShareType;
import com.minecraft.savagemc.core.member.list.bedwars.objects.menu.BedMenuMetadata;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.stats.BedStats;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class BedMetadata {

    private ProfileType profile = ProfileType.NORMAL;
    private ShareType share = ShareType.DEFAULT;

    private boolean tntTimerEnabled = true, showRank = true, showBar = true;

    private final List<BedStats> statsList = new ArrayList<>();
    private List<BedMenuMetadata> menus = new ArrayList<>();

    private final Map<BedCollectibleType, String> collectibles = new HashMap<>();
    private final Map<BedShop, Integer> hotbarPreferences = new HashMap<>();
}
