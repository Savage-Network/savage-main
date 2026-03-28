package com.minecraft.savagemc.core.bukkit.listener.account;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.bukkit.api.option.ServerOptions;
import com.minecraft.savagemc.core.bukkit.event.type.server.ServerChatEvent;
import com.minecraft.savagemc.core.util.Util;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InfoListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void chat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();

        Account account = Savage.getAccountController().of(player.getUniqueId());

        if (account == null) return;

        if (account.hasCooldown(Constant.CHAT_COOLDOWN_KEY)) {
            account.send("§cAguarde " + account.getFormattedCooldown(Constant.CHAT_COOLDOWN_KEY) + " para conversar novamente.");
            return;
        }

        if (!account.isStaffer() && !ServerOptions.CHAT_ENABLED) {
            account.send("§cO chat do servidor está desativado.");
            return;
        }

        String message = event.getMessage();

        Predicate<Account> predicate = target -> target != null && target.player() != null && target.player().inWorld(player.getWorld())
                && !target.hasBlock(account);

        if (ServerOptions.DEFAULT_CHAT) {
            Tag tag = account.getTag();

            Savage.getAccountController()
                    .filter(predicate)
                    .forEach(target -> {
                        TextComponent text = new TextComponent(tag.getByPrefix(target.getTagPrefix()) + account.getNickname() + ": "
                                + (account.hasRank(RankType.NATURE) ? "§f" + Util.color(message) : "§7" + message));

                        target.send(text);
                    });
        } else {
            // Chamando chat customizado

            List<Player> recipients = Savage.getAccountController()
                    .filter(predicate)
                    .stream().map(Account::player)
                    .collect(Collectors.toList());

            new ServerChatEvent(player, message, recipients).call();
        }

        if (!account.hasRank(RankType.NATURE))
            account.setCooldown(Constant.CHAT_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(3));

        Savage.Savage.Logger().info("[Chat] " + player.getName() + ": " + message);
    }
}
