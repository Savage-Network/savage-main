package com.minecraft.savagemc.arcade.bedwars.menu.game.teleport;

import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class TeleportMenu extends Menu {

    private final User user;

    public TeleportMenu(Player player) {
        super(player, "Teleportador", 1);

        this.user = (User) User.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        Arena arena = user.getArena();

        List<User> userList = arena.getAliveUsers();

        if (userList.isEmpty())
            addErrorButton(4, "§cNão há jogadores...");
        else {
            int slot = 0;
            for (User user : userList) {
                Team team = user.getTeam();

                if (team == null) continue;

                Account account = user.getAccount();

                Player player = account.player();

                if (player == null) continue;

                int lifePercent = Util.percentage((int) player.getHealth(), (int) player.getMaxHealth());

                String lifeColor = lifePercent >= 70 ? "§a"
                        : lifePercent <= 50 && lifePercent >= 20 ? "§e"
                        : "§c";

                final int finalSlot = slot;
                addItem(finalSlot, Item.of(Material.SKULL_ITEM, 3, account.getTag().getPrefix() + account.getNickname(),
                                "",
                                "§7Time: " + team.getColoredName(),
                                "§7Vida atual: " + lifeColor + lifePercent + "%",
                                "",
                                "§eClique para ir.")
                        .skullByBase64(account.getSkin().getValue())
                        .updater(view -> {
                            if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                            Item item = getContents().get(finalSlot);

                            if (item != null) {
                                int newPercent = Util.percentage((int) player.getHealth(), (int) player.getMaxHealth());

                                String newColor = newPercent >= 70 ? "§a"
                                        : newPercent <= 50 && newPercent >= 20 ? "§e"
                                        : "§c";

                                item.lore("",
                                        "§7Time: " + team.getColoredName(),
                                        "§7Vida atual: " + newColor + newPercent + "%",
                                        "",
                                        "§eClique para ir.");

                                view.setItem(finalSlot, item);

                                getPlayer().updateInventory();
                            }
                        })
                        .click(event -> {
                            close();
                            sound(MenuSound.DONE);

                            getPlayer().teleport(account.player());
                        }));

                slot++;
            }
        }

        display();
    }
}
