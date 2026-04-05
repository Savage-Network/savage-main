package com.minecraft.savagemc.core.account.context.objects.tag;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.savagemc.core.account.context.objects.tag.role.TagRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Getter
public enum Tag {

    MEMBER("Membro", "Z", "§7", emptyList(), RankType.MEMBER, TagRole.NONE, ChatColor.GRAY),

    /* Custom Role Tags */
    RED("Vermelho",   "§c§lV §r§c", "A", TagRole.COLORED, ChatColor.RED),
    BLUE("Azul",      "§9§lA §r§9", "B", TagRole.COLORED, ChatColor.BLUE),
    PINK("Rosa",      "§d§lR §r§d", "C", TagRole.COLORED, ChatColor.LIGHT_PURPLE),
    ORANGE("Laranja", "§6§lL §r§6", "D", TagRole.COLORED, ChatColor.GOLD),
    YELLOW("Amarelo", "§e§lA §r§e", "E", TagRole.COLORED, ChatColor.YELLOW),
    GREEN("Verde",    "§a§lV §r§a", "F", TagRole.COLORED, ChatColor.GREEN),
    AQUA("Ciano",     "§b§lC §r§b", "G", TagRole.COLORED, ChatColor.AQUA),
    GRAY("Cinza",     "§8§lC §r§8", "H", TagRole.COLORED, ChatColor.DARK_GRAY),
    MASKED("Camuflada", "X", "§7§k", TagRole.COLORED, ChatColor.MAGIC),
    SPECTATOR("Espectador", "X", "§7", emptyList(), RankType.MEMBER, TagRole.COLORED, ChatColor.GRAY),

/* Special */
GOAT("Goat",     "§e§lGOAT §r§e", TagRole.SPECIAL, ChatColor.YELLOW),

/* VIP */
VIP("VIP",           "T", "§a§lVIP §a",         emptyList(), RankType.VIP,        TagRole.VIP, ChatColor.GREEN),
NATURE("Nature",     "S", "§e§lNATURE §e",      emptyList(), RankType.NATURE,     TagRole.VIP, ChatColor.YELLOW),
EMBER("Ember",       "R", "§c§lEMBER §c",       emptyList(), RankType.EMBER,      TagRole.VIP, ChatColor.RED),
SAVAGE("Savage",     "Q", "§2§lSAVAGE §2",      emptyList(), RankType.SAVAGE,     TagRole.VIP, ChatColor.DARK_GREEN),

BETA("Beta",         "P", "§1§lBETA §1",        emptyList(), RankType.BETA,       TagRole.VIP, ChatColor.DARK_BLUE),
TAG_2026("2026",     "O", "§b§l2026 §b",        emptyList(), RankType.TAG_2026,   TagRole.EXCLUSIVE, ChatColor.AQUA),
CHAMPION("Champion", "N", "§6§lCHAMPION §6",    emptyList(), RankType.CHAMPION,   TagRole.EXCLUSIVE, ChatColor.GOLD),

/* Mídia / Creator */
TIKTOKER("Tiktoker",   "M", "§b§lTIKTOKER §b",  emptyList(), RankType.TIKTOKER,   TagRole.VIP, ChatColor.AQUA),
STREAMER("Streamer",   "L", "§b§lSTREAMER §b",  emptyList(), RankType.STREAMER,   TagRole.VIP, ChatColor.AQUA),
YOUTUBER("Youtuber",   "K", "§b§lYOUTUBER §b",  emptyList(), RankType.YOUTUBER,   TagRole.VIP, ChatColor.AQUA),
MIDIA("Mídia",         "J", "§b§lMÍDIA §b",     emptyList(), RankType.MIDIA,      TagRole.VIP, ChatColor.AQUA),
MIDIA_PLUS("Mídia+",   "I", "§3§lMÍDIA+ §3",    singletonList("midia+"), RankType.MIDIA_PLUS, TagRole.VIP, ChatColor.DARK_AQUA),

/* Staff */
STUDIO("Studio",     "H", "§2§lSTUDIO §2",    emptyList(), RankType.STUDIO,   TagRole.STAFF, ChatColor.DARK_GREEN),
BUILDER("Builder",   "G", "§2§lBUILDER §2",   emptyList(), RankType.BUILDER,  TagRole.STAFF, ChatColor.DARK_GREEN),
HELPER("Helper",     "F", "§9§lHELPER §9",    emptyList(), RankType.HELPER,   TagRole.STAFF, ChatColor.BLUE),
TRIAL("Trial",       "E", "§5§lTRIAL §5",     emptyList(), RankType.TRIAL,    TagRole.STAFF, ChatColor.DARK_PURPLE),
MOD("Mod",           "D", "§5§lMOD §5",       emptyList(), RankType.MOD,      TagRole.STAFF, ChatColor.DARK_PURPLE),
MODPLUS("Mod+",      "C", "§5§lMOD§5+ §5",    emptyList(), RankType.MODPLUS,  TagRole.STAFF, ChatColor.DARK_PURPLE),
ADMIN("Admin",       "B", "§4§lADMIN §4",     emptyList(), RankType.ADMIN,    TagRole.STAFF, ChatColor.DARK_RED),
CHEFE("Chefe",       "A", "§4§lCHEFE §4",     emptyList(), RankType.CHIEF,    TagRole.STAFF, ChatColor.DARK_RED);

