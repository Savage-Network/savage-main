package com.minecraft.savagemc.arcade.bedwars.menu.shop;

import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedShop;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HotbarEditorMenu extends Menu {

    private final User user;
    private final BedMember member;
    
    @Setter
    private Integer selectedSlot = null;

    public HotbarEditorMenu(Player player, Menu last) {
        super(player, "Editando hotbar...", last, 3);
        this.user = (User) User.of(player.getUniqueId());
        this.member = user.getMember();
    }

    @Override
    public void handle() {
        clear();

        if (selectedSlot == null) {
            // Tela inicial: mostrar slots da hotbar
            setTitle("Editando hotbar...");
            
            // Linha 1: Vazia (slots 0-8)
            for (int i = 0; i < 9; i++) {
                addItem(i, null);
            }
            
            // Linha 2: Slots da hotbar com ícones das categorias ou vidros vermelhos vazios
            for (int i = 0; i < 9; i++) {
                addHotbarSlot(i + 9, i);
            }
            
            // Linha 3: Vazia (slots 18-26)
            for (int i = 18; i < 27; i++) {
                addItem(i, null);
            }
            
        } else {
            // Tela de seleção: mostrar categorias disponíveis
            setTitle("Adicionado no slot #" + (selectedSlot + 1) + "...");
            
            // Linha 1: Vazia
            for (int i = 0; i < 9; i++) {
                addItem(i, null);
            }
            
            // Linha 2: Vidro verde (voltar) + categorias
            addItem(9, Item.of(Material.STAINED_GLASS_PANE, 5, "§aVoltar")
                    .click(event -> {
                        selectedSlot = null;
                        sound(MenuSound.PAGINATED);
                        handle();
                    }));
            
            // Adicionar categorias nos slots 10-17
            int slot = 10;
            for (BedShop category : BedShop.values()) {
                if (category == BedShop.FAVORITE) continue;
                if (slot >= 18) break;
                
                int currentSlot = member.getHotbarSlot(category);
                
                List<String> lore = new ArrayList<>();
                lore.add("");
                if (currentSlot >= 0) {
                    lore.add("§7Slot atual: §a#" + (currentSlot + 1));
                } else {
                    lore.add("§7Slot atual: §cNenhum");
                }
                lore.add("");
                lore.add("§eClique para adicionar");
                
                addItem(slot, Item.fromStack(category.getIcon())
                        .name("§e" + category.getName())
                        .lore(lore)
                        .click(event -> {
                            member.removeHotbarSlot(category);
                            member.setHotbarSlot(category, selectedSlot);
                            sound(MenuSound.DONE);
                            getPlayer().sendMessage("§a" + category.getName() + " §aadicionado ao slot #" + (selectedSlot + 1));
                            selectedSlot = null;
                            handle();
                        }));
                slot++;
            }
            
            // Linha 3: Vazia
            for (int i = 18; i < 27; i++) {
                addItem(i, null);
            }
        }

        if (isReturnable() && selectedSlot == null)
            addBackButton(22);

        display();
    }
    
    private void addHotbarSlot(int menuSlot, int hotbarSlot) {
        // Verificar qual categoria está configurada para este slot
        BedShop assignedCategory = null;
        for (BedShop category : BedShop.values()) {
            if (category == BedShop.FAVORITE) continue;
            if (member.getHotbarSlot(category) == hotbarSlot) {
                assignedCategory = category;
                break;
            }
        }

        if (assignedCategory != null) {
            // Slot ocupado - mostrar o ícone da categoria
            BedShop finalCategory = assignedCategory;
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Categoria: §e" + assignedCategory.getName());
            lore.add("");
            lore.add("§eBotão esquerdo: §7Adicionar");
            lore.add("§cBotão direito: §7Remover");
            
            addItem(menuSlot, Item.fromStack(assignedCategory.getIcon())
                    .name("§aSlot #" + (hotbarSlot + 1))
                    .lore(lore)
                    .click(event -> {
                        if (event.isRightClick()) {
                            member.removeHotbarSlot(finalCategory);
                            sound(MenuSound.DONE);
                            getPlayer().sendMessage("§c" + finalCategory.getName() + " §cremovido do slot #" + (hotbarSlot + 1));
                            handle();
                        } else if (event.isLeftClick()) {
                            selectedSlot = hotbarSlot;
                            sound(MenuSound.PAGINATED);
                            handle();
                        }
                    }));
        } else {
            // Slot vazio - mostrar vidro vermelho
            addItem(menuSlot, Item.of(Material.STAINED_GLASS_PANE, 14, "§cSlot #" + (hotbarSlot + 1))
                    .lore("",
                            "§7Nenhuma categoria",
                            "",
                            "§eClique para adicionar")
                    .click(event -> {
                        if (event.isLeftClick()) {
                            selectedSlot = hotbarSlot;
                            sound(MenuSound.PAGINATED);
                            handle();
                        }
                    }));
        }
    }
}
