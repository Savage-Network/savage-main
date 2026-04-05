package com.minecraft.savagemc.core.bukkit.api.menu;

import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandSender;
import com.minecraft.savagemc.core.bukkit.manager.Manager;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.page.ItemPageBuilder;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
@Setter
public abstract class Menu {

    private final Player player;

    private String title;
    private final String initialTitle;

    private org.bukkit.inventory.Inventory holder;

    private final Menu last;
    private final Map<Integer, Item> contents;

    private final List<ItemStack> protectedContents;
    private final List<Integer> protectedSlots;

    // Variáveis configuráveis
    private boolean returnable, paginated, allowClick, allowClickItemWithQuantity, allowDrag = true, allowShift = true, showTitlePage = true;

    private int size, rows, maxItems, pageIndex, pageNumber = 1, totalPages = 1;
    
    private ServerType serverType;

    public Menu(Player player, String title, Menu last, int rows, int maxItems) {
        this.player = player;

        this.title = title;
        this.initialTitle = title;

        this.last = last;
        this.contents = new HashMap<>();

        this.returnable = last != null;
        this.paginated = maxItems > 0;

        this.rows = rows;
        this.size = rows * 9;

        this.maxItems = maxItems;

        this.protectedContents = new ArrayList<>();
        this.protectedSlots = new ArrayList<>();

        this.holder = Bukkit.createInventory(player, size, title);
    }

    public Menu(Player player, String title, Menu last, int rows) {
        this(player, title, last, rows, 0);
    }

    public Menu(Player player, String title, int rows, int maxItems) {
        this(player, title, null, rows, maxItems);
    }

    public Menu(Player player, String title, int rows) {
        this(player, title, null, rows, 0);
    }

    public Menu(Player player, String title, boolean allowClick, int rows) {
        this(player, title, null, rows, 0);

        setAllowClick(allowClick);
    }

    /**
     * Inicializar o processo de criação do inventário.
     */
    public abstract void handle();

    public void display() {
        if (player == null) return;

        contents.forEach((slot, item) -> holder.setItem(slot, item));

        player.openInventory(holder);

        if (totalPages > 0 && showTitlePage)
            updateTitle();

        BukkitCore.getManager().getMenu().save(this);
    }

