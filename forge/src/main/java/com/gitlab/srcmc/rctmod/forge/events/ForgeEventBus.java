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
package com.gitlab.srcmc.rctmod.forge.events;

import java.io.InputStream;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.commands.PlayerCommands;
import com.gitlab.srcmc.rctmod.commands.TrainerCommands;
import com.gitlab.srcmc.rctmod.forge.world.VolatileTrainer;
import com.selfdot.cobblemontrainers.CobblemonTrainers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = ModCommon.MOD_ID, bus = Bus.FORGE)
public class ForgeEventBus {
    @SubscribeEvent
    static void onServerStarted(ServerStartedEvent event) {
        RCTMod.get().getServerDataManager().listTrainerTeams(ForgeEventBus::addTrainer);
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent event) {
        if(event.side.isServer()) {
            var interval = RCTMod.get().getServerConfig().spawnIntervalTicks();

            if(interval == 0 || interval > 0 && event.player.tickCount % interval == 0) {
                RCTMod.get().getTrainerSpawner().attemptSpawnFor(event.player);
            }
        }
    }

    @SubscribeEvent
    static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(RCTMod.get().getTrainerManager());
    }

	@SubscribeEvent
	static void onCommandRegistry(final RegisterCommandsEvent event) {
		PlayerCommands.register(event.getDispatcher());
        TrainerCommands.register(event.getDispatcher());
    }

    private static void addTrainer(ResourceLocation rl, IoSupplier<InputStream> io) {
        var trainerReg = CobblemonTrainers.INSTANCE.getTRAINER_REGISTRY();
        var trainer = new VolatileTrainer(rl, io);
        trainerReg.addOrUpdateTrainer(trainer);
    }
}
