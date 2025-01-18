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
package com.gitlab.srcmc.rctmod.api.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.cobblemon.mod.common.Cobblemon;
import com.gitlab.srcmc.rctapi.api.errors.RCTException;
import com.gitlab.srcmc.rctapi.api.trainer.Trainer;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.TrainerBattle;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.save.TrainerBattleMemory;
import com.gitlab.srcmc.rctmod.api.data.save.TrainerPlayerData;
import com.gitlab.srcmc.rctmod.api.utils.PathUtils;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData.Factory;

public class TrainerManager extends SimpleJsonResourceReloadListener {
    private final static Gson GSON = new Gson();

    private Map<String, TrainerMobData> trainerMobs = new HashMap<>();
    private Map<UUID, TrainerBattle> trainerBattles = new HashMap<>();

    private Map<UUID, String> uuidToTrainerId = new HashMap<>();
    private Set<String> playerTrainerIds = new HashSet<>();
    private Set<Player> receivedUpdates = new HashSet<>();

    private MinecraftServer server;
    private ResourceManager resourceManager;
    private boolean configReady;

    private int minRequiredLevelCap;

    public TrainerManager() {
        super(GSON, ModCommon.MOD_ID);
    }

    public void notifyServerReady(MinecraftServer server) {
        ModCommon.RCT.getTrainerRegistry().init(server);
        this.resourceManager = server.getResourceManager();
        this.server = server;
        this.attemptReload();
    }

    public void notifyServerStop(MinecraftServer server) {
        if(server == this.server) {
            this.server = null;
            this.configReady = false;
        }
    }

    public void notifyConfigReady() {
        this.configReady = true;
        this.attemptReload();
    }

    private boolean isReady() {
        return this.configReady && this.resourceManager != null;
    }

    private boolean isServerRunning() {
        return this.server != null && this.server.isRunning();
    }

    private void registerTrainer(String trainerId, TrainerMobData tmd, Map<String, Trainer> oldTrainers) {
        if(this.isServerRunning()) {
            var reg = ModCommon.RCT.getTrainerRegistry();

            try {
                var newTrainer = reg.registerNPC(trainerId, tmd.getTrainerTeam());
                var oldTrainer = oldTrainers.get(trainerId);

                if(oldTrainer != null) {
                    newTrainer.setEntity(oldTrainer.getEntity());
                }
            } catch(RCTException errors) {
                ModCommon.LOG.error("Model validation failure for '" + trainerId + "'");
                errors.getErrors().forEach(error -> ModCommon.LOG.error(error.message));
            } catch(Exception e) {
                // this.trainerMobs.remove(trainerId); // not that it really matters at this point
                ModCommon.LOG.error("Failed to register trainer '" + trainerId + "'", e);
            }
        }
    }

    public String registerPlayer(Player player) {
        var trainerId = this.uuidToTrainerId.get(player.getUUID());

        if(trainerId == null) {
            int attempt = 0;
            var name = player.getName().getString().replace(' ', '_');
            trainerId = name;

            while(this.playerTrainerIds.contains(trainerId)) {
                trainerId = String.format("%s_%d", name, ++attempt);
            }

            this.uuidToTrainerId.put(player.getUUID(), trainerId);
            this.playerTrainerIds.add(trainerId);
        }

        return trainerId;
    }

    public String unregisterPlayer(Player player) {
        var trainerId = this.uuidToTrainerId.remove(player.getUUID());

        if(trainerId != null) {
            this.playerTrainerIds.remove(trainerId);
        }

        return trainerId;
    }

    public String getTrainerId(Player player) {
        if(!this.uuidToTrainerId.containsKey(player.getUUID())) {
            this.registerPlayer(player);
        }

        return this.uuidToTrainerId.get(player.getUUID());
    }

    public void addBattle(Player initiator, TrainerMob opponent) {
        this.trainerBattles.put(initiator.getUUID(), new TrainerBattle(initiator, opponent));
    }

    public void addBattle(Player[] initiatorSidePlayers, TrainerMob[] initiatorSideMobs, Player[] trainerSidePlayers, TrainerMob[] trainerSideMobs) {
        var battle = new TrainerBattle(initiatorSidePlayers, initiatorSideMobs, trainerSidePlayers, trainerSideMobs);
        this.trainerBattles.put(battle.getInitiator().getUUID(), battle);
    }

    public Optional<TrainerBattle> getBattle(UUID initiatorId) {
        return this.trainerBattles.containsKey(initiatorId)
            ? Optional.of(this.trainerBattles.get(initiatorId))
            : Optional.empty();
    }

    public boolean removeBattle(UUID initiatorId) {
        return this.trainerBattles.remove(initiatorId) != null;
    }

    public TrainerMobData getData(TrainerMob mob) {
        return this.getData(mob.getTrainerId());
    }

