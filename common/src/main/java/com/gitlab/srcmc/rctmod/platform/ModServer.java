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
package com.gitlab.srcmc.rctmod.platform;

import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.platform.network.PlayerStatePayload;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent.LevelTick;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;

public class ModServer {
    public static void init() {
        ModCommon.LOG.info("INITIALIZING SERVER");
        LifecycleEvent.SERVER_STARTING.register(ModServer::onServerStarting);
        LevelTick.SERVER_LEVEL_PRE.register(ModServer::onServerWorldTick);
        LevelTick.SERVER_PRE.register(ModServer::onServerTick);
        PlayerEvent.PLAYER_JOIN.register(ModServer::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(ModServer::onPlayerQuit);
    }

    static void onPlayerJoin(ServerPlayer player) {
        var trainerId = RCTMod.get().getTrainerManager().registerPlayer(player);
        RCTApi.getInstance().getTrainerRegistry().registerPlayer(trainerId, player);
        ModCommon.LOG.info(String.format("Registered trainer player: %s", trainerId));
    }

    static void onPlayerQuit(ServerPlayer player) {
        var trainerId = RCTMod.get().getTrainerManager().unregisterPlayer(player);

        if(RCTApi.getInstance().getTrainerRegistry().unregisterById(trainerId) != null) {
            ModCommon.LOG.info(String.format("Unregistered trainer player: %s", trainerId));
        }
    }

    static void onServerStarting(MinecraftServer server) {
        RCTApi.getInstance().getTrainerRegistry().init(server);
        RCTMod.get().getTrainerSpawner().init(server.overworld());
        RCTMod.get().getTrainerManager().forceReload(server.getResourceManager());
        ReloadListenerRegistry.register(PackType.SERVER_DATA, RCTMod.get().getTrainerManager());
    }

    static void onServerTick(MinecraftServer server) {
        RCTMod.get().getTrainerSpawner().checkDespawns();
    }

    static void onServerWorldTick(ServerLevel level) {
        level.players().forEach(player -> {
            if(player.tickCount % RCTMod.get().getServerConfig().spawnIntervalTicks() == 0) {
                RCTMod.get().getTrainerSpawner().attemptSpawnFor(player);
            }

            if(player.tickCount % PlayerState.SYNC_INTERVAL_TICKS == 0) {
                var bytes = PlayerState.get(player).serializeUpdate();

                if(bytes.length > 0) {
                    NetworkManager.sendToPlayer(player, PlayerStatePayload.of(bytes));
                }
            }
        });
    }
}