    // Fields
    private final String name, order, prefix;
    private final List<String> surnames;
    private final RankType source;
    private final TagRole role;
    private final ChatColor color;
    private final List<UUID> onlyThese;

    // Constructor with all parameters
    Tag(String name, String order, String prefix, List<String> surnames, RankType source, TagRole role, ChatColor color, List<UUID> onlyThese) {
        this.name = name;
        this.order = order;
        this.prefix = prefix;
        this.surnames = surnames;
        this.source = source;
        this.role = role;
        this.color = color;
        this.onlyThese = onlyThese;
    }

    // Constructor without onlyThese
    Tag(String name, String order, String prefix, List<String> surnames, RankType source, TagRole role, ChatColor color) {
        this(name, order, prefix, surnames, source, role, color, new ArrayList<>());
    }

    // Constructor for colored tags
    Tag(String name, String prefix, String order, TagRole role, ChatColor color) {
        this(name, order, prefix, emptyList(), RankType.MEMBER, role, color, new ArrayList<>());
    }

    // Constructor for special tags
    Tag(String name, String prefix, TagRole role, ChatColor color) {
        this(name, "Y", prefix, emptyList(), RankType.MEMBER, role, color, new ArrayList<>());
    }

    public static Tag of(String name) {
        return Arrays.stream(values())
                .filter(tag -> tag.name().equalsIgnoreCase(name)
                        || tag.getName().equalsIgnoreCase(name)
                        || tag.getSurnames().stream().anyMatch(s -> s.equalsIgnoreCase(name)))
                .findFirst()
                .orElse(null);
    }

    public static Tag of(RankType source) {
        if (source.equals(RankType.CHIEF)) return CHEFE;

        return Arrays.stream(values())
                .filter(tag -> tag.getSource().equals(source))
                .findFirst()
                .orElse(null);
    }

    // Método para comparar tags pela ordem correta (A = maior, Z = menor)
    public static int compareByOrder(Tag tag1, Tag tag2) {
        return tag1.getOrder().compareTo(tag2.getOrder());
    }

    // Método para verificar se uma tag é maior ou igual a outra
    public boolean isHigherOrEqual(Tag other) {
        return compareByOrder(this, other) <= 0; // A vem antes de Z, então <= significa maior ou igual
    }

    // Método para verificar se uma tag é maior que outra
    public boolean isHigherThan(Tag other) {
        return compareByOrder(this, other) < 0; // A vem antes de Z, então < significa maior
    }

    public String getByPrefix(TagPrefix prefix) {
        if (this == SPECTATOR || this == MEMBER) return getPrefix();

        String prefixRegex = getPrefix().split(" ")[0];
        String result = prefixRegex;

        switch (prefix) {
            case DEFAULT:            result = getPrefix(); break;
            case COLOR:              result = getColor().toString(); break;
            case BRACKETS: {
                String n = isRole(TagRole.COLORED)
                        ? ChatColor.stripColor(getPrefix()).split(" ")[0]
                        : getName().toUpperCase();
                result = getColor() + "[" + n + "] ";
                break;
            }
            case BRACKETS_NORMAL_CASE: {
                String n = isRole(TagRole.COLORED)
                        ? ChatColor.stripColor(getPrefix()).split(" ")[0]
                        : getName();
                result = getColor() + "[" + n + "] ";
                break;
            }
            case DEFAULT_WHITE: result = prefixRegex + " "; break;
            case DEFAULT_GRAY:  result = prefixRegex + " §7"; break;
        }

        return result;
    }

    // Método para remover formatação mas manter a cor
    public String removeBoldKeepColor(String text) {
        return text.replaceAll("§l", "");
    }

    public boolean isOnlyThese(UUID id) {
        return !onlyThese.isEmpty() && onlyThese.contains(id);
    }

    public boolean isRole(TagRole role) {
        return this.role.equals(role);
    }

    public String getColoredName() {
        return color + name;
    }

    public String getPermission() {
        return "tag." + name.toLowerCase();
    }

