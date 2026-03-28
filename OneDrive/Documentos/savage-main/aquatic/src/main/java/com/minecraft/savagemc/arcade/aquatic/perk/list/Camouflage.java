package com.minecraft.savagemc.arcade.aquatic.perk.list;

import com.minecraft.savagemc.arcade.aquatic.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.aquatic.perk.Perk;
import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Camouflage extends Perk implements Listener {

    private static final Map<UUID, Long> map = new ConcurrentHashMap<>();

    public Camouflage() {
        super("Camuflagem", Item.of(Material.THIN_GLASS), RankType.MEMBER, 0, 15);

        setLore("§7Camufle-se nas sombras dos", "§7seus inimigos.");
        setItems(Item.of(Material.THIN_GLASS)
                .name("§eCamuflar")
                .interact(event -> {
                    Player player = event.getPlayer();

                    if (!isUsing(player.getUniqueId())) return;
                    if (hasCooldown(player)) return;

                    User user = (User) User.of(player.getUniqueId());

                    Arena arena = user.getArena();

                    // Esconder jogador dos membros da arena
                    for (Player target : arena.getPlayers()) {
                        if (player.equals(target)) continue;

                        ProtocolHandler.hideArmor(player, target);
                        ProtocolHandler.hidePlayerName(player, target);
                    }

                    map.putIfAbsent(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5));

                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 5, 1));

                    player.sendMessage("§aA sua camuflagem foi ativada! Você ficará invisível por 5 segundos.");
                    player.playSound(Sound.BAT_LOOP);

                    applyCooldown(player);
                }));
    }

    @EventHandler(ignoreCancelled = true)
    public void onHitCamouflage(PlayerDamageTargetEvent event) {
        Player player = event.getTarget();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        if (!event.isCancelled() && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);

            arena.getPlayers().forEach(receiver -> {
                ProtocolHandler.showArmor(player, receiver);
                ProtocolHandler.showPlayerName(player, receiver);
            });
        }
    }

    @EventHandler
    public void onCamouflage(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {

            Iterator<Map.Entry<UUID, Long>> iterator = map.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, Long> entry = iterator.next();

                Player player = Bukkit.getPlayer(entry.getKey());

                if (player == null) {
                    iterator.remove();
                    continue;
                }

                long time = entry.getValue();

                if (time <= System.currentTimeMillis()) {
                    iterator.remove();

                    // Acabou o tempo, mostrar o jogador
                    User user = (User) User.of(player.getUniqueId());

                    if (user != null) {
                        Arena arena = user.getArena();

                        for (Player target : arena.getPlayers()) {
                            if (player.equals(target)) continue;

                            ProtocolHandler.showArmor(player, target);
                            ProtocolHandler.showPlayerName(player, target);
                        }

                        player.removePotionEffect(PotionEffectType.INVISIBILITY);

                        player.sendMessage("§eA sua camuflagem acabou!");
                    }
                }
            }
        }
    }
}
