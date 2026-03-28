package com.minecraft.savagemc.core.bukkit.menu.account.friend;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.friend.Friend;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FriendInfoMenu extends Menu {

    private final Account account;

    private final Friend friend;

    public FriendInfoMenu(Player player, Friend friend, Menu last) {
        super(player, "Informações de Amigo", last, 4);

        this.account = Savage.getAccountController().of(player.getUniqueId());
        this.friend = friend;
    }

    @Override
    public void handle() {
        clear();

        addItem(12, Item.of(Material.BOW, "§aTipo de Amigo",
                        "§7Formas de atribuir",
                        "§7privilégios para amigos",
                        "§7mais confiáveis.",
                        "",
                        "§7Tipo atual: §b" + friend.getType().getName(),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    sound(MenuSound.SUCCESS);

                    friend.setType(friend.getType().next());
                    account.updateFriend(friend);

                    handle();
                }));

        addItem(14, Item.of(Material.DIODE, "§aPreferências",
                "§7Customize os privilégios",
                "§7do seu amigo.",
                "",
                "§eClique para ver!"));

        if (isReturnable())
            addBackButton();

        display();
    }
}
