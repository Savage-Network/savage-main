package com.minecraft.savagemc.core.bukkit.api.menu.page;

@FunctionalInterface
public interface ItemPageBuilder<T> {
    void accept(T item, int slot);
}
