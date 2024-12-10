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
package com.gitlab.srcmc.rctmod;

import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedPreEvent;
import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.commands.PlayerCommands;
import com.gitlab.srcmc.rctmod.commands.TrainerCommands;
import com.gitlab.srcmc.rctmod.network.PlayerStatePayload;
import com.mojang.brigadier.CommandDispatcher;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent.LevelTick;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import kotlin.Unit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.EnvironmentInterface;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;

@EnvironmentInterface(itf = ModCommon.DedicatedServer.class, value = EnvType.SERVER)
public class ModCommon {
    interface DedicatedServer {}

    public static final String MOD_ID = "rctmod";
    public static final String MOD_NAME = "Radical Cobblemon Trainers";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static Supplier<Player> player;

    public static void init() {
        ModRegistries.init();
        ModCommon.registerEvents();
        ReloadListenerRegistry.register(PackType.SERVER_DATA, RCTMod.getInstance().getTrainerManager());

        if(DedicatedServer.class.isAssignableFrom(ModCommon.class)) {
            NetworkManager.registerS2CPayloadType(PlayerStatePayload.TYPE, PlayerStatePayload.CODEC);
        }
    }

    public static void initPlayer(Supplier<Player> player) {
        ModCommon.player = player;
    }

    public static Player localPlayer() {
        if(ModCommon.player == null) {
            throw new IllegalStateException("Local player not initialized, call ModCommon.initPlayer on the client side");
        }

        return ModCommon.player.get();
    }

    static void registerEvents() {
        CommandRegistrationEvent.EVENT.register(ModCommon::onCommandRegistration);
        LifecycleEvent.SERVER_STARTING.register(ModCommon::onServerStarting);
        LevelTick.SERVER_LEVEL_PRE.register(ModCommon::onServerWorldTick);
        LevelTick.SERVER_PRE.register(ModCommon::onServerTick);
        PlayerEvent.PLAYER_JOIN.register(ModCommon::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(ModCommon::onPlayerQuit);
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, ModCommon::onBattleVictory);
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(Priority.HIGHEST, ModCommon::onExperienceGained);
    }

    // CommandRegistrationEvent

    static void onCommandRegistration(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, CommandSelection env) {
        PlayerCommands.register(dispatcher);
        TrainerCommands.register(dispatcher);
    }

    // LifecycleEvent

    static void onServerStarting(MinecraftServer server) {
        RCTApi.getInstance().getTrainerRegistry().init(server);
        RCTMod.getInstance().getTrainerSpawner().init(server.overworld());
        RCTMod.getInstance().getTrainerManager().init(server);
        RCTMod.getInstance().getTrainerManager().forceReload();
    }

    // LevelTick

    static void onServerTick(MinecraftServer server) {
        RCTMod.getInstance().getTrainerSpawner().checkDespawns();
    }

    static void onServerWorldTick(ServerLevel level) {
        level.players().forEach(player -> {
            if(player.tickCount % RCTMod.getInstance().getServerConfig().spawnIntervalTicks() == 0) {
                RCTMod.getInstance().getTrainerSpawner().attemptSpawnFor(player);
            }

            if(player.tickCount % PlayerState.SYNC_INTERVAL_TICKS == 0) {
                var bytes = PlayerState.get(player).serializeUpdate();

                if(bytes.length > 0) {
                    NetworkManager.sendToPlayer(player, PlayerStatePayload.of(bytes));
                }
            }
        });
    }

    // PlayerEvent
    
    static void onPlayerJoin(ServerPlayer player) {
        var trainerId = RCTMod.getInstance().getTrainerManager().registerPlayer(player);
        RCTApi.getInstance().getTrainerRegistry().registerPlayer(trainerId, player);
        ModCommon.LOG.info(String.format("Registered trainer player: %s", trainerId));
    }

    static void onPlayerQuit(ServerPlayer player) {
        var trainerId = RCTMod.getInstance().getTrainerManager().unregisterPlayer(player);

        if(RCTApi.getInstance().getTrainerRegistry().unregisterById(trainerId) != null) {
            ModCommon.LOG.info(String.format("Unregistered trainer player: %s", trainerId));
        }
    }

    // CobblemonEvent

    public static Unit onBattleVictory(BattleVictoryEvent event) {
        if(!ModCommon.removeBattleFromInitiator(event.getWinners(), true)) {
            ModCommon.removeBattleFromInitiator(event.getLosers(), false);
        }

        return Unit.INSTANCE;
    }

    public static Unit onExperienceGained(ExperienceGainedPreEvent event) {
        if(!RCTMod.getInstance().getServerConfig().allowOverLeveling()) {
            var owner = event.getPokemon().getOwnerPlayer();

            if(owner != null) {
                var playerTr = RCTMod.getInstance().getTrainerManager().getData(owner);
                var maxExp = event.getPokemon().getExperienceToLevel(playerTr.getLevelCap());

                if(maxExp < event.getExperience()) {
                    owner.server.getCommands().performPrefixedCommand(
                        owner.server.createCommandSourceStack().withSuppressedOutput(),
                        String.format("title %s actionbar \"%s is %s the level cap (%d)\"",
                            owner.getName().getString(),
                            event.getPokemon().getDisplayName().getString(),
                            event.getPokemon().getLevel() == playerTr.getLevelCap() ? "at" : "over",
                            playerTr.getLevelCap()));
                }

                event.setExperience(Math.min(event.getExperience(), maxExp));
            }
        }

        return Unit.INSTANCE;
    }

    private static boolean removeBattleFromInitiator(List<BattleActor> actors, boolean winners) {
        for(var actor : actors) {
            var trainerBattle = RCTMod.getInstance().getTrainerManager().getBattle(actor.getUuid());

            if(trainerBattle.isPresent()) {
                RCTMod.getInstance().getTrainerManager().removeBattle(actor.getUuid());
                trainerBattle.get().distributeRewards(winners);
                return true;
            }
        }

        return false;
    }
}
