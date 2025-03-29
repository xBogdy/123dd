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
package com.gitlab.srcmc.rctmod.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.DataPackManager;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerType;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.utils.ArrUtils;
import com.gitlab.srcmc.rctmod.client.renderer.TargetArrowRenderer;
import com.gitlab.srcmc.rctmod.client.renderer.TrainerAssociationRenderer;
import com.gitlab.srcmc.rctmod.client.renderer.TrainerRenderer;
import com.gitlab.srcmc.rctmod.client.screens.IScreenManager;
import com.gitlab.srcmc.rctmod.client.screens.ScreenManager;
import com.gitlab.srcmc.rctmod.network.BatchedPayload;
import com.gitlab.srcmc.rctmod.network.BatchedPayloads;
import com.gitlab.srcmc.rctmod.network.TrainerTargetPayload;
import com.gitlab.srcmc.rctmod.network.BatchedPayload.Payload;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ModClient {
    public static final DataPackManager RESOURCE_MANAGER = new DataPackManager(PackType.CLIENT_RESOURCES);
    public static final IScreenManager SCREENS = new ScreenManager();

    public static void init() {
        var mc = Minecraft.getInstance();
        ModCommon.initPlayer(() -> mc.player);
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, RESOURCE_MANAGER);
        EntityRendererRegistry.register(ModRegistries.Entities.TRAINER, TrainerRenderer::new);
        EntityRendererRegistry.register(ModRegistries.Entities.TRAINER_ASSOCIATION, TrainerAssociationRenderer::new);
        TargetArrowRenderer.init();
    }

    public static void setup() {
        NetworkManager.registerReceiver(Side.S2C, BatchedPayloads.PLAYER_STATE.TYPE, BatchedPayloads.PLAYER_STATE.CODEC, ModClient::receivePlayerState);
        NetworkManager.registerReceiver(Side.S2C, BatchedPayloads.TRAINER_MANAGER.TYPE, BatchedPayloads.TRAINER_MANAGER.CODEC, ModClient::receiveTrainerManager);
        NetworkManager.registerReceiver(Side.S2C, TrainerTargetPayload.TYPE, TrainerTargetPayload.CODEC, ModClient::receiveTrainerTarget);
        ClientTickEvent.CLIENT_LEVEL_POST.register(ModClient::onClientLevelTick);
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(ModClient::onClientPlayerJoin);
    }

    // ClientTickEvent

    static void onClientLevelTick(Level level) {
        var psu = ModClient.playerStateUpdates.poll();

        if(psu != null) {
            PlayerState.get(ModCommon.localPlayer()).deserializeUpdate(psu);
        }

        var tmu = ModClient.trainerManagerUpdates.poll();

        if(tmu != null) {
            TrainerType.clear();
            RCTMod.getInstance().getTrainerManager().fromPayloads(tmu);
        }

        TargetArrowRenderer.getInstance().tick();
    }

    // ClientPlayerEvent

    static void onClientPlayerJoin(LocalPlayer player) {
        PLAYER_STATE_PAYLOADS.clear();
        TRAINER_MANAGER_PAYLOADS.clear();
        RCTMod.getInstance().getTrainerManager().setLoading(true);
        PlayerState.initFor(player);
    }

    // NetworkManager

    private static final Queue<byte[]> playerStateUpdates = new ConcurrentLinkedDeque<>();
    private static final Queue<Payload[]> trainerManagerUpdates = new ConcurrentLinkedDeque<>();

    static List<byte[]> PLAYER_STATE_PAYLOADS = new ArrayList<>();
    static List<Payload> TRAINER_MANAGER_PAYLOADS = new ArrayList<>();
    
    static void receivePlayerState(BatchedPayload.Payload pl, PacketContext context) {
        PLAYER_STATE_PAYLOADS.add(pl.bytes());

        if(pl.remainingBatches() == 0) {
            if(!ModClient.playerStateUpdates.offer(ArrUtils.combine(PLAYER_STATE_PAYLOADS))) {
                ModCommon.LOG.error("Failed to store player state updates");
            }

            PLAYER_STATE_PAYLOADS.clear();
        }
    }

    static void receiveTrainerManager(BatchedPayload.Payload pl, PacketContext context) {
        RCTMod.getInstance().getTrainerManager().setLoading(true);
        TRAINER_MANAGER_PAYLOADS.add(pl);

        if(pl.remainingBatches() == 0) {
            if(!ModClient.trainerManagerUpdates.offer(TRAINER_MANAGER_PAYLOADS.toArray(new Payload[TRAINER_MANAGER_PAYLOADS.size()]))) {
                ModCommon.LOG.error("Failed to store trainer manager updates");
            }

            TRAINER_MANAGER_PAYLOADS.clear();
        }
    }

    static void receiveTrainerTarget(TrainerTargetPayload tt, PacketContext context) {
        TargetArrowRenderer.getInstance().setTarget(context.getPlayer(), new Vec3(tt.targetX(), tt.targetY(), tt.targetZ()), tt.otherDim());
    }
}
