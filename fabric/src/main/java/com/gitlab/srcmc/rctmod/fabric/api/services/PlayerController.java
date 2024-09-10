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
package com.gitlab.srcmc.rctmod.fabric.api.services;

import com.cobblemon.mod.common.Cobblemon;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.service.IPlayerController;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.selfdot.cobblemontrainers.CobblemonTrainers;
import com.selfdot.cobblemontrainers.util.PokemonUtility;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerController implements IPlayerController {
    @Override
    public int getPlayerLevel(Player player) {
        int maxLevel = 0;

        for(var pk : Cobblemon.INSTANCE.getStorage().getParty((ServerPlayer)player)) {
            maxLevel = Math.max(maxLevel, pk.getLevel());
        }

        return maxLevel;
    }

    @Override
    public int getActivePokemonCount(Player player) {
        int count = 0;

        for(var pk : Cobblemon.INSTANCE.getStorage().getParty((ServerPlayer)player)) {
            if(!pk.isFainted()) {
                count++;
            }
        }

        return count;
    }

    @Override
    public boolean isInBattle(Player player) {
        return Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayerId(player.getUUID()) != null;
    }

    @Override
    public void startBattle(TrainerMob trainer, Player player) {
        PokemonUtility.startTrainerBattle((ServerPlayer)player, CobblemonTrainers.INSTANCE.getTrainerRegistry().getTrainer(trainer.getTrainerId()), trainer);
    }

    @Override
    public void stopBattle(TrainerMob trainer) {
        var opp = (ServerPlayer)trainer.getOpponent();

        if(opp != null) {
            var battle = Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(opp);

            if(battle == null) {
                ModCommon.LOG.error(String.format("Player '%s' is not in a battle", opp.getDisplayName().getString()));
            } else {
                battle.stop();
            }
        }
    }
}
