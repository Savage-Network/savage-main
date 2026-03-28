package com.minecraft.savagemc.core.bukkit.menu.server.arcade.mode.aquatic;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.bukkit.menu.account.stats.StatsInfoMenu;
import com.minecraft.savagemc.core.member.list.aquatic.AquaticMember;
import com.minecraft.savagemc.core.server.type.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AquaticMenu extends Menu {

    private final Account account;

    private final AquaticMember member;

    public AquaticMenu(Player player) {
        super(player, "Menu do " + ServerType.AQUATIC.getName(), 5);

        this.account = Savage.getAccountController().of(player.getUniqueId());

        this.member = (AquaticMember) account.loadMember(ServerType.AQUATIC);
    }

    @Override
    public void handle() {
        clear();

        /* Se o membro for nulo, não carregar os dados */
        if (member == null)
            addErrorButton("§cNão foi possível carregar os seus dados...");
        else {
            addItem(16, Item.of(Material.PAPER, "§aEstatísticas",
                            "§7Veja o seu desempenho",
                            "§7nas partidas.",
                            "",
                            "§eClique para ver!")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new StatsInfoMenu(getPlayer(), account, ServerType.AQUATIC, this).handle();
                    }));
        }

        addCloseButton();

        display();
    }
}
