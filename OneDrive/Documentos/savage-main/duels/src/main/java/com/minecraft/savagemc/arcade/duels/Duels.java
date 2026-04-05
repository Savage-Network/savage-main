package com.minecraft.savagemc.arcade.duels;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.option.ServerOptions;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.savagemc.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.savagemc.core.server.type.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ShapelessRecipe;

public class Duels extends BukkitCore {

    @Override
    public void onLoad() {
        super.onLoad();

        saveDefaultConfig();

        // Configurações do Servidor
        ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE = false;
        ServerOptions.DAMAGE_ENABLED = true;

        ServerOptions.DROP_ITEM_ENABLED = true;
        ServerOptions.DEFAULT_CHAT = false;

        ServerOptions.BLOCK_PLACE = true;
        ServerOptions.BLOCK_BREAK = true;

        ServerOptions.BLOCK_INTERACTION_ENABLED = true;

        Savage.setServerType(ServerType.DUELS);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".arcade.duels.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".arcade.duels");

        getManager().getArcade().handle(this, Constant.SOURCE_DIR + ".arcade.duels.arcade.list");

        handleRecipes();

        Savage.getLogger().info("Duels iniciado com sucesso!");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        Savage.getLogger().info("Duels encerrado com sucesso!");
    }

    @SuppressWarnings("deprecation")
    protected void handleRecipes() {
        ShapelessRecipe recipe = new ShapelessRecipe(Item.of(Material.MUSHROOM_SOUP))
                .addIngredient(Material.BOWL);

        Bukkit.addRecipe(recipe.addIngredient(Material.INK_SACK, 3));
        Bukkit.addRecipe(recipe.addIngredient(Material.CACTUS));
    }
}
