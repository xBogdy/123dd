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
package com.gitlab.srcmc.rctmod.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedPreEvent;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.save.TrainerBattleMemory;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.utils.ArrUtils;
import com.gitlab.srcmc.rctmod.commands.PlayerCommands;
import com.gitlab.srcmc.rctmod.commands.TrainerCommands;
import com.gitlab.srcmc.rctmod.commands.utils.SuggestionUtils;
import com.gitlab.srcmc.rctmod.network.BatchedPayloads;
import com.gitlab.srcmc.rctmod.network.TrainerTargetPayload;
import com.gitlab.srcmc.rctmod.world.entities.TrainerAssociation;
import com.mojang.brigadier.CommandDispatcher;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent.LevelTick;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import kotlin.Unit;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;

public class ModServer {
    record TrainerManagerPayloadTargets(byte[] bytes, int remainingBatches, Iterable<ServerPlayer> players) {}
    private static final Map<UUID, Queue<Map.Entry<byte[], Integer>>> PLAYER_STATE_PAYLOADS = new HashMap<>();
    private static final Queue<TrainerManagerPayloadTargets> TRAINER_MANAGER_PAYLOADS = new LinkedList<>();

    public static void init() {
        NetworkManager.registerS2CPayloadType(BatchedPayloads.PLAYER_STATE.TYPE, BatchedPayloads.PLAYER_STATE.CODEC);
        NetworkManager.registerS2CPayloadType(BatchedPayloads.TRAINER_MANAGER.TYPE, BatchedPayloads.TRAINER_MANAGER.CODEC);
        NetworkManager.registerS2CPayloadType(TrainerTargetPayload.TYPE, TrainerTargetPayload.CODEC);
    }

