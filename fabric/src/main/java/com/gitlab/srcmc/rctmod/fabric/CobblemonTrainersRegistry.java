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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.fabric.world.trainer.VolatileTrainer;
import com.selfdot.cobblemontrainers.CobblemonTrainers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;

public class CobblemonTrainersRegistry {
    private static List<ResourceLocation> registered = new ArrayList<>();

    public static void registerTrainers() {
        registered.clear();
        RCTMod.get().getServerDataManager().listTrainerTeams(CobblemonTrainersRegistry::registerTrainer);
        ModCommon.LOG.info(String.format("Registered %d trainers", registered.size()));
    }

    private static void registerTrainer(ResourceLocation rl, IoSupplier<InputStream> io) {
        var trainerReg = CobblemonTrainers.INSTANCE.getTrainerRegistry();

        try {
            var trainer = new VolatileTrainer(rl, io);
            trainerReg.addOrUpdateTrainer(trainer);
            registered.add(rl);
        } catch(Exception e) {
            ModCommon.LOG.error("Failed to register trainer: " + rl.getPath(), e);
        }
    }
}
