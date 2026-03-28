package com.minecraft.savagemc.core.bukkit.manager.list;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.savagemc.core.bukkit.user.UserModel;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TagManager {

    public static List<Tag> getAvailableTags(Account account) {
        return Arrays.stream(Tag.values())
                .filter(account::hasTag)
                .collect(Collectors.toList());
    }

    protected static TextComponent create(Account account, Tag tag, boolean end) {
        TextComponent message = new TextComponent(tag.getColoredName());

        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                "§7Prévia:"
                        + "\n§8> " + tag.getByPrefix(account.getTagPrefix()) + account.getNickname()
                        + "\n\n" + (account.isUsingTag(tag) ? "§cJá selecionada." : "§eClique para escolher.")
        )));

        message.addExtra("§r" + (end ? "." : ", "));

        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tag " + tag.getName().toLowerCase()));

        return message;
    }

    public static void sendTags(Account account) {
        List<Tag> tags = getAvailableTags(account);

        TextComponent message = new TextComponent("§aSuas tags: ");

        boolean end = false;

        int index = 1;
        for (Tag tag : tags) {
            if (index >= tags.size())
                end = true;

            message.addExtra(create(account, tag, end));
            index++;
        }

        message.addExtra(new TextComponent("§r"));

        account.send(message);
    }

    public static void updateTag(Account account) {
        updateTag(account, UserModel.has(account.getId()) ? UserModel.of(account.getId()).getTag() : account.getTag());
    }

    public static void updateTag(Player player) {
        Account account = Savage.getAccountController().of(player.getUniqueId());

        if (account != null)
            updateTag(account);
    }

    public static void updateTag(Account account, Tag tag) {
        Player player = account.player();

        if (player == null || !player.isOnline()) return;

        if (tag == null) tag = Tag.MEMBER;

        String order = "tag:" + tag.getOrder() + ":" + player.getEntityId();

        String prefix = getAdjustedPrefix(account.getTagPrefix(), tag);
        String suffix = !account.isUsingFake() ? (account.hasClan() && account.getToggle().isAllowClanTag() ? " §8[" + account.getClanTag() + "]" : "") : "";

        // Scoreboard sem suffix para evitar limite de 16 chars e kick
        Team team = createOrResetTeam(getOrCreateScoreboard(player), order, prefix, "");
        team.addEntry(player.getName());

        // displayName e playerListName com suffix completo (sem limite de 16 chars)
        String displayName = prefix + player.getName() + suffix;
        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);
        player.setScoreboard(team.getScoreboard());

        // Atualizar para todos os jogadores online
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;

            applyTagForViewer(player, onlinePlayer, tag);
        }
    }

    private static void applyTagForViewer(Player player, Player viewer, Tag tag) {
        Account viewerAccount = Savage.getAccountController().of(viewer.getUniqueId());
        Account playerAccount = Savage.getAccountController().of(player.getUniqueId());

        if (viewerAccount == null || playerAccount == null) return;

        String viewerPrefix = getAdjustedPrefix(viewerAccount.getTagPrefix(), tag);
        String viewerClanTag = !viewerAccount.isUsingFake() ? (viewerAccount.hasClan() && viewerAccount.getToggle().isAllowClanTag()
                ? " §8[" + viewerAccount.getClanTag() + "]" : "") : "";

        String playerOrder = "tag:" + tag.getOrder() + ":" + player.getEntityId();

        // Scoreboard do viewer sem suffix do player (evita limite de 16 chars)
        Team viewerTeam = createOrResetTeam(getOrCreateScoreboard(viewer), playerOrder, viewerPrefix, "");
        viewerTeam.addEntry(player.getName());

        // Atualizar display do viewer com sua própria clan tag
        String viewerDisplay = viewerPrefix + viewer.getName() + viewerClanTag;
        viewer.setDisplayName(viewerDisplay);
        viewer.setPlayerListName(viewerDisplay);
    }

    private static Scoreboard getOrCreateScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null || scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard()))
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(scoreboard);
        return scoreboard;
    }

    private static Team createOrResetTeam(Scoreboard scoreboard, String name, String prefix, String suffix) {
        Team team = scoreboard.getTeam(name);
        if (team != null) scoreboard.getTeam(name).unregister();
        team = scoreboard.registerNewTeam(name);
        team.setCanSeeFriendlyInvisibles(false);
        team.setPrefix(prefix.length() > 16 ? prefix.substring(0, 16) : prefix);
        team.setSuffix(suffix.length() > 16 ? suffix.substring(0, 16) : suffix);
        return team;
    }

    private static String getAdjustedPrefix(TagPrefix prefixType, Tag tag) {
        String prefix = tag.getByPrefix(prefixType);
        if (prefix.length() > 16) prefix = prefix.substring(0, 16);
        return prefix;
    }

    private static String getAdjustedSuffix(String suffix) {
        if (suffix.length() > 16) suffix = suffix.substring(0, 16);
        return suffix;
    }

    public static void removeTag(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());

        player.getScoreboard().getTeams().stream()
                .filter(team -> team.getName().startsWith("tag:"))
                .forEach(Team::unregister);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player) || onlinePlayer.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard()))
                continue;

            onlinePlayer.getScoreboard().getTeams().forEach(team -> removeEntries(team, player.getName()));
        }
    }

    private static void removeEntries(Team team, String entry) {
        if (team.getName().startsWith("tag:")) {
            team.removeEntry(entry);
            if (team.getEntries().isEmpty()) team.unregister();
        }
    }
}
