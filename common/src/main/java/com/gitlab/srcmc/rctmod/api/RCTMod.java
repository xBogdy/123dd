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

import java.util.function.Supplier;

import com.gitlab.srcmc.rctmod.api.data.pack.DataPackManager;
import com.gitlab.srcmc.rctmod.api.service.TrainerManager;
import com.gitlab.srcmc.rctmod.api.service.TrainerSpawner;
import com.gitlab.srcmc.rctmod.world.loot.conditions.LevelRangeCondition;

import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class RCTMod {
    private TrainerManager trainerManager;
    private DataPackManager dataPackManager;
    private TrainerSpawner trainerSpawner;

    private static Supplier<RCTMod> instance = () -> {
        throw new RuntimeException(RCTMod.class.getName() + " not initialized");
    };

    public static RCTMod get() {
        return instance.get();
    }

    public static void init(Supplier<LootItemConditionType> levelRangeConditon) {
        var local = new RCTMod();
        instance = () -> local;
        LevelRangeCondition.init(levelRangeConditon);
    }

    private RCTMod() {
        this.trainerManager = new TrainerManager();
        this.dataPackManager = new DataPackManager();
        this.trainerSpawner = new TrainerSpawner();
    }

    public TrainerManager getTrainerManager() {
        return this.trainerManager;
    }

    public DataPackManager getDataPackManager() {
        return this.dataPackManager;
    }

    public TrainerSpawner getTrainerSpawner() {
        return this.trainerSpawner;
    }
}
