package com.minecraft.core.api.collectible.type.title;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Agrupamento opcional dentro de {@link CollectibleTitleGroup#MUSICAS}
 * para o menu de seleção filtrar por artista/intérprete.
 */
@Getter
@AllArgsConstructor
public enum CollectibleMusicArtist {

    UNKNOWN("§8Sem artista"),
    BOLADIN211("§eBoladin 211"),
    CABELINHO("§6Cabelinho"),
    MC_GP("§cMC GP"),
    MC_LELE_JP("§dMC Lele JP"),
    MENO_K("§eMeno K"),
    /** Matuê — nome interno Java sem acento. */
    MATUE("§5Matuê"),
    OROCHI("§dOrochi"),
    VINICIN("§aVinicin"),
    /** Trap nacional — Dos Prédio / vida chique. */
    VEIGH("§bVeigh");

    private final String coloredLabel;
}
