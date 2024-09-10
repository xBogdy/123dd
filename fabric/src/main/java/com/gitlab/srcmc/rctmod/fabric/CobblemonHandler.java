/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2024, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctmod.fabric;

import java.util.List;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedPreEvent;
import com.gitlab.srcmc.rctmod.api.RCTMod;

import kotlin.Unit;

public class CobblemonHandler {
    public static Unit handleBattleVictory(BattleVictoryEvent event) {
        if(!checkForTrainerBattle(event.getWinners(), true)) {
            checkForTrainerBattle(event.getLosers(), false);
        }

        return Unit.INSTANCE;
    }

    public static Unit handleExperienceGained(ExperienceGainedPreEvent event) {
        var owner = event.getPokemon().getOwnerPlayer();

        if(owner != null) {
            var playerTr = RCTMod.get().getTrainerManager().getData(owner);
            var maxExp = event.getPokemon().getExperienceToLevel(playerTr.getLevelCap());

            if(maxExp < event.getExperience()) {
                owner.server.getCommands().performPrefixedCommand(
                    owner.server.createCommandSourceStack().withSuppressedOutput(),
                    String.format("title %s actionbar \"%s is %s the level cap (%d)\"",
                        owner.getName().getString(),
                        event.getPokemon().getDisplayName().getString(),
                        event.getPokemon().getLevel() == playerTr.getLevelCap() ? "at" : "over",
                        playerTr.getLevelCap()));
            }

            event.setExperience(Math.min(event.getExperience(), maxExp));
        }

        return Unit.INSTANCE;
    }

    private static boolean checkForTrainerBattle(List<BattleActor> actors, boolean winners) {
        for(var actor : actors) {
            var trainerBattle = RCTMod.get().getTrainerManager().getBattle(actor.getUuid());

            if(trainerBattle.isPresent()) {
                RCTMod.get().getTrainerManager().removeBattle(actor.getUuid());
                trainerBattle.get().distributeRewards(winners);
                return true;
            }
        }

        return false;
    }
}
