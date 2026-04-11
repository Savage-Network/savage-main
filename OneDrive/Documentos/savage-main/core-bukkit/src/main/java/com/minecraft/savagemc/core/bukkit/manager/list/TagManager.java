package com.minecraft.savagemc.core.bukkit.manager.list;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.savagemc.core.bukkit.api.protocol.ProtocolHandler;
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

import static com.minecraft.savagemc.core.Savage.*;

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
        Account account = getAccountController().of(player.getUniqueId());

        if (account != null)
            updateTag(account);
    }

    public static void updateTag(Account account, Tag tag) {
        updateTagWithSuffix(account, tag, null);
    }
    
    public static void updateTagWithSuffix(Account account, Tag tag, String customSuffix) {
        Player player = account.player();

        if (player == null || !player.isOnline()) return;

        if (tag == null) tag = Tag.MEMBER;

        // Nome do team deve ter no máximo 16 caracteres
        String teamName = "tag:" + tag.getOrder() + ":" + player.getEntityId();

        String prefix = getAdjustedPrefix(account.getTagPrefix(), tag);
        
        // Usar customSuffix se fornecido, caso contrário usar clan tag
        String suffix;
        if (customSuffix != null) {
            suffix = customSuffix;
        } else {
            suffix = !account.isUsingFake() ? (account.hasClan() && account.getToggle().isAllowClanTag() ? " " + account.getClanTag() : "") : "";
        }

        // displayName com suffix completo (para chat)
        String displayName = prefix + player.getName() + suffix;
        player.setDisplayName(displayName);
        
        // Definir playerListName com suffix para garantir que apareça no tab
        // Truncar se necessário para caber no limite de 16 caracteres do Minecraft
        String playerListName = prefix + player.getName() + suffix;
        if (playerListName.length() > 40) {
            playerListName = truncateForScoreboard(playerListName, 40);
        }
        player.setPlayerListName(playerListName);

        // Criar/atualizar team no próprio scoreboard do jogador PRIMEIRO
        Scoreboard playerScoreboard = getOrCreateScoreboard(player);
        Team playerOwnTeam = playerScoreboard.getTeam(teamName);
        if (playerOwnTeam == null) {
            playerOwnTeam = playerScoreboard.registerNewTeam(teamName);
            playerOwnTeam.setCanSeeFriendlyInvisibles(false);
            playerOwnTeam.setNameTagVisibility(org.bukkit.scoreboard.NameTagVisibility.ALWAYS);
        }
        playerOwnTeam.setPrefix(truncateForScoreboard(prefix, 16));
        playerOwnTeam.setSuffix(truncateForScoreboard(suffix, 16));
        if (!playerOwnTeam.hasEntry(player.getName())) {
            playerOwnTeam.addEntry(player.getName());
        }

        // Atualizar para TODOS os outros jogadores online
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(player)) continue; // Já fizemos o próprio jogador acima
            
            Scoreboard viewerScoreboard = getOrCreateScoreboard(viewer);
            
            // Criar/atualizar team no scoreboard do viewer
            Team viewerTeam = viewerScoreboard.getTeam(teamName);
            if (viewerTeam == null) {
                viewerTeam = viewerScoreboard.registerNewTeam(teamName);
                viewerTeam.setCanSeeFriendlyInvisibles(false);
                viewerTeam.setNameTagVisibility(org.bukkit.scoreboard.NameTagVisibility.ALWAYS);
            }
            
            // Atualizar prefix e suffix
            viewerTeam.setPrefix(truncateForScoreboard(prefix, 16));
            viewerTeam.setSuffix(truncateForScoreboard(suffix, 16));
            
            // Adicionar o jogador ao team se ainda não estiver
            if (!viewerTeam.hasEntry(player.getName())) {
                viewerTeam.addEntry(player.getName());
            }
        }
        
        // Atualizar todos os outros jogadores no scoreboard deste jogador
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue; // Já fizemos o próprio jogador no início
            
            Account otherAccount = getAccountController().of(other.getUniqueId());
            if (otherAccount != null) {
                Tag otherTag = UserModel.has(otherAccount.getId()) ? UserModel.of(otherAccount.getId()).getTag() : otherAccount.getTag();
                if (otherTag == null) otherTag = Tag.MEMBER;
                
                String otherTeamName = "tag:" + otherTag.getOrder() + ":" + other.getEntityId();
                String otherPrefix = getAdjustedPrefix(otherAccount.getTagPrefix(), otherTag);
                String otherSuffix = !otherAccount.isUsingFake() ? (otherAccount.hasClan() && otherAccount.getToggle().isAllowClanTag() ? " " + otherAccount.getClanTag() : "") : "";
                
                Team otherTeam = playerScoreboard.getTeam(otherTeamName);
                if (otherTeam == null) {
                    otherTeam = playerScoreboard.registerNewTeam(otherTeamName);
                    otherTeam.setCanSeeFriendlyInvisibles(false);
                    otherTeam.setNameTagVisibility(org.bukkit.scoreboard.NameTagVisibility.ALWAYS);
                }
                
                otherTeam.setPrefix(truncateForScoreboard(otherPrefix, 16));
                otherTeam.setSuffix(truncateForScoreboard(otherSuffix, 16));
                
                if (!otherTeam.hasEntry(other.getName())) {
                    otherTeam.addEntry(other.getName());
                }
            }
        }
    }

    private static Scoreboard getOrCreateScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null || scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }
        return scoreboard;
    }
    
    private static String truncateForScoreboard(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        
        // Truncar mantendo códigos de cor válidos
        StringBuilder result = new StringBuilder();
        int length = 0;
        
        for (int i = 0; i < text.length() && length < maxLength; i++) {
            char c = text.charAt(i);
            
            // Se encontrar §, adicionar ele e o próximo caractere juntos
            if (c == '§' && i + 1 < text.length()) {
                // Só adicionar se ainda couber
                if (length + 2 <= maxLength) {
                    result.append(c);
                    result.append(text.charAt(i + 1));
                    i++; // Pular o próximo char
                    length += 2;
                }
            } else {
                if (length < maxLength) {
                    result.append(c);
                    length++;
                }
            }
        }
        
        return result.toString();
    }

    private static String getAdjustedPrefix(TagPrefix prefixType, Tag tag) {
        return tag.getByPrefix(prefixType);
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