    // Métodos adicionais para compatibilidade
    public void setAllowClick(boolean allowClick) {
        this.allowClick = allowClick;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void close() {
        player.closeInventory();
    }

    public void clear() {
        if (holder != null)
            holder.clear();

        if (!contents.isEmpty())
            contents.clear();
    }

    public void clear(int... selectedSlots) {
        for (int slot : selectedSlots) {

            Item item = contents.get(slot);
            if (item == null) continue;

            item.type(Material.AIR);
            contents.remove(slot);
        }
    }

    public void addProtectedContent(int... slots) {
        for (int slot : slots) {
            protectedSlots.add(slot);
        }
    }

    public boolean isProtectedContent(ItemStack stack) {
        return stack != null && protectedContents.stream().anyMatch(item -> item.isSimilar(stack));
    }

    public boolean isProtectedSlot(int slot) {
        return protectedSlots.contains(slot);
    }

    public void updateTitle() {
        EntityPlayer entity = ((CraftPlayer) player).getHandle();

        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(
                entity.activeContainer.windowId,
                "minecraft:chest",
                new ChatMessage(title),
                player.getOpenInventory().getTopInventory().getSize());

        entity.playerConnection.sendPacket(packet);
        entity.updateInventory(entity.activeContainer);
    }

    public void addItem(int slot, Item item) {
        addItem(slot, item, false);
    }

    public void addItem(int slot, Item item, boolean protection) {
        contents.put(slot, item);

        if (protection)
            protectedContents.add(item);
    }

    public void removeItem(int slot) {
        holder.setItem(slot, Item.of(Material.AIR));
    }

    public <T> void buildPageItems(List<T> list, int lastPageSlot, int nextPageSlot, int initialSlot, ItemPageBuilder<T> itemPageBuilder) {
        setTotalPages((list.size() + maxItems - 1) / maxItems);
        addBorderPage(lastPageSlot, nextPageSlot);

        if (totalPages > 1 && showTitlePage)
            setTitle(initialTitle + " (" + pageNumber + "/" + totalPages + ")");

        int last = initialSlot;

        for (int i = 0; i < maxItems; i++) {
            int index = maxItems * (pageNumber - 1) + i;
            if (index >= list.size()) break;

            T item = list.get(index);

            if (item != null)
                itemPageBuilder.accept(item, initialSlot);

            initialSlot++;
            if (initialSlot == (last + 7)) {
                initialSlot += 2;
                last = initialSlot;
            }
        }
    }

    public <T> void buildPageItems(List<T> list, int initialSlot, ItemPageBuilder<T> itemPageBuilder) {
        buildPageItems(list, getTotalSlots() - 6, getTotalSlots() - 4, initialSlot, itemPageBuilder);
    }

    public void addBorderPage() {
        addBorderPage(getTotalSlots() - 6, getTotalSlots() - 4);
    }

    public void addBorderPage(int lastSlot, int nextSlot) {
        // Adicionar botão de página anterior.
        if (pageNumber > 1) {
            addItem(lastSlot, Item.of(Material.ARROW, "§aPágina anterior",
                            "§ePágina " + (pageNumber - 1))
                    .click(event -> {
                        setPageNumber(pageNumber - 1);

                        if (showTitlePage)
                            setTitle(initialTitle + " (" + pageNumber + "/" + totalPages + ")");

                        sound(MenuSound.PAGINATED);
                        handle();
                    }));
        }

        // Adicionar botão de próxima página.
        if (pageNumber < totalPages) {
            addItem(nextSlot, Item.of(Material.ARROW, "§aPróxima página",
                            "§ePágina " + (pageNumber + 1))
                    .click(event -> {
                        setPageNumber(pageNumber + 1);

                        if (showTitlePage)
                            setTitle(initialTitle + " (" + pageNumber + "/" + totalPages + ")");

                        sound(MenuSound.PAGINATED);
                        handle();
                    }));
        }
    }

    public void addCloseButton() {
        addCloseButton(getTotalSlots() - (hasBackButton() ? 6 : 5));
    }

    public void addCloseButton(int slot) {
        addItem(slot, new Item(Material.ARROW)
                .name("§cFechar")
                .click(event -> {
                    sound(MenuSound.ERROR);

                    close();
                }), true);
    }

    public boolean hasBackButton() {
        boolean found = false;

        for (Map.Entry<Integer, Item> entry : contents.entrySet()) {
            int slot = entry.getKey();

            Item item = entry.getValue();

            if (slot == (getTotalSlots() - 5) && item.getType().equals(Material.ARROW)) {
                found = true;
                break;
            }
        }

        return found;
    }

    public void addBackButton(int slot) {
        addItem(slot, Item.of(Material.ARROW, "§aVoltar", last != null ? "§7Para " + last.getInitialTitle() : "")
                .click(event -> {
                    if (last == null) {
                        sound(MenuSound.ERROR);
                        return;
                    }
                    last.handle();

                    sound(MenuSound.PAGINATED);
                }), true);
    }

    public void addBackButton() {
        if (pageNumber <= 1)
            addBackButton(getTotalSlots() - (totalPages > 1 ? 6 : 5));
    }

    public void addErrorButton(String name) {
        addErrorButton(13, name);
    }

    public void addErrorButton(int slot, String name) {
        addItem(slot, Item.of(Material.WEB, name), true);
    }

    public Item toggleButton(boolean preference, String name) {
        return new Item(Material.INK_SACK, 1, (preference ? 10 : 8))
                .name(getColorByToggle(preference) + name)
                .lore("§7Estado: §f" + (preference ? "Ativo" : "Inativo"),
                        "",
                        "§eClique para alternar!");
    }

    public ChatColor getColorByToggle(boolean toggle) {
        return toggle ? ChatColor.GREEN : ChatColor.RED;
    }

    public void sound(MenuSound sound) {
        this.sound(sound.getSound());
    }

    public void sound(Sound sound) {
        player.playSound(player.getLocation(), sound, 2f, 2.5f);
    }

    public int getItemSlot(Material material) {
        int slot = 0;

        for (Map.Entry<Integer, Item> entry : getContents().entrySet()) {
            Item item = entry.getValue();

            if (item.getType().equals(material))
                slot = entry.getKey();
        }

        return slot;
    }

    public int getTotalSlots() {
        return rows * 9;
    }

    // Métodos adicionais para compatibilidade
    public Map<Integer, Item> getContents() {
        return contents;
    }

    public void setTitle(String title) {
        this.title = title;
        holder = Bukkit.createInventory(player, size, title);
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getInitialTitle() {
        return initialTitle;
    }

    public int getRows() {
        return rows;
    }

    public boolean isReturnable() {
        return returnable;
    }

    public Menu getLast() {
        return last;
    }

    public void setAllowClickItemWithQuantity(boolean allow) {
        this.allowClickItemWithQuantity = allow;
    }

    public void setAllowShift(boolean allow) {
        this.allowShift = allow;
    }

    public void setAllowDrag(boolean allow) {
        this.allowDrag = allow;
    }

    public void setServer(ServerType server) {
        this.serverType = server;
    }

    public Player getPlayer() {
        return player;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAllowClickItemWithQuantity() {
        return allowClickItemWithQuantity;
    }

    public boolean isAllowShift() {
        return allowShift;
    }

    public boolean isAllowClick() {
        return allowClick;
    }

    public boolean isAllowDrag() {
        return allowDrag;
    }

    public boolean isProtectedContent(Item item) {
        // Implementação básica - pode ser ajustada conforme necessário
        return false;
    }

    public int getMaxItems() {
        return getTotalSlots();
    }

    public int getTotalPages() {
        return totalPages;
    }
}
