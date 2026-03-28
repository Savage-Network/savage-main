package com.minecraft.savagemc.core.bukkit.menu.server.arcade.map;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.api.party.Party;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.payload.ArcadePayload;
import com.minecraft.savagemc.core.arcade.room.map.Map;
import com.minecraft.savagemc.core.arcade.room.payload.RoomPayload;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.arcade.room.type.Type;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.arcade.route.join.Join;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.RoomRequestMessage;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.server.Server;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ArcadeMapMenu extends Menu {

    private final Account account;

    private final ArcadeCategory arcade;
    private final Type type;

    public ArcadeMapMenu(Player player, ArcadeCategory arcade, Type type, Menu last) {
        super(player, "Selecionar mapa", last, 5);

        this.account = Savage.getAccountController().of(player.getUniqueId());

        this.arcade = arcade;
        this.type = type;
    }

    @Override
    public void handle() {
        clear();

        ArcadePayload payload = Savage.getArcadeData().read(arcade);

        if (payload == null)
            addErrorButton("§cNão foi possível encontrar mapas...");
        else {
            List<Map> maps = payload.getMaps();

            int slot = 10, last = slot;
            for (Map map : maps) {
                List<RoomPayload> properties = payload.getRooms(room -> room.getMap().getId() == map.getId() && room.getType().equals(type));

                int availableRooms = (int) properties.stream()
                        .filter(room -> room.getPhase().equals(RoomPhase.WAITING) || room.getPhase().equals(RoomPhase.STARTING))
                        .count();

                boolean request = availableRooms == 0;

                int finalSlot = slot;

                addItem(finalSlot, Item.of(Material.EMPTY_MAP, "§a" + map.getName(),
                                "",
                                "§7Salas:",
                                " §7Abertas: §a" + availableRooms,
                                " §7Total: §a" + properties.size(),
                                "",
                                "§eClique para " + (request ? "solicitar uma sala" : "jogar") + ".")
                        .updater(view -> {
                            if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                            Item item = getContents().get(finalSlot);

                            if (item == null) return;

                            item.lore(
                                    "",
                                    "§7Salas:",
                                    " §7Abertas: §a" + availableRooms,
                                    " §7Total: §a" + properties.size(),
                                    "",
                                    "§eClique para " + (request ? "solicitar uma sala" : "jogar") + "."
                            );

                            view.setItem(finalSlot, item);

                            getPlayer().updateInventory();
                        })
                        .click(event -> {
                            close();
                            sound(MenuSound.PAGINATED);

                            Slot slotType = arcade.getSlots().get(0);

                            Party party = account.getParty();

                            if (party != null && !party.isAuthor(account.getId())) {
                                account.send("§cApenas o dono da party pode procurar salas.");
                                return;
                            }

                            /* Solicitar uma nova sala */
                            if (request) {
                                getPlayer().sendMessage("§aProcurando servidor...");

                                new RoomRequestMessage(getPlayer().getUniqueId(), arcade, slotType, type, map.getId()).send();
                            } else {
                                /* Entrar em uma sala */
                                ArcadeRouteContext route = ArcadeRouteContext.builder()
                                        .arcade(arcade)
                                        .slot(slotType)
                                        .type(type)
                                        .mapId(map.getId())
                                        .join(Join.PLAYER)
                                        .build();

                                Server server = Savage.getServerData().of(arcade.getServer());

                                route.setServerId(server != null ? server.getId() : 0);

                                if (party != null)
                                    route.setLink(party.getMembersId());

                                new ArenaSearchMessage(account.getId(), route).send();
                            }
                        }));

                slot++;
                if (slot == (last + 7)) {
                    slot += 2;
                    last = slot;
                }
            }
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
