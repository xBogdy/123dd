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
package com.gitlab.srcmc.rctmod.fabric.client;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.client.TrainerRenderer;
import com.gitlab.srcmc.rctmod.fabric.ModFabric;
import com.gitlab.srcmc.rctmod.fabric.network.Packets;
import dev.architectury.registry.ReloadListenerRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class ModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModFabric.TRAINER, (context) -> {
            return new TrainerRenderer(context);
        });

        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, RCTMod.get().getClientDataManager());
        ClientTickEvents.START_WORLD_TICK.register(ModClient::handleClientWorldTick);
    }

    static void handleClientWorldTick(Level level) {
        var mc = Minecraft.getInstance();

        if(mc.player.tickCount % RCTMod.get().getServerConfig().spawnIntervalTicks() == 0) {
            ClientPlayNetworking.send(Packets.PLAYER_PING, PacketByteBufs.empty());
        }
    }
}
