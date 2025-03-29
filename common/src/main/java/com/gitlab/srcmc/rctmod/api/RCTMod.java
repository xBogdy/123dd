/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2025, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctmod.api;

import java.util.List;
import java.util.function.Supplier;

import com.cobblemon.mod.common.Cobblemon;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerPlayer;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.config.IClientConfig;
import com.gitlab.srcmc.rctmod.api.config.IServerConfig;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager;
import com.gitlab.srcmc.rctmod.api.service.TrainerManager;
import com.gitlab.srcmc.rctmod.api.service.TrainerSpawner;
import com.gitlab.srcmc.rctmod.config.ClientConfig;
import com.gitlab.srcmc.rctmod.config.ServerConfig;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class RCTMod {
    private static Supplier<RCTMod> instanceSupplier = () -> {
        var defaultInstance = new RCTMod(
            new TrainerManager(), new TrainerSpawner(),
            new ClientConfig(), new ServerConfig());

        RCTMod.instanceSupplier = () -> defaultInstance;
        return defaultInstance;
    };

    private TrainerManager trainerManager;
    private TrainerSpawner trainerSpawner;
    private IClientConfig clientConfig;
    private IServerConfig serverConfig;

    private RCTMod(
        TrainerManager trainerManager,
        TrainerSpawner trainerSpawner,
        IClientConfig clientConfig,
        IServerConfig serverConfig)
    {
        this.trainerManager = trainerManager;
        this.trainerSpawner = trainerSpawner;
        this.clientConfig = clientConfig;
        this.serverConfig = serverConfig;
    }

    public TrainerManager getTrainerManager() {
        return this.trainerManager;
    }

    public SeriesManager getSeriesManager() {
        return this.trainerManager.getSeriesManager();
    }

    public TrainerSpawner getTrainerSpawner() {
        return this.trainerSpawner;
    }

    public IClientConfig getClientConfig() {
        return this.clientConfig;
    }

    public IServerConfig getServerConfig() {
        return this.serverConfig;
    }

    public boolean makeBattle(TrainerMob mob, Player player) {
        var reg = ModCommon.RCT.getTrainerRegistry();

        try {
            var trPlayer = reg.getById(RCTMod.getInstance().getTrainerManager().getTrainerId(player), TrainerPlayer.class);
            var trNPC = reg.getById(mob.getTrainerId(), TrainerNPC.class);

            if(trPlayer == null) {
                ModCommon.LOG.error("Failed to start battle: No trainer registered for player '" + player.getDisplayName().getString() + "'");
            } else if(trNPC == null) {
                ModCommon.LOG.error("Failed to start battle: No trainer registered for mob '" + mob.getDisplayName().getString() + "'");
            } else {
                var team = RCTMod.getInstance().getTrainerManager().getData(mob).getTrainerTeam();
                return ModCommon.RCT.getBattleManager().start(List.of(trPlayer), List.of(trNPC), team.getBattleFormat(), team.getBattleRules());
            }
        } catch(IllegalArgumentException e) {
            ModCommon.LOG.error("Failed to start battle", e);
        }

        return false;
    }

    public void stopBattle(TrainerMob mob) {
        var opp = (ServerPlayer)mob.getOpponent();

        if(opp != null) {
            var battle = Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(opp);

            if(battle == null) {
                ModCommon.LOG.error(String.format("Player '%s' is not in a battle", opp.getDisplayName().getString()));
            } else {
                battle.stop();
            }
        }
    }

    public boolean isInBattle(Player player) {
        return Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayerId(player.getUUID()) != null;
    }

    public static RCTMod getInstance() {
        return RCTMod.instanceSupplier.get();
    }
}
