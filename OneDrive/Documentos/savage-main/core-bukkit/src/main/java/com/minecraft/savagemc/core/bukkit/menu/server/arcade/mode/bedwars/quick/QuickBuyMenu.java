package com.minecraft.savagemc.core.bukkit.menu.server.arcade.mode.bedwars.quick;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.bukkit.menu.server.arcade.mode.bedwars.quick.favorite.FavoriteSelectMenu;
import com.minecraft.savagemc.core.bukkit.menu.server.arcade.mode.bedwars.quick.share.ShareSearchMenu;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.share.ShareType;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.savagemc.core.member.list.bedwars.objects.menu.BedMenuMetadata;
import com.minecraft.savagemc.core.member.list.bedwars.objects.menu.item.BedItem;
import com.minecraft.savagemc.core.member.list.bedwars.objects.menu.type.BedMenuType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuickBuyMenu extends Menu {

    private final BedMember member;

    public QuickBuyMenu(Player player, BedMember member, Menu last) {
        super(player, "Editar " + BedShop.FAVORITE.getName().toLowerCase(), last, 6, 21);

        this.member = member;

        setAllowClickItemWithQuantity(true);
    }

    @Override
    public void handle() {
        clear();

        if (member == null)
            addErrorButton("§cNão foi possível recolher os seus dados...");
        else {

            BedMenuMetadata menu = member.getMenu(BedMenuType.FAVORITE);

            if (menu == null)
                addErrorButton("§cNão foi possível visualizar o seu inventário...");
            else {

                /* Adicionando Espaços Vázios - 3 linhas de 7 slots (21 slots totais) */
                // Linhas 2-4 (slots 10-16, 19-25, 28-34)
                int[] slots = {
                    10, 11, 12, 13, 14, 15, 16,  // Linha 2
                    19, 20, 21, 22, 23, 24, 25,  // Linha 3
                    28, 29, 30, 31, 32, 33, 34   // Linha 4
                };
                
                for (int slot : slots) {
                    addItem(slot, Item.of(Material.STAINED_GLASS_PANE, 14, "§cEspaço vázio!",
                                    "§7Este é um espaço para",
                                    "§7item favorito.",
                                    "",
                                    "§eClique esquerdo§7 para definir.")
                            .click(event -> {
                                if (event.isLeftClick()) {
                                    sound(MenuSound.PAGINATED);
                                    new FavoriteSelectMenu(getPlayer(), member, this, slot).handle();
                                } else
                                    sound(MenuSound.ERROR);
                            }));
                }

                List<BedItem> list = menu.getItemList();

                for (BedItem item : list) {
                    BedWarsItem root = BedWarsItem.of(item.getName());

                    if (root == null) continue;

                    int targetSlot = item.getSlot();
                    
                    // Validar se o slot está dentro dos slots válidos
                    boolean validSlot = false;
                    for (int validSlotCheck : slots) {
                        if (targetSlot == validSlotCheck) {
                            validSlot = true;
                            break;
                        }
                    }
                    
                    if (!validSlot) continue;

                    List<String> lore = new ArrayList<>(root.getLore());

                    if (lore.isEmpty())
                        lore.add("§7Sem descrição.");

                    lore.addAll(Arrays.asList(
                            "",
                            "§eClique esquerdo§7 para substituir.",
                            "§eClique direito§7 para remover."
                    ));

                    addItem(targetSlot, Item.fromStack(root.getStack())
                            .name("§a" + root.getName())
                            .flags(ItemFlag.values())
                            .lore(lore)
                            .click(event -> {
                                if (event.isRightClick()) {
                                    sound(MenuSound.DONE);

                                    member.removeFavoriteItem(root);

                                    handle();
                                    return;
                                }

                                if (event.isLeftClick()) {
                                    sound(MenuSound.PAGINATED);
                                    new FavoriteSelectMenu(getPlayer(), member, this, targetSlot).handle();
                                }
                            }));
                }

                /* My Share */
                ShareType share = member.getMetadata().getShare();

                List<String> myShareLore = new ArrayList<>(share.getLore());

                myShareLore.addAll(Arrays.asList(
                        "",
                        "§7Opções disponíveis:"
                ));

                for (ShareType search : ShareType.values()) {
                    myShareLore.add(" " + search.getColor() + (share.equals(search) ? Constant.ARROW_ALT_SYMBOL + " " : "") + search.getName());
                }

                myShareLore.addAll(Arrays.asList(
                        "",
                        "§eClique para alternar!"));

                int id = share.equals(ShareType.MEDIUM) ? 4 : share.equals(ShareType.HARD) ? 1 : share.equals(ShareType.MAX) ? 14 : 7;

                addItem(getTotalSlots() - 9, Item.of(Material.WOOL, id, "§aMeu compartilhamento", myShareLore)
                        .click(event -> {
                            sound(MenuSound.DONE);
                            member.setShare(share.next());

                            handle();
                        }));
                
                // Botão para remover todos os favoritos
                addItem(getTotalSlots() - 5, Item.of(Material.BARRIER, "§cRemover todos os favoritos",
                                "§7Remove todos os itens",
                                "§7da sua lista de favoritos.",
                                "",
                                "§cClique para remover tudo!")
                        .click(event -> {
                            sound(MenuSound.DONE);
                            
                            // Remover todos os itens favoritos
                            List<BedItem> itemsToRemove = new ArrayList<>(list);
                            for (BedItem item : itemsToRemove) {
                                BedWarsItem root = BedWarsItem.of(item.getName());
                                if (root != null) {
                                    member.removeFavoriteItem(root);
                                }
                            }
                            
                            getPlayer().sendMessage("§cTodos os favoritos foram removidos!");
                            handle();
                        }));

                addItem(getTotalSlots() - 1, Item.of(Material.ANVIL, "§aCompartilhar inventário",
                                "§7Compartilhe o seu estilo de",
                                "§7inventário com outros jogadores.",
                                "",
                                "§eClique para ver!")
                        .click(event -> {
                            sound(MenuSound.PAGINATED);
                            new ShareSearchMenu(getPlayer(), list, this).handle();
                        }));
            }
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
