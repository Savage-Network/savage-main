package com.minecraft.savagemc.core.api.collectible.operator;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.collectible.Collectible;
import com.minecraft.savagemc.core.api.collectible.CollectibleCategory;
import com.minecraft.savagemc.core.api.collectible.nms.module.companion.CompanionEntities;
import com.minecraft.savagemc.core.api.collectible.operator.list.*;
import com.minecraft.savagemc.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.savagemc.core.api.collectible.type.balloon.BalloonCollectible;
import com.minecraft.savagemc.core.api.collectible.type.banner.BannerCollectible;
import com.minecraft.savagemc.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.savagemc.core.api.collectible.type.emotion.EmotionCollectible;
import com.minecraft.savagemc.core.api.collectible.type.hat.HatCollectible;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class CollectibleOperator {

    @Getter
    private static final Set<CollectibleOperator> list = new CopyOnWriteArraySet<>();

    private final Player host;
    private final CollectibleCategory category;

    private Collectible collectible;
    private List<CollectibleCategory> categoriesWithConflict;

    public CollectibleOperator(Player host, CollectibleCategory category) {
        this.host = host;
        this.category = category;

        list.add(this);
    }

    public abstract void handle(Collectible collectible);

    public abstract void cancel();

    public void setCollectible(Collectible collectible) {
        this.collectible = collectible;

        Account account = Savage.getAccountController().of(host.getUniqueId());

        if (account != null)
            account.activateCollectible(collectible);
    }

    public static void load(Player host) {
        Account account = Savage.getAccountController().of(host.getUniqueId());

        if (account != null) {
            List<String> list = account.getActiveCollectibles();
            
            if (list.isEmpty()) {
                Savage.getLogger().info("[Collectible] Nenhum cosmético ativo para " + host.getName());
                return;
            }
            
            Savage.getLogger().info("[Collectible] Tentando carregar " + list.size() + " cosmético(s) para " + host.getName());
            
            int loaded = 0;
            int notFound = 0;

            for (String id : list) {
                Collectible collectible = Savage.getCollectibleController().of(id);

                if (collectible != null) {
                    Savage.getLogger().info("[Collectible] ✓ Carregado: " + collectible.getName() + " (" + collectible.getCategory() + ")");
                    start(host, collectible);
                    loaded++;
                } else {
                    Savage.getLogger().info("[Collectible] ✗ Não implementado: " + id);
                    notFound++;
                }
            }
            
            Savage.getLogger().info("[Collectible] Resumo para " + host.getName() + ": " + loaded + " carregados, " + notFound + " não implementados");
        } else {
            Savage.getLogger().warning("[Collectible] Conta não encontrada para " + host.getName());
        }
    }

    public static void start(Player host, Collectible collectible) {
        switch (collectible.getCategory()) {
            case HAT: {
                new HatOperator(host, (HatCollectible) collectible);
                break;
            }
            case EMOTION: {
                new EmotionOperator(host, (EmotionCollectible) collectible);
                break;
            }
            case BALLOON: {
                new BalloonOperator(host, (BalloonCollectible) collectible);
                break;
            }
            case COMPANION: {
                new CompanionOperator(host, (CompanionCollectible) collectible);
                break;
            }
            case ARTIFACT: {
                new ArtifactOperator(host, (ArtifactCollectible) collectible);
                break;
            }
            case BANNER: {
                new BannerOperator(host, (BannerCollectible) collectible);
                break;
            }
        }
    }

    public static List<CollectibleOperator> list(Player host) {
        return list.stream()
                .filter(operator -> operator.getHost().equals(host))
                .collect(Collectors.toList());
    }

    public static CollectibleOperator of(Player host, CollectibleCategory category) {
        return list(host).stream()
                .filter(operator -> operator.getCategory().equals(category))
                .findFirst()
                .orElse(null);
    }

    public static boolean has(Player host, CollectibleCategory category) {
        return of(host, category) != null;
    }

    public static boolean has(Player host, Collectible collectible) {
        return list(host).stream().anyMatch(operator -> operator.getCategory().equals(collectible.getCategory())
                && operator.getCollectible() != null && operator.getCollectible().equals(collectible));
    }

    public static void disable(Player host) {
        Account account = Savage.getAccountController().of(host.getUniqueId());

        list(host).forEach(operator -> {
            operator.cancel();

            account.removeActiveCollectible(operator.getCollectible());

            list.remove(operator);
        });

        CompanionEntities.removeByOwner(host);
    }

    public static void unload(Player host) {
        list(host).forEach(operator -> {
            operator.cancel();

            list.remove(operator);
        });

        CompanionEntities.removeByOwner(host);
    }

    public static void unload(Player host, CollectibleCategory category) {
        CollectibleOperator operator = of(host, category);

        if (operator != null) {
            operator.cancel();

            Account account = Savage.getAccountController().of(host.getUniqueId());

            if (account != null)
                account.removeActiveCollectible(operator.getCollectible());

            list.remove(operator);
        }
    }

    public static void remove(Player host, CollectibleCategory category) {
        list.removeIf(operator -> operator.getHost().equals(host) && operator.getCategory().equals(category));
    }

    public static void unloadConflicts(Player host, CollectibleCategory category) {
        list(host).stream().filter(operator -> operator.hasConflict(category)).forEach(operator -> unload(host, operator.getCategory()));
    }

    public boolean hasConflict(CollectibleCategory category) {
        return categoriesWithConflict != null && categoriesWithConflict.contains(category);
    }
}
