package com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.structure.trap.enums;

import com.minecraft.savagemc.arcade.bedwars.structure.team.objects.upgrade.structure.trap.action.TrapAction;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum TrapType {

    FATIGUE("Fadiga", Material.GOLD_PICKAXE, BedOre.DIAMOND, 1, Collections.singletonList(
            "§7Cause fadiga nos invasores."
    ), ((executor, team) -> executor.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 5, 0)))),

    ITS_A_TRAP("É uma Cilada!", Material.TRIPWIRE_HOOK, BedOre.DIAMOND, 1, Arrays.asList(
            "§7Cause Cegueira e Lentidão",
            "§7por 8 segundos em invasores."
    ), (executor, team) -> {
        executor.addPotionEffects(
                new PotionEffect(PotionEffectType.BLINDNESS, 20 * 8, 0),
                new PotionEffect(PotionEffectType.SLOW, 20 * 8, 0)
        );
    });

    private final String name;

    private final Item icon;
    private final BedOre ore;

    private final int cost;
    private final List<String> description;

    private final TrapAction action;

    TrapType(String name, Material icon, BedOre ore, int cost, List<String> description, TrapAction action) {
        this(name, Item.of(icon), ore, cost, description, action);
    }

    public static TrapType of(String name) {
        return Stream.of(values())
                .filter(trap -> trap.name().equalsIgnoreCase(name) || trap.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean canPurchase(Player player) {
        return BukkitUtil.getItemAmount(player, ore.getMaterial()) >= cost;
    }
}
