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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.client.renderer.TargetArrowRenderer;
import com.gitlab.srcmc.rctmod.client.renderer.TrainerRenderer;
import com.gitlab.srcmc.rctmod.client.screens.IScreenManager;
import com.gitlab.srcmc.rctmod.client.screens.ScreenManager;
import com.gitlab.srcmc.rctmod.network.PlayerStatePayload;
import com.gitlab.srcmc.rctmod.network.TrainerTargetPayload;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ModClient {
    public static final IScreenManager SCREENS = new ScreenManager();
    private static Queue<byte[]> playerStateUpdates = new ConcurrentLinkedDeque<>();

    public static void init() {
        var mc = Minecraft.getInstance();
        ModCommon.initPlayer(() -> mc.player);
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, RCTMod.getInstance().getClientDataManager());
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, RCTMod.getInstance().getTrainerManager());
        EntityRendererRegistry.register(ModRegistries.Entities.TRAINER, TrainerRenderer::new);
        EntityRendererRegistry.register(ModRegistries.Entities.TRAINER_ASSOCIATION, WanderingTraderRenderer::new);
        TargetArrowRenderer.init();
    }

    public static void setup() {
        NetworkManager.registerReceiver(Side.S2C, PlayerStatePayload.TYPE, PlayerStatePayload.CODEC, ModClient::receivePlayerState);
        NetworkManager.registerReceiver(Side.S2C, TrainerTargetPayload.TYPE, TrainerTargetPayload.CODEC, ModClient::receiveTrainerTarget);
        ClientTickEvent.CLIENT_LEVEL_POST.register(ModClient::onClientLevelTick);
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(ModClient::onClientPlayerJoin);
    }

    // ClientTickEvent

    static void onClientLevelTick(Level level) {
        var tm = RCTMod.getInstance().getTrainerManager();

        if(tm.isReloadRequired()) {
            tm.loadTrainers();
        }

        var psu = ModClient.playerStateUpdates.poll();

        if(psu != null) {
            PlayerState.get(ModCommon.localPlayer()).deserializeUpdate(psu);
        }
        
        TargetArrowRenderer.getInstance().tick();
    }

    // ClientPlayerEvent

    static void onClientPlayerJoin(LocalPlayer player) {
        PlayerState.get(player, true);
    }

    // NetworkManager

    static void receivePlayerState(PlayerStatePayload pl, PacketContext context) {
        if(!ModClient.playerStateUpdates.offer(pl.bytes())) {
            // TODO: log error instead of exception
            throw new IllegalStateException("Failed to store player state updates");
        }
    }

    static void receiveTrainerTarget(TrainerTargetPayload tt, PacketContext context) {
        TargetArrowRenderer.getInstance().setTarget(context.getPlayer(), new Vec3(tt.targetX(), tt.targetY(), tt.targetZ()), tt.otherDim());
    }
}