    public TrainerMobData getData(String trainerId) {
        if(!this.trainerMobs.containsKey(trainerId)) {
            // currently disabled -> spams log -> alternative in TrainerMob
            // if(!trainerId.isEmpty()) {
            //     ModCommon.LOG.error(String.format("Invalid trainer id '%s'", trainerId));
            // }

            return new TrainerMobData();
        }

        return this.trainerMobs.get(trainerId);
    }

    public boolean isValidId(String trainerId) {
        return this.trainerMobs.containsKey(trainerId);
    }

    public int getPlayerLevel(Player player) {
        int maxLevel = 0;

        if(player instanceof ServerPlayer serverPlayer) {
            for(var pk : Cobblemon.INSTANCE.getStorage().getParty(serverPlayer)) {
                maxLevel = Math.max(maxLevel, pk.getLevel());
            }
        }

        return maxLevel;
    }

    public int getActivePokemon(Player player) {
        int count = 0;

        if(player instanceof ServerPlayer serverPlayer) {
            for(var pk : Cobblemon.INSTANCE.getStorage().getParty(serverPlayer)) {
                if(!pk.isFainted()) {
                    count++;
                }
            }
        }

        return count;
    }

    public TrainerPlayerData getData(Player player) {
        var builder = new TrainerPlayerData.Builder(player);
        
        return player.getServer().overworld().getDataStorage().computeIfAbsent(
            new Factory<>(builder::create, builder::of, DataFixTypes.LEVEL),
            TrainerPlayerData.filePath(player));
    }

    public Stream<Map.Entry<String, TrainerMobData>> getAllData() {
        return this.trainerMobs.entrySet().stream();
    }

    public TrainerBattleMemory getBattleMemory(TrainerMob mob) {
        return getBattleMemory(mob.getServer().overworld(), mob.getTrainerId());
    }

    public TrainerBattleMemory getBattleMemory(ServerLevel level, String trainerId) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
            new Factory<>(TrainerBattleMemory::new, TrainerBattleMemory::of, DataFixTypes.LEVEL),
            TrainerBattleMemory.filePath(trainerId));
    }

    public boolean updateRequired(Player player) {
        return this.receivedUpdates.add(player);
    }

    public int getMinRequiredLevelCap() {
        return this.minRequiredLevelCap;
    }

    protected void attemptReload() {
        if(this.isReady()) {
            this.forceReload(this.resourceManager);
        }
    }

    protected void forceReload(ResourceManager resourceManager) {
        var dpm = RCTMod.getInstance().getServerDataManager();
        dpm.init(resourceManager);

        var oldTrainers = new HashMap<String, Trainer>();
        var newTrainerMobs = new HashMap<String, TrainerMobData>();

        if(this.isServerRunning()) {
            var reg = ModCommon.RCT.getTrainerRegistry();

            reg.getIds().stream()
                .map(tid -> Map.<String, Trainer>entry(tid, reg.getById(tid)))
                .filter(entry -> entry.getValue() instanceof TrainerNPC)
                .forEach(entry -> oldTrainers.put(entry.getKey(), entry.getValue()));

            reg.clearNPCs();
        }

        dpm.listTrainerTeams((rl, io) -> {
            var trainerId = PathUtils.filename(rl.getPath());

            dpm.loadResource(trainerId, "mobs", tmd -> {
                newTrainerMobs.put(trainerId, tmd);
                this.registerTrainer(trainerId, tmd, oldTrainers);
            }, TrainerMobData.class);
        });

        newTrainerMobs.forEach((trainerId, tmd) -> {
            tmd.getMissingRequirements(Set.of(), true).forEach(tid -> {
                var tmd2 = newTrainerMobs.get(tid);

                if(tmd2 != null) {
                    tmd2.addFollowedBy(trainerId);
                }
            });
        });

        var mrlvlcap = new int[]{100};

        newTrainerMobs.values().forEach(tmd -> {
            tmd.setRewardLevelCap(tmd.getFollowdBy()
                .stream().map(tid -> {
                    var tm = newTrainerMobs.get(tid);
                    var tl = tm.getTrainerTeam().getTeam().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0);
                    return tl == 100 ? tl : tm.getRequiredLevelCap();
                }).max(Integer::compare).orElse(tmd.getRequiredLevelCap()));
            
            if(!tmd.getFollowdBy().isEmpty()) {
                mrlvlcap[0] = Math.min(mrlvlcap[0], tmd.getRequiredLevelCap());
            }
        });

        // atomic operation ensures 'valid' trainer ids are always properly initialized (when accessed from a different thread)
        this.trainerMobs = newTrainerMobs;

        if(this.isServerRunning()) {
            ModCommon.LOG.info(String.format("Registered %d trainers", this.trainerMobs.size()));
        }

        dpm.close();
        this.minRequiredLevelCap = mrlvlcap[0];
        this.receivedUpdates = new HashSet<>();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.resourceManager = resourceManager;
        this.attemptReload();
    }
}