    // Método para obter todas as tags que uma tag pode acessar
    public List<Tag> getAccessibleTags() {
        List<Tag> accessible = new ArrayList<>();
        accessible.add(this); // Sempre pode acessar a própria tag

        switch (this) {
            case CHEFE:
            case ADMIN:
                // Chefe e Admin podem acessar todas as tags
                Collections.addAll(accessible, Tag.values());
                break;

            case MODPLUS:
                // Mod+ pode acessar a própria, todas abaixo dele, exceto 2026 e champion
                for (Tag tag : Tag.values()) {
                    if (tag != TAG_2026 && tag != CHAMPION && 
                        (tag.isHigherOrEqual(this) || tag == this)) {
                        if (!accessible.contains(tag)) {
                            accessible.add(tag);
                        }
                    }
                }
                break;

            case MOD:
            case TRIAL:
                // Mod e Trial podem acessar a própria, todas abaixo dele, exceto 2026, champion, midia+ e midia
                for (Tag tag : Tag.values()) {
                    if (tag != TAG_2026 && tag != CHAMPION && tag != MIDIA_PLUS && tag != MIDIA &&
                        (tag.isHigherOrEqual(this) || tag == this)) {
                        if (!accessible.contains(tag)) {
                            accessible.add(tag);
                        }
                    }
                }
                break;

            case HELPER:
                // Helper pode acessar a própria, todas abaixo dele, exceto 2026, champion, midia+, midia, beta, studio e builder
                for (Tag tag : Tag.values()) {
                    if (tag != TAG_2026 && tag != CHAMPION && tag != MIDIA_PLUS && tag != MIDIA &&
                        tag != BETA && tag != STUDIO && tag != BUILDER &&
                        (tag.isHigherOrEqual(this) || tag == this)) {
                        if (!accessible.contains(tag)) {
                            accessible.add(tag);
                        }
                    }
                }
                break;

            case MIDIA_PLUS:
                // Mídia+ pode acessar a própria, todas abaixo dele, exceto 2026 e champion
                for (Tag tag : Tag.values()) {
                    if (tag != TAG_2026 && tag != CHAMPION &&
                        (tag.isHigherOrEqual(this) || tag == this)) {
                        if (!accessible.contains(tag)) {
                            accessible.add(tag);
                        }
                    }
                }
                break;

            case MIDIA:
                // Mídia pode acessar a própria, todas abaixo dele, exceto 2026, champion, midia+
                for (Tag tag : Tag.values()) {
                    if (tag != TAG_2026 && tag != CHAMPION && tag != MIDIA_PLUS &&
                        (tag.isHigherOrEqual(this) || tag == this)) {
                        if (!accessible.contains(tag)) {
                            accessible.add(tag);
                        }
                    }
                }
                break;

            case TIKTOKER:
            case YOUTUBER:
            case STREAMER:
                // Apenas a própria tag
                break;

            case CHAMPION:
            case TAG_2026:
                // Apenas a própria tag
                break;

            case BETA:
                // Beta pode acessar a própria, ember, nature e vip
                Collections.addAll(accessible, EMBER, NATURE, VIP);
                break;

            case SAVAGE:
                // Savage pode acessar a própria, nature e vip
                Collections.addAll(accessible, NATURE, VIP);
                break;

            case NATURE:
                // Nature pode acessar a própria e vip
                accessible.add(VIP);
                break;

            case VIP:
                // Apenas a própria tag
                break;

            default:
                // Para outras tags (colored, member, etc), apenas a própria
                break;
        }

        // Remover duplicatas e ordenar por hierarquia
        return accessible.stream()
                .distinct()
                .sorted(Tag::compareByOrder)
                .collect(java.util.stream.Collectors.toList());
    }

    // Método para verificar se uma tag pode acessar outra tag específica
    public boolean canAccessTag(Tag targetTag) {
        return getAccessibleTags().contains(targetTag);
    }

    // Método utilitário para exibir todas as permissões de uma tag (para debug/verificação)
    public String getPermissionSummary() {
        List<Tag> accessible = getAccessibleTags();
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" pode acessar: ");
        
        for (int i = 0; i < accessible.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(accessible.get(i).name());
        }
        
        return sb.toString();
    }

    // Método estático para exibir todas as permissões do sistema (para debug)
    public static void displayAllPermissions() {
        System.out.println("=== SISTEMA DE PERMISSÕES DE TAGS ===");
        for (Tag tag : Tag.values()) {
            if (tag != MEMBER && tag != SPECTATOR) { // Ignora tags básicas
                System.out.println(tag.getPermissionSummary());
            }
        }
        System.out.println("=====================================");
    }
}
