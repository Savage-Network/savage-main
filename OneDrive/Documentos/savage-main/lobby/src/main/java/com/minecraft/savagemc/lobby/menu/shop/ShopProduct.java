package com.minecraft.savagemc.lobby.menu.shop;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum ShopProduct {

    BETA(
            "#0352",
            "§b§lBETA",
            Material.INK_SACK,
            12,
            30.00,
            true,
            ShopCategory.RANKS,
            Arrays.asList(
                    "§7Permanente.",
                    "",
                    "§a✔ §7Voo no Lobby;",
                    "§a✔ §7Selecionar mapas;",
                    "§a✔ §7Acesso à kits extras;",
                    "§a✔ §7Acesso antecipado a modos;",
                    "§a✔ §7Entrar em salas lotadas;",
                    "§a✔ §7Tag §bBETA"
            )
    ),

    VIP(
            "#0351",
            "§a§lVIP",
            Material.INK_SACK,
            10,
            25.00,
            true,
            ShopCategory.RANKS,
            Arrays.asList(
                    "§7Permanente.",
                    "",
                    "§a✔ §7Voo no Lobby;",
                    "§a✔ §7Selecionar mapas;",
                    "§a✔ §7Anúncia ao entrar;",
                    "§a✔ §7Preferências VIP;",
                    "§7E muito mais!"
            )
    ),

    SAVAGE(
            "#0353",
            "§2§lSAVAGE",
            Material.EMERALD,
            0,
            27.99,
            true,
            ShopCategory.RANKS,
            Arrays.asList(
                    "§7Permanente.",
                    "",
                    "§a✔ §7Todos os benefícios VIP;",
                    "§a✔ §7Todos os benefícios BETA;",
                    "§a✔ §7Acesso prioritário;",
                    "§a✔ §7Kits exclusivos;",
                    "§a✔ §7Tag §2SAVAGE;",
                    "§7E muito mais!"
            )
    );

    private final String id;
    private final String name;
    private final Material icon;
    private final int data;
    private final double price;
    private final boolean permanent;
    private final ShopCategory category;
    private final List<String> description;

    ShopProduct(String id, String name, Material icon, int data, double price, boolean permanent, ShopCategory category, List<String> description) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.data = data;
        this.price = price;
        this.permanent = permanent;
        this.category = category;
        this.description = description;
    }

    public List<String> getLore() {
        List<String> lore = new ArrayList<>(description);
        lore.add("");
        lore.add("§7Preço: §aR$" + String.format("%.2f", price));
        lore.add("");
        lore.add("§eClique para adquirir!");
        return lore;
    }

    public static List<ShopProduct> getAvailableProducts(Account account) {
        List<ShopProduct> available = new ArrayList<>();

        for (ShopProduct product : values()) {
            // Se for BETA e o jogador já tem BETA ou superior, não mostrar
            if (product == BETA && account.hasRank(RankType.BETA)) {
                continue;
            }
            
            // Se for VIP e o jogador já tem VIP ou superior, não mostrar
            if (product == VIP && account.hasRank(RankType.VIP)) {
                continue;
            }

            // Se for SAVAGE e o jogador já tem SAVAGE ou superior, não mostrar
            if (product == SAVAGE && account.hasRank(RankType.SAVAGE)) {
                continue;
            }

            available.add(product);
        }

        return available;
    }

    public static List<ShopProduct> getRankProducts(Account account) {
        List<ShopProduct> available = new ArrayList<>();

        for (ShopProduct product : values()) {
            if (product.getCategory() != ShopCategory.RANKS) continue;

            // Se for BETA e o jogador já tem BETA ou superior, não mostrar
            if (product == BETA && account.hasRank(RankType.BETA)) {
                continue;
            }
            
            // Se for VIP e o jogador já tem VIP ou superior, não mostrar
            if (product == VIP && account.hasRank(RankType.VIP)) {
                continue;
            }

            // Se for SAVAGE e o jogador já tem SAVAGE ou superior, não mostrar
            if (product == SAVAGE && account.hasRank(RankType.SAVAGE)) {
                continue;
            }

            available.add(product);
        }

        return available;
    }
}
