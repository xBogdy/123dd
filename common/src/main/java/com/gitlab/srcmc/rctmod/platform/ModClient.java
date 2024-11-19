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

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.client.TrainerRenderer;
import com.gitlab.srcmc.rctmod.client.renderer.TargetArrowRenderer;
import com.gitlab.srcmc.rctmod.client.screens.ScreenManager;
import com.gitlab.srcmc.rctmod.platform.network.PlayerStatePayload;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ModClient extends com.gitlab.srcmc.rctmod.client.ModClient {
    private static Queue<byte[]> playerStateUpdates = new ConcurrentLinkedDeque<>();

    public static void init() {
        ModCommon.LOG.info("INITIALIZING CLIENT");
        EntityRendererRegistry.register(ModRegistries.Entities.TRAINER, TrainerRenderer::new);
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, RCTMod.get().getClientDataManager());
        // ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, RCTMod.get().getTrainerManager());
        ClientTickEvent.CLIENT_LEVEL_PRE.register(ModClient::onClientWorldTick);
        NetworkManager.registerReceiver(Side.S2C, PlayerStatePayload.TYPE, PlayerStatePayload.CODEC, ModClient::handleReceivedPlayerState);
        TargetArrowRenderer.init(ModRegistries.Items.TRAINER_CARD);
        ModCommon.SCREENS = new ScreenManager();
        // ForgeConfigRegistry.INSTANCE.register(ModCommon.MOD_ID, ModConfig.Type.CLIENT, RCTMod.get().getClientConfig().getSpec());
        com.gitlab.srcmc.rctmod.client.ModClient.init(new ModClient()); // TODO: temp solution
    }

    @Override
    public Optional<Player> getLocalPlayer() {
        var mc = Minecraft.getInstance();
        return Optional.of(mc.player);
    }

    static void onClientWorldTick(Level level) {
        var mc = Minecraft.getInstance();
        var psu = playerStateUpdates.poll();

        if(psu != null) {
            PlayerState.get(mc.player).deserializeUpdate(psu);
        }
        
        TargetArrowRenderer.getInstance().tick();
    }

    static void handleReceivedPlayerState(PlayerStatePayload pl, PacketContext context) {
        if(!playerStateUpdates.offer(pl.bytes())) {
            throw new IllegalStateException("Failed to store player state updates");
        }
    }
}
