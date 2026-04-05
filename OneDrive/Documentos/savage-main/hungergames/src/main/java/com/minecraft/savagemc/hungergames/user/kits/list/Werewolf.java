package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.base.Strings;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.event.game.GameTimeEvent;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Werewolf extends Kit {

    public Werewolf(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.IRON_BARDING));
        setItems(new ItemFactory(Material.WATCH).setName("§aRelógio").setDescription("§7Kit Description").getStack());
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
        setActive(false, false);
    }

    private final double NIGHT_TIME = 13600;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.getAction() != Action.PHYSICAL && isItem(event.getItem())) {

            Player player = event.getPlayer();

            double currentTime = player.getWorld().getTime();

            if (currentTime <= NIGHT_TIME) {
                String percentage = Constants.SIMPLE_DECIMAL_FORMAT.format((currentTime * 100) / NIGHT_TIME);
                String seconds = Constants.SIMPLE_DECIMAL_FORMAT.format((NIGHT_TIME - currentTime) / 20);

                player.sendMessage("§eVocê se transformará em " + seconds + "s.");
                player.sendMessage(progress((int) currentTime, (int) NIGHT_TIME, 30) + " §7(" + percentage + "%)");
            } else {
                player.sendMessage("§cVocê já se transformou!");
            }
        }
    }

    @EventHandler
    public void onGameTime(GameTimeEvent event) {

        World world = getWorld();

        if (world.getTime() > NIGHT_TIME) {
            getPlugin().getUserStorage().getAliveUsers().stream().filter(this::isUser).forEach(user -> {
                user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1), true);
                user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 60, 0), true);
            });
        }
    }

    public String progress(int current, int max, int totalBars) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);
        return Strings.repeat("§a§m ", progressBars)
                + Strings.repeat("§f§m ", totalBars - progressBars) + "§r";
    }

}