    public static void initCommon() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, RCTMod.getInstance().getTrainerManager());
        registerEvents();
    }

    static void registerEvents() {
        CommandRegistrationEvent.EVENT.register(ModServer::onCommandRegistration);
        LifecycleEvent.SERVER_STARTING.register(ModServer::onServerStarting);
        LifecycleEvent.SERVER_STOPPED.register(ModServer::onServerStopped);
        LifecycleEvent.SERVER_LEVEL_SAVE.register(ModServer::onServerLevelSave);
        LevelTick.SERVER_LEVEL_PRE.register(ModServer::onServerWorldTick);
        LevelTick.SERVER_PRE.register(ModServer::onServerTick);
        PlayerEvent.PLAYER_JOIN.register(ModServer::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(ModServer::onPlayerQuit);
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, ModServer::onBattleVictory);
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(Priority.HIGHEST, ModServer::onExperienceGained);
    }

    public static void syncTrainerManger(ServerPlayer... players) {
        syncTrainerManger(List.of(players));
    }

    public static void syncTrainerManger(Iterable<ServerPlayer> players) {
        var list = new LinkedList<ServerPlayer>();

        for(var p : players) {
            if(p.getGameProfile() != p.getServer().getSingleplayerProfile()) {
                list.add(p);
            }
        }

        if(!list.isEmpty()) {
            for(var pl : RCTMod.getInstance().getTrainerManager().toPayloads()) {
                TRAINER_MANAGER_PAYLOADS.offer(new TrainerManagerPayloadTargets(pl.bytes(), pl.remainingBatches(), list));
            }
        }
    }

    // CommandRegistrationEvent

    static void onCommandRegistration(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, CommandSelection env) {
        PlayerCommands.register(dispatcher);
        TrainerCommands.register(dispatcher);
    }

    // LifecycleEvent

    static void onServerLevelSave(ServerLevel level) {
        if(level.getServer().overworld() == level) {
            TrainerBattleMemory.clearLegacyFiles();
        }
    }

    static void onServerStarting(MinecraftServer server) {
        PLAYER_STATE_PAYLOADS.clear();
        TRAINER_MANAGER_PAYLOADS.clear();
        RCTMod.getInstance().getTrainerSpawner().init(server.overworld());
        RCTMod.getInstance().getTrainerManager().setIsReloadedAsDatapack(true);
        RCTMod.getInstance().getTrainerManager().setServer(server);
    }

    static void onServerStopped(MinecraftServer server) {
        RCTMod.getInstance().getTrainerManager().setIsReloadedAsDatapack(false);
        RCTMod.getInstance().getTrainerManager().setServer(null);
    }

    // LevelTick

    static void onServerTick(MinecraftServer server) {
        var tm = RCTMod.getInstance().getTrainerManager();

        if(server.isRunning()) {
            RCTMod.getInstance().getTrainerSpawner().checkDespawns();

            if(tm.isReloadRequired()) {
                tm.loadTrainers();

                if(TrainerBattleMemory.getVersion(server.overworld().getDataStorage()).isOutdated()) {
                    TrainerBattleMemory.migrate(server, tm);
                }

                SuggestionUtils.initSuggestions();
            }

            while(!TRAINER_MANAGER_PAYLOADS.isEmpty()) {
                var pl = TRAINER_MANAGER_PAYLOADS.poll();
                var it = pl.players().iterator();
                
                while(it.hasNext()) {
                    if(it.next().hasDisconnected()) {
                        it.remove();
                    }
                }

                if(pl.players().iterator().hasNext()) {
                    NetworkManager.sendToPlayers(pl.players(), BatchedPayloads.TRAINER_MANAGER.payload(pl.bytes(), pl.remainingBatches()));
                    break;
                }
            }
        }
    }

    static void onServerWorldTick(ServerLevel level) {
        var rct = RCTMod.getInstance();

        if(!rct.getTrainerManager().isReloadRequired()) {
            var cfg = rct.getServerConfig();
            var trs = rct.getTrainerSpawner();

            level.players().forEach(player -> {
                var maxCountPl = cfg.maxTrainersPerPlayer();
                
                if(maxCountPl > 0) {
                    var spawnCountPl = trs.getSpawnCount(player.getUUID());
                    var ticksRange = Math.max(0, cfg.spawnIntervalTicksMaximum() - cfg.spawnIntervalTicks());
                    var spawnIntervalTicks = cfg.spawnIntervalTicks() + (int)(ticksRange*(maxCountPl > 1 ? Math.min(1, spawnCountPl/(float)(maxCountPl - 1)) : 1));

                    if(spawnIntervalTicks == 0 || player.tickCount % spawnIntervalTicks == 0) {
                        rct.getTrainerSpawner().attemptSpawnFor(player);
                    }
                }

                if(player.tickCount % TrainerAssociation.SPAWN_INTERVAL_TICKS == 0) {
                    if(cfg.spawnTrainerAssociation()) {
                        TrainerAssociation.trySpawnFor(player);
                    }
                }

                if(player.tickCount % PlayerState.SYNC_INTERVAL_TICKS == 0) {
                    var bytes = PlayerState.get(player).serializeUpdate();
                    var payloads = PLAYER_STATE_PAYLOADS.computeIfAbsent(player.getUUID(), k -> new LinkedList<>());

                    if(bytes.length > 0) {
                        var batches = ArrUtils.split(bytes, Math.max(PlayerState.MIN_BATCH_SIZE, Math.min(PlayerState.MAX_BATCH_SIZE, bytes.length / PlayerState.SYNC_INTERVAL_TICKS)));
                        int i = batches.size();

                        for(var batch : batches) {
                            payloads.offer(Map.entry(batch, --i));
                        }
                    }

                    if(!payloads.isEmpty()) {
                        var pl = payloads.poll();
                        NetworkManager.sendToPlayer(player, BatchedPayloads.PLAYER_STATE.payload(pl.getKey(), pl.getValue()));
                    }
                }
            });
        }
    }

    // PlayerEvent

    static void onPlayerJoin(ServerPlayer player) {
        PLAYER_STATE_PAYLOADS.put(player.getUUID(), new LinkedList<>());
        PlayerState.initFor(player);
        var trainerId = RCTMod.getInstance().getTrainerManager().registerPlayer(player);
        ModCommon.RCT.getTrainerRegistry().registerPlayer(trainerId, player);
        ModCommon.LOG.info(String.format("Registered trainer player: %s", trainerId));
        syncTrainerManger(player);
    }

    static void onPlayerQuit(ServerPlayer player) {
        var trainerId = RCTMod.getInstance().getTrainerManager().unregisterPlayer(player);

        if(ModCommon.RCT.getTrainerRegistry().unregisterById(trainerId) != null) {
            ModCommon.LOG.info(String.format("Unregistered trainer player: %s", trainerId));
        }
    }

    // CobblemonEvent

    public static Unit onBattleVictory(BattleVictoryEvent event) {
        if(!ModServer.removeBattleFromInitiator(event.getWinners(), true)) {
            ModServer.removeBattleFromInitiator(event.getLosers(), false);
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
