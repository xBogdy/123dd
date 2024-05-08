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
package com.gitlab.srcmc.rctmod.fabric.server;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.fabric.CobblemonHandler;
import com.gitlab.srcmc.rctmod.fabric.network.Packets;
import dev.architectury.registry.ReloadListenerRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.fml.config.ModConfig;

public class ModServer {
    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(ModServer::onServerStarted);
        ServerTickEvents.START_WORLD_TICK.register(ModServer::onServerWorldTick);
        ForgeConfigRegistry.INSTANCE.register(ModCommon.MOD_ID, ModConfig.Type.SERVER, RCTMod.get().getServerConfig().getSpec());
        ReloadListenerRegistry.register(PackType.SERVER_DATA, RCTMod.get().getTrainerManager());
        ServerPlayNetworking.registerGlobalReceiver(Packets.PLAYER_PING, ModServer::handleReceivedPlayerPing);
    }

    private static void onServerStarted(MinecraftServer server) {
        RCTMod.get().getServerDataManager().listTrainerTeams(CobblemonHandler::registerTrainer);
    }

    private static void onServerWorldTick(ServerLevel level) {
        level.players().forEach(player -> {
            if(player.tickCount % 200 == 0) {
                var bytes = PlayerState.get(player).serialize();

                if(bytes.length > 0) {
                    var ps = PlayerState.get(player);
                    ModCommon.LOG.info("SENDING PS: " + bytes.length + ", " + ps.getLevelCap() + ", " + ps.getTrainerDefeatCounts().size() + ", " + ps.getTypeDefeatCounts().size());

                    var buf = PacketByteBufs.create();
                    buf.writeBytes(bytes);
                    ServerPlayNetworking.send(player, Packets.PLAYER_STATE, buf);
                }
            }
        });
    }

    private static void handleReceivedPlayerPing(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        RCTMod.get().getTrainerSpawner().attemptSpawnFor(player);
    }
}
