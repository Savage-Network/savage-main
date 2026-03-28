package com.minecraft.savagemc.core.member.list.bedwars.objects.menu;

import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.member.context.menu.MenuMetadata;
import com.minecraft.savagemc.core.member.list.bedwars.objects.menu.item.BedItem;
import com.minecraft.savagemc.core.member.list.bedwars.objects.menu.type.BedMenuType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BedMenuMetadata extends MenuMetadata {

    private final BedMenuType type;

    private final List<BedItem> itemList = new ArrayList<>();

    public BedMenuMetadata(BedMenuType type) {
        super(ArcadeCategory.NONE, "...");

        this.type = type;
    }

    public boolean hasItem(String name) {
        return itemList.stream().anyMatch(item -> item.getName().equalsIgnoreCase(name));
    }
}
