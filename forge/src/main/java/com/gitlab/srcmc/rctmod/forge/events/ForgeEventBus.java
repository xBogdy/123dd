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

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.client.renderer.TargetArrowRenderer;
import com.gitlab.srcmc.rctmod.commands.PlayerCommands;
import com.gitlab.srcmc.rctmod.commands.TrainerCommands;
import com.gitlab.srcmc.rctmod.forge.CobblemonTrainersRegistry;
import com.gitlab.srcmc.rctmod.forge.ModRegistries.Items;
import com.gitlab.srcmc.rctmod.forge.network.NetworkManager;
import com.gitlab.srcmc.rctmod.forge.network.packets.S2CPlayerState;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = ModCommon.MOD_ID, bus = Bus.FORGE)
public class ForgeEventBus {
    @SubscribeEvent
    static void onServerStarted(ServerStartedEvent event) {
        CobblemonTrainersRegistry.registerTrainers();
        RCTMod.get().getTrainerSpawner().init(event.getServer().overworld());
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent event) {
        RCTMod.get().getTrainerSpawner().checkDespawns();
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent event) {
        if(event.phase == Phase.START) {
            if(event.side.isServer()) {
                var interval = RCTMod.get().getServerConfig().spawnIntervalTicks();

                if(interval == 0 || interval > 0 && event.player.tickCount % interval == 0) {
                    RCTMod.get().getTrainerSpawner().attemptSpawnFor(event.player);
                }

                if(event.player.tickCount % PlayerState.SYNC_INTERVAL_TICKS == 0) {
                    var bytes = PlayerState.get(event.player).serializeUpdate();

                    if(bytes.length > 0) {
                        NetworkManager.INSTANCE.sendTo(new S2CPlayerState(bytes), ((ServerPlayer)event.player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
            } else {
                TargetArrowRenderer.tick();
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

    @SubscribeEvent
    static void onRenderHand(RenderHandEvent event) {
        if(event.getItemStack().is(Items.TRAINER_CARD.get())) {
            var mc = Minecraft.getInstance();
            var player = mc.player;

            if(player != null) {
                var ps = PlayerState.get(player);
                TargetArrowRenderer.setTarget(player, ps.getTarget());
                TargetArrowRenderer.render(event.getPoseStack(), event.getPartialTick());
            }
        }
    }
}
