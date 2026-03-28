package com.minecraft.savagemc.core.controller.list;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.collectible.Collectible;
import com.minecraft.savagemc.core.api.collectible.CollectibleCategory;
import com.minecraft.savagemc.core.api.collectible.nms.NMSContract;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.collectible.type.artifact.ArtifactCollectible;
import com.minecraft.savagemc.core.api.collectible.type.balloon.BalloonCollectible;
import com.minecraft.savagemc.core.api.collectible.type.banner.BannerCollectible;
import com.minecraft.savagemc.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.savagemc.core.api.collectible.type.companion.type.CompanionType;
import com.minecraft.savagemc.core.api.collectible.type.emotion.EmotionCollectible;
import com.minecraft.savagemc.core.api.collectible.type.hat.HatCollectible;
import com.minecraft.savagemc.core.controller.Controller;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.loader.ClassLoader;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class CollectibleController extends Controller<String, Collectible> {

    @Override
    public void save(Collectible collectible) {
        getCache().put(collectible.getIdentifier(), collectible);
    }

    public void handle(Object instance, String path) {
        Instant now = Instant.now();

        Savage.Savage.Logger().info("Registrando colecionáveis...");

        // Iniciando NMS
        NMSContract.handle();

        List<Class<? extends Collectible>> prohibitedClasses = Arrays.asList(
                Collectible.class, EmotionCollectible.class, BalloonCollectible.class, CompanionCollectible.class, HatCollectible.class, ArtifactCollectible.class,
                BannerCollectible.class);

        for (Class<?> cosmeticClass : ClassLoader.getClassesForPackage(instance, path)) {
            if (prohibitedClasses.contains(cosmeticClass)) continue;

            if (Collectible.class.isAssignableFrom(cosmeticClass)) {
                try {
                    Collectible collectible = (Collectible) cosmeticClass.newInstance();

                    save(collectible);
                } catch (Exception e) {
                    Savage.Savage.Logger().log(Level.SEVERE, "Não foi possível iniciar o cosmético " + cosmeticClass.getSimpleName(), e);
                }
            }
        }

        /* Caso a parte, cabeças, companheiros... */
        HatCollectible.handle();
        ArtifactCollectible.handle();
        CompanionType.handle();

        if (!isEmpty())
            Savage.Savage.Logger().info(size() + " colecionáveis foram carregados. (Tempo médio: " + Util.formatInstant(now) + ")");
    }

    public Collectible of(CollectibleCategory category, String name) {
        return list(category).stream().filter(collectible -> collectible != null && collectible.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Collectible> list(CollectibleCategory category) {
        return filter(collectible -> collectible.getCategory().equals(category));
    }

    public List<Collectible> list(CollectibleCategory category, CollectibleRarity rarity) {
        return filter(collectible -> collectible.getCategory().equals(category) && collectible.getRarity().equals(rarity));
    }
}
