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
package com.gitlab.srcmc.rctmod.forge;

import java.io.InputStream;
import java.util.List;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedPreEvent;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.forge.world.trainer.VolatileTrainer;
import com.selfdot.cobblemontrainers.CobblemonTrainers;

import kotlin.Unit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.entity.player.Player;

public class CobblemonHandler {
    public static void registerTrainer(ResourceLocation rl, IoSupplier<InputStream> io) {
        var trainerReg = CobblemonTrainers.INSTANCE.getTrainerRegistry();
        var trainer = new VolatileTrainer(rl, io);
        trainerReg.addOrUpdateTrainer(trainer);
    }

    public static int getPlayerLevel(Player player) {
        int maxLevel = 0;

        for(var pk : Cobblemon.INSTANCE.getStorage().getParty((ServerPlayer)player)) {
            maxLevel = Math.max(maxLevel, pk.getLevel());
        }

        return maxLevel;
    }

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
