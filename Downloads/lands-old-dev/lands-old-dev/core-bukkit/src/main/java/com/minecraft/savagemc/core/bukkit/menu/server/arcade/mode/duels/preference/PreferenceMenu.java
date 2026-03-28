package com.minecraft.savagemc.core.bukkit.menu.server.arcade.mode.duels.preference;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.member.list.duels.DuelMember;
import com.minecraft.savagemc.core.member.list.duels.metadata.DuelMetadata;
import com.minecraft.savagemc.core.server.type.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PreferenceMenu extends Menu {

    private final DuelMember member;

    public PreferenceMenu(Player player, Menu last) {
        super(player, "Preferências", last, 5);

        Account account = Savage.getAccountController().of(player.getUniqueId());

        this.member = (DuelMember) account.loadMember(ServerType.DUELS);
    }

    @Override
    public void handle() {
        clear();

        if (member == null)
            addErrorButton("§cNão foi possível carregar os seus dados...");
        else {
            DuelMetadata metadata = member.getMetadata();

            addItem(10, Item.of(Material.SKULL_ITEM, 3, getColorByToggle(metadata.isSpectatorEnabled()) + "Espectador",
                            "§7Permita que outros jogadores",
                            "§7vejam suas partidas.",
                            "",
                            "§eClique para alternar!")
                    .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2VlZWQ1MGIzMmZjNDhhZWUzZGJkMDdmNjFkNjYyYmZmNDgyYWIwYWE5N2M3OGNjMzExMGI2MmJjZTU5YTJlOSJ9fX0=")
                    .click(event -> {
                        metadata.setSpectatorEnabled(!metadata.isSpectatorEnabled());
                        member.saveMetadata(metadata);

                        sound(MenuSound.SUCCESS);
                        handle();
                    }));
            addItem(19, toggleButton(metadata.isSpectatorEnabled(), "Espectador")
                    .click(event -> {
                        metadata.setSpectatorEnabled(!metadata.isSpectatorEnabled());
                        member.saveMetadata(metadata);

                        sound(MenuSound.SUCCESS);
                        handle();
                    }));
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
