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
package com.gitlab.srcmc.rctmod.client;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.client.renderer.TargetArrowRenderer;
import com.gitlab.srcmc.rctmod.client.renderer.TrainerRenderer;
import com.gitlab.srcmc.rctmod.client.renderer.TrainerSpawnerBlockEntityRenderer;
import com.gitlab.srcmc.rctmod.client.screens.IScreenManager;
import com.gitlab.srcmc.rctmod.client.screens.ScreenManager;
import com.gitlab.srcmc.rctmod.network.PlayerStatePayload;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.Level;

public class ModClient {
    public static final IScreenManager SCREENS = new ScreenManager();
    private static Queue<byte[]> playerStateUpdates = new ConcurrentLinkedDeque<>();

    public static void init() {
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, RCTMod.getInstance().getClientDataManager());
        NetworkManager.registerReceiver(Side.S2C, PlayerStatePayload.TYPE, PlayerStatePayload.CODEC, ModClient::receivePlayerState);
        ClientTickEvent.CLIENT_LEVEL_PRE.register(ModClient::onClientWorldTick);
        EntityRendererRegistry.register(ModRegistries.Entities.TRAINER, TrainerRenderer::new);
        LifecycleEvent.SETUP.register(ModClient::onSetup);
        TargetArrowRenderer.init();
    }

    // Setup

    public static void onSetup() {
        BlockEntityRendererRegistry.register(ModRegistries.BlockEntityTypes.TRAINER_SPAWNER.get(), TrainerSpawnerBlockEntityRenderer::new);
    }

    // ClientTickEvent

    static void onClientWorldTick(Level level) {
        var mc = Minecraft.getInstance();
        var psu = ModClient.playerStateUpdates.poll();

        if(psu != null) {
            PlayerState.get(mc.player).deserializeUpdate(psu);
        }
        
        TargetArrowRenderer.getInstance().tick();
    }

    // NetworkManager

    static void receivePlayerState(PlayerStatePayload pl, PacketContext context) {
        if(!ModClient.playerStateUpdates.offer(pl.bytes())) {
            // TODO: log error instead of exception
            throw new IllegalStateException("Failed to store player state updates");
        }
    }
}
