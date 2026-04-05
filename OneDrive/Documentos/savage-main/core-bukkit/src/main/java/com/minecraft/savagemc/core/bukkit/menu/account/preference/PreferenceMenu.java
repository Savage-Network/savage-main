package com.minecraft.savagemc.core.bukkit.menu.account.preference;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.toggle.Toggle;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.bukkit.manager.list.TagManager;
import com.minecraft.savagemc.core.bukkit.menu.account.preference.prefix.PrefixTypeMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Setter
public class PreferenceMenu extends Menu {

    private final Account account;

    private ToggleCategory category;

    public PreferenceMenu(Player player, Menu last) {
        super(player, "Preferências", last, 6);

        this.account = Savage.getAccountController().of(player.getUniqueId());
        this.category = ToggleCategory.MEMBER;
    }

    // Método adicional para compatibilidade
    public void setCategory(ToggleCategory category) {
        this.category = category;
    }

    @Override
    public void handle() {
        clear();

        /* Filtro de Preferências */
        List<String> filterLore = buildFilterLore();

        addItem(4, Item.of(Material.HOPPER, "§bFiltrando por:", filterLore)
                .click(event -> {
                    ToggleCategory next = category.next(account);

                    if (next.equals(category)) return;

                    setCategory(next);
                    sound(MenuSound.PAGINATED);

                    handle();
                }));

        /* Filtrando Preferências por Categoria */
        handleFilteredItems();

        if (isReturnable())
            addBackButton();

        display();
    }

    private List<String> buildFilterLore() {
        List<String> filterLore = new ArrayList<>();

        filterLore.add("");

        for (ToggleCategory toggleCategory : ToggleCategory.values()) {
            if (toggleCategory.equals(ToggleCategory.VIP) && !account.isVIP()) continue;
            if (toggleCategory.equals(ToggleCategory.STAFF) && !account.isStaffer()) continue;

            boolean current = this.category.equals(toggleCategory);

            filterLore.add((current ? "§a" + Constant.ARROW_ALT_SYMBOL + " " : "§7") + toggleCategory.getName());
        }

        filterLore.add("");
        filterLore.add("§eClique para alternar!");
        return filterLore;
    }

    @Getter
public enum ToggleCategory {

        MEMBER("Membro", ChatColor.GRAY),
        VIP("VIP", ChatColor.GREEN),
        STAFF("Staff", ChatColor.RED);

        private final String name;
        private final ChatColor color;

        ToggleCategory(String name, ChatColor color) {
            this.name = name;
            this.color = color;
        }

        public ToggleCategory next(Account account) {
            if (account.isStaffer()) return this != STAFF ? values()[ordinal() + 1] : MEMBER;

            if (account.isVIP()) return this == VIP ? MEMBER : VIP;

            return MEMBER;
        }
    }

