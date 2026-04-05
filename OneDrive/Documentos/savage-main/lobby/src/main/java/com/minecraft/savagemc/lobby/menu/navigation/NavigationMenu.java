package com.minecraft.savagemc.lobby.menu.navigation;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.bukkit.menu.server.arcade.custom.CustomArcadeMenu;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.lobby.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NavigationMenu extends Menu {

    private final User user;
    private final NavigationType type;

    public NavigationMenu(Player player, NavigationType type) {
        super(player, type.equals(NavigationType.SERVER) ? "Navegação" : "Selecionar Sala", 4, 8);

        this.user = (User) User.of(getPlayer().getUniqueId());
        this.type = type;

        setShowTitlePage(false);
    }

    @Override
    public void handle() {
        clear();

        switch (type) {
            case SERVER: {
                buildComponent(10, ServerType.BEDWARS,
                        "§7Destrua as camas inimigas,",
                        "§7seja o último vivo, e vença!");

                buildComponent(11, ServerType.DUELS,
                        "§7Jogue modos únicos,",
                        "§7e vença os inimigos!");

                buildComponent(12, ServerType.PVP,
                        "§7Enfrente outros jogadores",
                        "§7com habilidades especiais.");

                buildComponent(13, ServerType.AQUATIC,
                        "§7Enfrente outros jogadores",
                        "§7em zonas aquáticas.");

                addItem(14, Item.of(Material.STAINED_CLAY, "§aThe Bridge",
                                "§7" + Util.formatNumber(ArcadeCategory.DUELS_THE_BRIDGE.getPlayingNow()) + " jogando agora.")
                        .click(event -> {
                            sound(MenuSound.PAGINATED);
                            //new ArcadeSlotMenu(getPlayer(), this, ArcadeCategory.DUELS_THE_BRIDGE).handle();
                        }));

                addItem(getTotalSlots() - 1, Item.of(Material.BREWING_STAND_ITEM, "§aSalas customizadas",
                                "§7Crie salas customizadas",
                                "§7para jogar com seus amigos.",
                                "",
                                "§7Exclusivo para " + RankType.BETA.getBoldColorName().toUpperCase(),
                                "",
                                "§eClique para criar!")
                        .click(event -> {
                            if (!user.getAccount().hasRank(RankType.CHIEF)) {
                                sound(MenuSound.ERROR);
                                return;
                            }

                            sound(MenuSound.PAGINATED);
                            new CustomArcadeMenu(getPlayer(), this).handle();
                        }));
                break;
            }

            case ROOM: {
                addItem(10, Item.of(Material.WOOL, 14, "§cLobby #1",
                                "§7Jogadores: §a" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(),
                                "",
                                "§cSala atual.")
                        .click(event -> {
                            close();
                            sound(MenuSound.ERROR);
                        }));
                break;
            }
        }

        display();
    }

    protected void buildComponent(int slot, ServerType server, String... lore) {
        Item item = Item.of(Material.getMaterial(server.getIconId()), "§a" + server.getName())
                .flags(ItemFlag.values());

        ServerType serverLobby = server.getServerLobby();

        int online = Savage.getServerData().getOnlinePlayers(server) + (serverLobby != null ? Savage.getServerData().getOnlinePlayers(serverLobby) : 0);

        List<String> itemLore = new ArrayList<>(Arrays.asList(
                "§8" + server.getStyle().getName(),
                ""));

        itemLore.addAll(Arrays.asList(lore));

        itemLore.addAll(Arrays.asList(
                "",
                "§a" + Constant.ARROW_ALT_SYMBOL + " Clique para jogar!",
                "§7" + Util.formatNumber(online) + " jogando."
        ));

        // Finalizando configuração de Item
        item.lore(itemLore)
                .updater(view -> {
                    if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                    // Atualizando última e penúltima linha
                    boolean blink = (System.currentTimeMillis() / 1000) % 2 == 0;

                    int onlinePlayers = Savage.getServerData().getOnlinePlayers(server) + (serverLobby != null ? Savage.getServerData().getOnlinePlayers(serverLobby) : 0);

                    itemLore.set(itemLore.size() - 2, "§a" + (blink ? Constant.ARROW_ALT_SYMBOL : "") + " Clique para jogar!");
                    itemLore.set(itemLore.size() - 1, "§7" + Util.formatNumber(onlinePlayers) + " jogando.");

                    item.lore(itemLore);

                    view.setItem(slot, item);

                    getPlayer().updateInventory();
                });

        item.click(event -> {
            close();
            sound(MenuSound.DONE);

            user.getAccount().redirect(serverLobby != null ? serverLobby : server);
        });

        addItem(slot, item);
    }

    public enum NavigationType {
        SERVER, ROOM
    }
}


