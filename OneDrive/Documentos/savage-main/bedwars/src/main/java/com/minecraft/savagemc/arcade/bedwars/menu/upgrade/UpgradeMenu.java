package com.minecraft.savagemc.arcade.bedwars.menu.upgrade;

import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.menu.upgrade.trap.TrapMenu;
import com.minecraft.savagemc.arcade.bedwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.arcade.bedwars.structure.team.objects.forge.Forge;
import com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.TeamUpgrade;
import com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.structure.armor.ArmorProtection;
import com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.structure.haste.Haste;
import com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.structure.sword.Sword;
import com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.structure.trap.Trap;
import com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums.TrapCategory;
import com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums.TrapType;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.room.slot.Slot;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class UpgradeMenu extends Menu {

    private final User user;

    private final Arena arena;

    private final boolean isTrioOrQuartet;

    public UpgradeMenu(Player player) {
        super(player, "Armadilhas & Melhorias", 4);

        this.user = (User) User.of(player.getUniqueId());

        this.arena = user.getArena();

        this.isTrioOrQuartet = arena.getSlot().ordinal() >= Slot.TRIO.ordinal();
    }

    @Override
    public void handle() {
        clear();

        Team team = user.getTeam();

        addItem(10, buildSword(team));        // Sharp
        addItem(11, buildArmor(team));        // Armaduras
        addItem(12, buildForge(team));        // Forja
        addItem(13, buildHaste(team));        // Haste
        addItem(14, buildAutoRegeneration(team)); // Auto Regeneração
        
        // Armadilhas diretamente no menu
        addItem(16, buildTrapItem(team, com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums.TrapType.FATIGUE));
        addItem(25, buildTrapItem(team, com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums.TrapType.ITS_A_TRAP));

        addCloseButton();

        display();
    }

    protected Item buildForge(Team team) {
        Item icon = Item.of(Material.FURNACE)
                .flags(ItemFlag.values());

        Forge forge = team.getForge();

        if (!forge.isLevel(GeneratorLevel.NONE) && !icon.hasEnchantments())
            icon.unsafeEnchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Expanda os seus recursos",
                "§7aprimorando a sua forja.",
                ""
        ));

        for (GeneratorLevel level : GeneratorLevel.values()) {
            if (level.equals(GeneratorLevel.NONE)) continue;

            boolean has = forge.isLevel(level);

            int cost = isTrioOrQuartet ? level.getCost() * 2 : level.getCost();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + level.ordinal() + ": §7" + level.getDescription()
                    + ", §b" + cost + " diamantes");
        }

        lore.addAll(Arrays.asList(
                "",
                forge.isHighest()
                        ? "§cNível máximo."
                        : forge.canPurchase(getPlayer()) ? "§eClique para evoluir!"
                        : "§cVocê não tem diamantes suficientes."
        ));

        icon.name((forge.canPurchase(getPlayer()) ? "§a" : "§c") + "Forja")
                .lore(lore)
                .click(event -> {
                    if (forge.isHighest()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sua forja está no nível máximo.");

                        return;
                    }

                    if (!forge.canPurchase(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. Faltam " + user.getRemainingOre(BedOre.DIAMOND, forge.getNextCost()) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(BedOre.DIAMOND, forge.getNextCost());

                    forge.upgrade();

                    team.getPlayers().forEach(player -> player.sendMessage(team.getColor()
                            + getPlayer().getName() + "§e evoluiu a forja para o nível §c" + forge.getLevel().getTag() + "§e!"));

                    handle();
                });

        return icon;
    }

    protected Item buildSword(Team team) {
        Item icon = Item.of(Material.DIAMOND_SWORD)
                .flags(ItemFlag.values());

        Sword sword = team.getSword();

        if (sword != null && !icon.hasEnchantments())
            icon.enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Torne as suas espadas",
                "§7mais poderosas.",
                ""
        ));

        for (Sword swordLevel : Sword.values()) {
            if (isTrioOrQuartet && swordLevel.equals(Sword.THREE)) continue;

            boolean has = sword != null && sword.ordinal() >= swordLevel.ordinal();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + (swordLevel.ordinal() + 1) + ": §7" + swordLevel.getName()
                    + ", §b" + swordLevel.getAdjustedPrice(arena) + " diamantes");
        }

        lore.addAll(Arrays.asList(
                "",
                team.isHighestSword()
                        ? "§cNível máximo."
                        : team.canBuySword(getPlayer()) ? "§eClique para evoluir!"
                        : "§cVocê não tem diamantes suficientes."
        ));

        icon.name((team.canBuySword(getPlayer()) ? "§a" : "§c") + "Afiação")
                .lore(lore)
                .click(event -> {
                    if (isTrioOrQuartet && sword != null && sword.equals(Sword.TWO) || team.isHighestSword()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sua espada está no nível máximo.");

                        return;
                    }

                    Sword next = team.getNextSword();

                    if (!team.canBuySword(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(BedOre.DIAMOND, next.getAdjustedPrice(arena)) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(BedOre.DIAMOND, next.getAdjustedPrice(arena));

                    team.getUpgrade().setSword(next);

                    team.getPlayers().forEach(player -> {
                        player.sendMessage(team.getColor()
                                + getPlayer().getName() + "§e evoluiu as espadas para o nível §c" + next.getTag() + "§e!");
                    });
                    
                    // Fechar menu e aplicar encantamento após 1 tick
                    getPlayer().closeInventory();
                    
                    Bukkit.getScheduler().runTaskLater(com.minecraft.savagemc.arcade.bedwars.BedWars.getInstance(), () -> {
                        team.getPlayers().forEach(p -> {
                            next.applyEnchantment(p);
                            p.updateInventory();
                        });
                    }, 1L);
                });

        return icon;
    }

    protected Item buildHaste(Team team) {
        Item icon = Item.of(Material.GOLD_PICKAXE)
                .flags(ItemFlag.values());

        Haste haste = team.getHaste();

        if (haste != null && !icon.hasEnchantments())
            icon.enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Torne as suas picaretas",
                "§7mais ágeis.",
                ""
        ));

        for (Haste hasteLevel : Haste.values()) {
            boolean has = haste != null && haste.ordinal() >= hasteLevel.ordinal();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + (hasteLevel.ordinal() + 1) + ": §7" + hasteLevel.getName()
                    + ", §b" + hasteLevel.getAdjustedPrice(arena) + " diamantes");
        }

        lore.addAll(Arrays.asList(
                "",
                team.isHighestHaste()
                        ? "§cNível máximo."
                        : team.canBuyHaste(getPlayer()) ? "§eClique para evoluir!"
                        : "§cVocê não tem diamantes suficientes."
        ));

        icon.name((team.canBuyHaste(getPlayer()) ? "§a" : "§c") + "Agilidade")
                .lore(lore)
                .click(event -> {
                    if (team.isHighestHaste()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sua picareta está no nível máximo.");

                        return;
                    }

                    Haste next = team.getNextHaste();

                    if (!team.canBuyHaste(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(BedOre.DIAMOND, next.getAdjustedPrice(arena)) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(BedOre.DIAMOND, next.getAdjustedPrice(arena));

                    team.getUpgrade().setHaste(next);

                    team.getPlayers().forEach(player -> {
                        player.sendMessage(team.getColor()
                                + getPlayer().getName() + "§e evoluiu as picaretas para o nível §c" + next.getTag() + "§e!");
                    });
                    
                    // Fechar menu e aplicar encantamento após 1 tick
                    getPlayer().closeInventory();
                    
                    Bukkit.getScheduler().runTaskLater(com.minecraft.savagemc.arcade.bedwars.BedWars.getInstance(), () -> {
                        team.getPlayers().forEach(p -> {
                            next.applyEnchantment(p);
                        });
                    }, 1L);
                });

        return icon;
    }

    protected Item buildArmor(Team team) {
        Item icon = Item.of(Material.IRON_BOOTS)
                .flags(ItemFlag.values());

        ArmorProtection armor = team.getArmor();

        if (armor != null && !icon.hasEnchantments())
            icon.enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Torne as suas armaduras",
                "§7mais resistentes.",
                ""
        ));

        for (ArmorProtection armorLevel : ArmorProtection.values()) {
            if (armorLevel.equals(ArmorProtection.NONE)) continue;

            boolean has = armor != null && armor.ordinal() >= armorLevel.ordinal();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + armorLevel.ordinal() + ": §7" + armorLevel.getName()
                    + ", §b" + armorLevel.getAdjustedPrice(arena) + " diamantes");
        }

        lore.addAll(Arrays.asList(
                "",
                team.isHighestArmor()
                        ? "§cNível máximo."
                        : team.canBuyArmor(getPlayer()) ? "§eClique para evoluir!"
                        : "§cVocê não tem diamantes suficientes."
        ));

        icon.name((team.canBuyArmor(getPlayer()) ? "§a" : "§c") + "Armaduras")
                .lore(lore)
                .click(event -> {
                    if (team.isHighestArmor()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sua armadura está no nível máximo.");

                        return;
                    }

                    ArmorProtection next = team.getNextArmor();

                    if (!team.canBuyArmor(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(BedOre.DIAMOND, next.getAdjustedPrice(arena)) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(BedOre.DIAMOND, next.getAdjustedPrice(arena));

                    team.getUpgrade().setArmor(next);

                    team.getPlayers().forEach(player -> {
                        player.sendMessage(team.getColor()
                                + getPlayer().getName() + "§e evoluiu as armaduras para o nível §c" + next.getTag() + "§e!");
                    });
                    
                    // Fechar menu e aplicar encantamento após 1 tick
                    getPlayer().closeInventory();
                    
                    Bukkit.getScheduler().runTaskLater(com.minecraft.savagemc.arcade.bedwars.BedWars.getInstance(), () -> {
                        team.getPlayers().forEach(p -> {
                            next.applyEnchantment(p);
                            p.updateInventory();
                        });
                    }, 1L);
                });

        return icon;
    }

    protected Item buildAutoRegeneration(Team team) {
        Item icon = Item.of(Material.BEACON)
                .flags(ItemFlag.values());

        TeamUpgrade upgrade = team.getUpgrade();

        final int cost = 1;
        final boolean buy = BukkitUtil.getItemAmount(getPlayer(), Material.DIAMOND) >= cost;

        if (upgrade.isAutoRegeneration())
            icon.unsafeEnchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Invoque um campo de regeneração",
                "§7em volta da sua ilha.",
                ""
        ));

        if (upgrade.isAutoRegeneration())
            lore.add("§cCampo em ação.");
        else
            lore.addAll(Arrays.asList(
                    "§7Custo: §b" + cost + " diamante",
                    "",
                    buy ? "§eClique para adquirir!" : "§cVocê não tem diamantes suficientes."));

        icon.name((buy ? "§a" : "§c") + "Regeneração na ilha")
                .lore(lore)
                .click(event -> {
                    if (upgrade.isAutoRegeneration()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cO campo de regeneração já foi ativado.");

                        return;
                    }

                    if (!buy) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(BedOre.DIAMOND, cost) + " diamante.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    upgrade.setAutoRegeneration(true);
                    team.setUpgrade(upgrade);

                    user.removeOre(BedOre.DIAMOND, cost);

                    team.getPlayers().forEach(player -> player.sendMessage(team.getColor()
                            + getPlayer().getName() + "§e ativou o campo de regeneração."));

                    handle();
                });

        return icon;
    }

    protected Item buildTrapItem(Team team, TrapType type) {
        Item icon = type.getIcon().clone()
                .flags(ItemFlag.values());

        List<String> description = new ArrayList<>(type.getDescription());

        boolean has = team.getTraps().stream().anyMatch(trap -> trap.getType().equals(type)),
                canPurchase = type.canPurchase(getPlayer());

        int totalTraps = team.getTraps().size();

        description.addAll(Arrays.asList(
                "",
                "§7Armadilhas ativas: §e" + totalTraps + "/3",
                "",
                "§7Custo: §b" + type.getCost() + " diamante" + (type.getCost() > 1 ? "s" : ""),
                "",
                totalTraps >= 3 ? "§cLimite de armadilhas atingido." :
                has ? "§cArmadilha já adquirida." : canPurchase
                        ? "§eClique para comprar!"
                        : "§cVocê não tem diamantes suficientes."
        ));

        icon.name(((has || !canPurchase || totalTraps >= 3) ? "§c" : "§a") + type.getName());
        icon.lore(description);

        icon.click(event -> {
            if (totalTraps >= 3) {
                sound(MenuSound.ERROR);
                getPlayer().sendMessage("§cVocê já atingiu o limite de armadilhas.");
                return;
            }

            if (has) {
                sound(MenuSound.ERROR);
                getPlayer().sendMessage("§cVocê já adquiriu a armadilha " + type.getName());
                return;
            }

            if (!canPurchase) {
                sound(MenuSound.ERROR);
                getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                        "Faltam " + user.getRemainingOre(type.getOre(), type.getCost()) + " diamantes.");
                return;
            }

            sound(MenuSound.SUCCESS);

            user.removeOre(type.getOre(), type.getCost());

            team.getTraps().add(new Trap(type, TrapCategory.of(totalTraps)));

            team.getPlayers().forEach(player -> ProtocolHandler.sendBar(player, team.getColor() + getPlayer().getName()
                    + "§e adquiriu §6Armadilha " + type.getName()));

            handle();
        });

        return icon;
    }

    protected Item buildTrap(Team team) {
        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Use as armadilhas para",
                "§7distrair os invasores da sua ilha.",
                "",
                "§7Suas armadilhas §8(" + team.getTraps().size() + "/3)§7:"
        ));

        if (!team.getTraps().isEmpty()) {
            Queue<Trap> traps = team.getTraps();

            StringBuilder builder = new StringBuilder();

            builder.append(" ");

            boolean end = false;
            int index = 1;
            for (Trap trap : traps) {
                if (trap == null) continue;

                if (index >= traps.size()) end = true;

                builder.append("§7").append(trap.getName()).append(end ? "." : ", ");
                index++;
            }

            lore.add(builder.toString());
        } else
            lore.add("§7Não há armadilhas.");

        lore.add("");
        lore.add("§eClique para ver!");

        return new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1YmNmZjJlNzRkZWVkMzdhMzE5YTFmNDA0ZTcwZDA2YTVmMzYwY2FjZWU5OWM3MTM0NmYzODU2MGNiZDcyYSJ9fX0=")
                .name("§aArmadilhas")
                .lore(lore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);

                    new TrapMenu(getPlayer(), this, team).handle();
                });
    }
}