    protected void handleFilteredItems() {
        Toggle toggle = account.getToggle();

        switch (category) {
            case VIP: {
                // Mensagem de entrada
                addItem(10, Item.of(Material.BANNER, getColorByToggle(toggle.isAllowJoinMessage()) + "Mensagem de entrada",
                                "§7Permita que ao entrar",
                                "§7os jogadores sejam notificados.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            toggle.setAllowJoinMessage(!toggle.isAllowJoinMessage());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));
                addItem(19, toggleButton(toggle.isAllowJoinMessage(), "Mensagem de entrada")
                        .click(event -> {
                            toggle.setAllowJoinMessage(!toggle.isAllowJoinMessage());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));

                // Estilo de prefixo
                addItem(12, Item.of(Material.NAME_TAG, "§aEstilo de prefixo",
                                "§7Altere o estilo do",
                                "§7seu prefixo no servidor.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            sound(MenuSound.PAGINATED);
                            new PrefixTypeMenu(getPlayer(), account, this).handle();
                        }));

                addItem(21, Item.of(Material.STONE_BUTTON, "§aEstilo de prefixo",
                                "§7Altere o estilo do",
                                "§7seu prefixo no servidor.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            sound(MenuSound.PAGINATED);
                            new PrefixTypeMenu(getPlayer(), account, this).handle();
                        }));

                break;
            }

            case STAFF: {
                // Bate-papo da equipe
                addItem(10, Item.of(Material.PAPER, getColorByToggle(toggle.isStaffChat()) + "Staffchat",
                                "§7Ative ou desative as",
                                "§7mensagens do staffchat.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            toggle.setStaffChat(!toggle.isStaffChat());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));
                addItem(19, toggleButton(toggle.isStaffChat(), "Staffchat")
                        .click(event -> {
                            toggle.setStaffChat(!toggle.isStaffChat());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));

                // Auto vanish
                addItem(12, Item.of(Material.NETHER_STAR, getColorByToggle(toggle.isAutoVanish()) + "Auto vanish",
                                "§7Ative ou desative o seu",
                                "§7vanish automático.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            toggle.setAutoVanish(!toggle.isAutoVanish());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));
                addItem(21, toggleButton(toggle.isAutoVanish(), "Auto vanish")
                        .click(event -> {
                            toggle.setAutoVanish(!toggle.isAutoVanish());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));

                // Logs do servidor
                addItem(14, Item.of(Material.CHEST, getColorByToggle(toggle.isAllowLogs()) + "Logs",
                                "§7Ative ou desative as",
                                "§7logs gerais do servidor.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            toggle.setAllowLogs(!toggle.isAllowLogs());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));
                addItem(23, toggleButton(toggle.isAllowLogs(), "Logs")
                        .click(event -> {
                            toggle.setAllowLogs(!toggle.isAllowLogs());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));

                break;
            }

            // Membro
            default: {
                // Mensagens privadas
                addItem(10, Item.of(Material.PAPER, getColorByToggle(toggle.isAllowMessages()) + "Mensagens Privadas",
                                "§7Permita que os jogadores enviem",
                                "§7mensagens privadas.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            toggle.setAllowMessages(!toggle.isAllowMessages());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));
                addItem(19, toggleButton(toggle.isAllowMessages(), "Mensagens Privadas")
                        .click(event -> {
                            toggle.setAllowMessages(!toggle.isAllowMessages());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));

                // Pedidos de Amizade
                addItem(12, Item.of(Material.COOKIE, getColorByToggle(toggle.isAllowFriendRequests()) + "Pedidos de Amizade",
                                "§7Permita que os jogadores enviem",
                                "§7pedidos de amizade.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            toggle.setAllowFriendRequests(!toggle.isAllowFriendRequests());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));
                addItem(21, toggleButton(toggle.isAllowFriendRequests(), "Pedidos de Amizade")
                        .click(event -> {
                            toggle.setAllowFriendRequests(!toggle.isAllowFriendRequests());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));

                // Convites de Party
                addItem(14, Item.of(Material.CAKE, getColorByToggle(toggle.isAllowPartyInvite()) + "Convites de Party",
                                "§7Permita que os jogadores convidem",
                                "§7você para a party.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            toggle.setAllowPartyInvite(!toggle.isAllowPartyInvite());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));
                addItem(23, toggleButton(toggle.isAllowPartyInvite(), "Convites de Party")
                        .click(event -> {
                            toggle.setAllowPartyInvite(!toggle.isAllowPartyInvite());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));

                // Convites de Clan
                addItem(16, Item.of(Material.ITEM_FRAME, getColorByToggle(toggle.isAllowClanInvite()) + "Convites de Clan",
                                "§7Permita que os jogadores convidem",
                                "§7você para o clan.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            toggle.setAllowClanInvite(!toggle.isAllowClanInvite());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));
                addItem(25, toggleButton(toggle.isAllowClanInvite(), "Convites de Clan")
                        .click(event -> {
                            toggle.setAllowClanInvite(!toggle.isAllowClanInvite());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));

                // Visualização de estatísticas
                addItem(28, Item.of(Material.SIGN, getColorByToggle(toggle.isAllowStatsView()) + "Visualizar estatísticas",
                                "§7Permita que os jogadores vejam",
                                "§7as suas estatísticas.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            toggle.setAllowStatsView(!toggle.isAllowStatsView());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));
                addItem(37, toggleButton(toggle.isAllowStatsView(), "Visualizar estatísticas")
                        .click(event -> {
                            toggle.setAllowStatsView(!toggle.isAllowStatsView());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));

                // Confirmar retorno para lobby
                addItem(30, Item.of(Material.ACACIA_DOOR_ITEM, getColorByToggle(toggle.isConfirmToHub()) + "Confirmar retorno",
                                "§7Ao tentar voltar ao lobby,",
                                "§7confirme o seu retorno.",
                                "",
                                "§eClique para alternar!")
                        .click(event -> {
                            toggle.setConfirmToHub(!toggle.isConfirmToHub());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));
                addItem(39, toggleButton(toggle.isConfirmToHub(), "Confirmar retorno")
                        .click(event -> {
                            toggle.setConfirmToHub(!toggle.isConfirmToHub());
                            account.setToggle(toggle);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }));

                // Permitir clan tag
                if (account.hasClan()) {
                    addItem(32, Item.of(Material.NAME_TAG, getColorByToggle(toggle.isAllowClanTag()) + "Tag do clan",
                                    "§7Ative ou desative a sua",
                                    "§7clan tag no nickname.",
                                    "",
                                    "§eClique para alternar!")
                            .click(event -> {
                                toggle.setAllowClanTag(!toggle.isAllowClanTag());
                                account.setToggle(toggle);

                                sound(MenuSound.SUCCESS);
                                handle();

                                TagManager.updateTag(account);
                            }));
                    addItem(41, toggleButton(toggle.isAllowClanTag(), "Tag do clan")
                            .click(event -> {
                                toggle.setAllowClanTag(!toggle.isAllowClanTag());
                                account.setToggle(toggle);

                                sound(MenuSound.SUCCESS);
                                handle();

                                TagManager.updateTag(account);
                            }));
                }

                break;
            }
        }
    }
}
