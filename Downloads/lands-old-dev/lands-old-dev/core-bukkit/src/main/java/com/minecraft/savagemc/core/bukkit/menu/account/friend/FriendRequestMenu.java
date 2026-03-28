package com.minecraft.savagemc.core.bukkit.menu.account.friend;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.friend.request.FriendRequest;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountExecuteCommandMessage;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.util.list.DateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class FriendRequestMenu extends Menu {

    public FriendRequestMenu(Player player, Menu last) {
        super(player, "Solicitações de amizade", last, 6, 21);
    }

    @Override
    public void handle() {
        clear();

        Account account = Savage.getAccountController().of(getPlayer().getUniqueId());

        List<FriendRequest> list = account.getFriendsRequests();

        if (list.isEmpty())
            addItem(22, Item.of(Material.WEB, "§cNão há solicitações de amizade!"));
        else {
            // Construindo página
            buildPageItems(list, 10, (request, slot) -> {
                Account target = Savage.getAccountData().of(request.getSender());

                if (request.hasExpired())
                    account.removeFriendRequest(request.getSender());
                else if (target != null)
                    addItem(slot, Item.of(Material.SKULL_ITEM, 3, "§a" + target.getName(),
                                    "§7Enviado em: §f" + DateUtil.getDate(request.getStartedAt()),
                                    "§7Expira em: §f" + DateUtil.getDate(request.getExpiresAt()),
                                    "",
                                    "§eClique esquerdo: §fAceitar",
                                    "§eClique direito: §fRecusar")
                            .skullByBase64(target.getSkin().getValue())
                            .click(event -> {
                                if (event.isLeftClick()) {
                                    account.removeFriendRequest(request.getSender());

                                    new AccountExecuteCommandMessage(account.getId(), "amigo aceitar " + target.getName()).send();
                                } else if (event.isRightClick())
                                    account.removeFriendRequest(request.getSender());

                                sound(MenuSound.SUCCESS);
                                handle();
                            }));
            });
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
