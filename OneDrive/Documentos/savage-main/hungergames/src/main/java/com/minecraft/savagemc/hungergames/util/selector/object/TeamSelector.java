package com.minecraft.savagemc.hungergames.util.selector.object;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.selector.ClickableItem;
import com.minecraft.core.bukkit.util.selector.SmartInventory;
import com.minecraft.core.bukkit.util.selector.content.InventoryContents;
import com.minecraft.core.bukkit.util.selector.content.InventoryProvider;
import com.minecraft.core.bukkit.util.selector.content.Pagination;
import com.minecraft.core.bukkit.util.selector.content.SlotIterator;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.game.list.Teamable;
import com.minecraft.savagemc.hungergames.game.team.Team;
import com.minecraft.savagemc.hungergames.game.team.TeamStorage;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class TeamSelector implements BukkitInterface, Assistance, Listener, InventoryProvider {

    private final SmartInventory inventory = SmartInventory.builder()
            .id("team_selector")
            .provider(this)
            .size(3, 9)
            .title("hg.team_selector")
            .closeable(true)
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {
        execute(player, contents);
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        execute(player, contents);
    }

    public void execute(Player player, InventoryContents inventoryContents) {

        Teamable teamable = (Teamable) HungerGames.getInstance().getGame();
        TeamStorage teamStorage = teamable.getTeamStorage();
        User user = User.fetch(player.getUniqueId());

        Pagination pagination = inventoryContents.pagination();
        ClickableItem[] items = new ClickableItem[teamStorage.teamCount()];

        for (int i = 0; i < teamStorage.getTeams().size(); i++) {
            Team team = teamStorage.getTeam(i);
            ItemFactory itemFactory = new ItemFactory(Material.LEATHER_CHESTPLATE).setColor(team.getColor());

            if (team.isMember(user))
                itemFactory.glow();

            int count = team.getMembers().size();

            itemFactory.setName(team.getChatColor() + user.getAccount().getLanguage().translate(team.getName()));
            List<String> description = new ArrayList<>();
            description.add("§aPlayers: §f" + team.getMembers().size() + "/" + teamStorage.getMaxSlots());
            description.add(" ");
            if (count >= teamStorage.getMaxSlots())
                description.add("§cEsse time está lotado!");
            else if (!team.isMember(user))
                description.add("§eClique para selecionar!");
            else
                description.add("§cClique para sair!");
            itemFactory.setDescription(description);

            items[i] = ClickableItem.of(itemFactory.getStack(), event -> {

                if (Vanish.getInstance().isVanished(player.getUniqueId())) {
                    player.sendMessage(user.getAccount().getLanguage().translate("hg.game.not_alive"));
                } else if (!team.isMember(user)) {

                    if (team.getMembers().size() >= teamStorage.getMaxSlots()) {
                        player.sendMessage("§cEste time está lotado.");
                        return;
                    }

                    if (user.hasTeam()) {
                        Team currentTeam = user.getTeam();
                        currentTeam.getMembers().remove(user);
                    }

                    user.setTeam(team);
                    team.getMembers().add(user);
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
                    player.sendMessage("§eVocê selecionou o time " + team.getChatColor() + user.getAccount().getLanguage().translate(team.getName()));
                } else if (user.hasTeam()) {
                    Team currentTeam = user.getTeam();
                    user.setTeam(null);
                    currentTeam.getMembers().remove(user);
                    player.sendMessage("§cVocê saiu do time " + team.getChatColor() + user.getAccount().getLanguage().translate(team.getName()));
                }
            });
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(7);

        if (!pagination.isFirst())
            inventoryContents.set(4, 0, ClickableItem.of(new ItemFactory(Material.ARROW).setName("§aPágina " + (pagination.getPage())).getStack(),
                    e -> inventory.open(player, pagination.previous().getPage(), null)));

        if (!pagination.isLast())
            inventoryContents.set(4, 8, ClickableItem.of(new ItemFactory(Material.ARROW).setName("§aPágina " + (pagination.getPage() + 2)).getStack(),
                    e -> inventory.open(player, pagination.next().getPage(), null)));

        SlotIterator slotIterator = inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1);
        slotIterator.blacklistBorders();

        pagination.addToIterator(slotIterator);
    }

    public SmartInventory getInventory() {
        return inventory;
    }
}
