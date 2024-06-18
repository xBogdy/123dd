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
package com.gitlab.srcmc.rctmod.api;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.config.IClientConfig;
import com.gitlab.srcmc.rctmod.api.config.ICommonConfig;
import com.gitlab.srcmc.rctmod.api.config.IServerConfig;
import com.gitlab.srcmc.rctmod.api.data.pack.DataPackManager;
import com.gitlab.srcmc.rctmod.api.service.TrainerManager;
import com.gitlab.srcmc.rctmod.api.service.TrainerSpawner;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.gitlab.srcmc.rctmod.world.loot.conditions.DefeatCountCondition;
import com.gitlab.srcmc.rctmod.world.loot.conditions.LevelRangeCondition;

import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class RCTMod {
    private TrainerManager trainerManager;
    private DataPackManager clientDataManager;
    private DataPackManager serverDataManager;
    private TrainerSpawner trainerSpawner;

    private ICommonConfig commonConfig;
    private IClientConfig clientConfig;
    private IServerConfig serverConfig;

    private BiConsumer<TrainerMob, Player> battleArgsConsumer = (m, p) -> {};

    private static Supplier<RCTMod> instance = () -> {
        throw new RuntimeException(RCTMod.class.getName() + " not initialized");
    };

    public static RCTMod get() {
        return instance.get();
    }

    public static void init(
        Supplier<LootItemConditionType> levelRangeConditon,
        Supplier<LootItemConditionType> defeatCountConditon,
        Function<Player, Integer> playerLevelSupplier,
        Function<Player, Integer> avtivePokemonSupplier,
        BiConsumer<TrainerMob, Player> battleArgsConsumer,
        IClientConfig clientConfig, ICommonConfig commonConfig, IServerConfig serverConfig)
    {
        var local = new RCTMod(playerLevelSupplier, avtivePokemonSupplier, battleArgsConsumer, clientConfig, commonConfig, serverConfig);
        instance = () -> local;
        LevelRangeCondition.init(levelRangeConditon);
        DefeatCountCondition.init(defeatCountConditon);
    }

    private RCTMod(
        Function<Player, Integer> playerLevelSupplier,
        Function<Player, Integer> avtivePokemonSupplier,
        BiConsumer<TrainerMob, Player> battleArgsConsumer,
        IClientConfig clientConfig, ICommonConfig commonConfig, IServerConfig serverConfig)
    {
        this.trainerManager = new TrainerManager(playerLevelSupplier, avtivePokemonSupplier);
        this.clientDataManager = new DataPackManager(PackType.CLIENT_RESOURCES);
        this.serverDataManager = new DataPackManager(PackType.SERVER_DATA);
        this.trainerSpawner = new TrainerSpawner();
        this.battleArgsConsumer = battleArgsConsumer;
        this.clientConfig = clientConfig;
        this.commonConfig = commonConfig;
        this.serverConfig = serverConfig;
    }

    public TrainerManager getTrainerManager() {
        return this.trainerManager;
    }

    public DataPackManager getClientDataManager() {
        return this.clientDataManager;
    }

    public DataPackManager getServerDataManager() {
        return this.serverDataManager;
    }

    public TrainerSpawner getTrainerSpawner() {
        return this.trainerSpawner;
    }

    public IClientConfig getClientConfig() {
        return this.clientConfig;
    }

    public ICommonConfig getCommonConfig() {
        return this.commonConfig;
    }

    public IServerConfig getServerConfig() {
        return this.serverConfig;
    }

    public boolean makeBattle(TrainerMob source, Player target) {
        try {
            this.battleArgsConsumer.accept(source, target);
        } catch(Exception e) {
            ModCommon.LOG.error(e.getMessage(), e);
            return false;
        }

        return true;
    }
}
