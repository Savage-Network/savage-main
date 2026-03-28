package com.minecraft.savagemc.core.bukkit.menu.server.collectible;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.collectible.CollectibleCategory;
import com.minecraft.savagemc.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.collectible.type.banner.BannerCollectible;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.controller.list.CollectibleController;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CollectibleMenu extends Menu {

    private final Account account;

    private final CollectibleController controller = Savage.getCollectibleController();

    public CollectibleMenu(Player player) {
        super(player, "Colecionáveis", 6);

        this.account = Savage.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        buildComponent(10, CollectibleCategory.COMPANION, Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGIzNmQ3OWJhNDA3MDUzYzI1MWJkNGFhOTEzYTVlMjgxY2NkMjRkOGQ5ZWJlOTNkMjhiYjYyZjE0MGFkYzUifX19"));

        buildComponent(11, CollectibleCategory.ARTIFACT, Item.of(Material.BREWING_STAND_ITEM));

        buildComponent(13, CollectibleCategory.BALLOON, Item.of(Material.SKULL_ITEM, 3)
                .skullByBase64("eyJ0aW1lc3RhbXAiOjE1MTMzMDU3MjcxOTYsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzczNGQzZjQ2NjQ4YTZiZjVlMzdjNjFkMmNiNzVhNmI4ZTI0NTExODExMWI5YzYwMzE0NTg4ZWYwNzMzM2U4In19fQ"));

        buildComponent(15, CollectibleCategory.EMOTION, Item.of(Material.SKULL_ITEM, 3)
                .skullByUrl("8c54055ee8f9e7638830c79ff12de81e9c7c4e8f3ba3db555019a65872b9c6c"));

        buildComponent(16, CollectibleCategory.HAT, Item.of(Material.DIAMOND_HELMET));

        buildComponent(29, CollectibleCategory.BANNER, BannerCollectible.getFirst());
        buildComponent(31, CollectibleCategory.PARTICLE, Item.of(Material.ENCHANTMENT_TABLE));
        buildComponent(33, CollectibleCategory.TITLE, Item.of(Material.NAME_TAG));

        addCloseButton();

        addItem(getTotalSlots() - 6, Item.of(Material.BARRIER, "§cDesativar",
                        "§7Desabilite todos os",
                        "§7colecionáveis ativos.",
                        "",
                        "§eClique para desabilitar!")
                .click(event -> {
                    if (CollectibleOperator.list(getPlayer()).isEmpty()) {
                        sound(MenuSound.ERROR);
                        account.send("§cVocê não possui colecionáveis ativos.");

                        return;
                    }

                    close();
                    sound(MenuSound.DONE);

                    CollectibleOperator.disable(getPlayer());

                    account.send("§aOs colecionáveis foram desabilitados.");
                }));

        display();
    }

    protected void buildComponent(int slot, CollectibleCategory category, Item icon) {
        List<String> lore = new ArrayList<>(category.getLore());

        lore.add("");
        lore.add("§7Suas variedades:");

        for (CollectibleRarity rarity : CollectibleRarity.values()) {
            int me = account.getTotalCollectibles(category, rarity), size = controller.list(category, rarity).size();

            lore.add(" " + rarity.getColoredName() + " §8" + me + "/" + size);
        }

        lore.add("");
        lore.add("§eClique para ver!");

        addItem(slot, icon.name("§a" + category.getName())
                .lore(lore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);

                    new CollectibleSelectorMenu(getPlayer(), category, this).handle();
                }));
    }
}
