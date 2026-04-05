/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.event.game;

import com.minecraft.core.bukkit.event.handler.ServerEvent;
import com.minecraft.savagemc.hungergames.game.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GameRecoveryModeToggleEvent extends ServerEvent {

    private Game game;

}
