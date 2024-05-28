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
package com.gitlab.srcmc.rctmod.api.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;

public class TrainerManager extends SimpleJsonResourceReloadListener {
    private final static Gson GSON = new Gson();

    private Map<String, TrainerMobData> trainerMobs = new HashMap<>();
    private Map<UUID, TrainerBattle> trainerBattles = new HashMap<>();
    private Function<Player, Integer> playerLevelSupplier;
    private Function<Player, Integer> avtivePokemonSupplier;

    public TrainerManager() {
        super(GSON, ModCommon.MOD_ID);
        this.playerLevelSupplier = p -> TrainerManager.this.getData(p).getLevelCap();
        this.avtivePokemonSupplier = p -> 0;
    }

    public TrainerManager(Function<Player, Integer> playerLevelSupplier, Function<Player, Integer> avtivePokemonSupplier) {
        super(GSON, ModCommon.MOD_ID);
        this.playerLevelSupplier = playerLevelSupplier;
        this.avtivePokemonSupplier = avtivePokemonSupplier;
    }

    public void addBattle(Player initiator, TrainerMob opponent) {
        trainerBattles.put(initiator.getUUID(), new TrainerBattle(initiator, opponent));
    }

    public void addBattle(Player[] initiatorSidePlayers, TrainerMob[] initiatorSideMobs, Player[] trainerSidePlayers, TrainerMob[] trainerSideMobs) {
        var battle = new TrainerBattle(initiatorSidePlayers, initiatorSideMobs, trainerSidePlayers, trainerSideMobs);
        trainerBattles.put(battle.getInitiator().getUUID(), battle);
    }

    public Optional<TrainerBattle> getBattle(UUID initiatorId) {
        return trainerBattles.containsKey(initiatorId)
            ? Optional.of(trainerBattles.get(initiatorId))
            : Optional.empty();
    }

    public boolean removeBattle(UUID initiatorId) {
        return trainerBattles.remove(initiatorId) != null;
    }

    public TrainerMobData getData(TrainerMob mob) {
        return this.getData(mob.getTrainerId());
    }

    public TrainerMobData getData(String trainerId) {
        if(!trainerMobs.containsKey(trainerId)) {
            // currently disabled -> spams log -> alternative in TrainerMob
            // if(!trainerId.isEmpty()) {
            //     ModCommon.LOG.error(String.format("Invalid trainer id '%s'", trainerId));
            // }

            return new TrainerMobData();
        }

        return this.trainerMobs.get(trainerId);
    }

    public boolean isValidId(String trainerId) {
        return trainerMobs.containsKey(trainerId);
    }

    public int getPlayerLevel(Player player) {
        return this.playerLevelSupplier.apply(player);
    }

    public int getActivePokemon(Player player) {
        return this.avtivePokemonSupplier.apply(player);
    }

    public TrainerPlayerData getData(Player player) {
        return player.getServer().overworld().getDataStorage().computeIfAbsent(
            TrainerPlayerData::of,
            TrainerPlayerData::new,
            TrainerPlayerData.filePath(player));
    }

    public Stream<Map.Entry<String, TrainerMobData>> getAllData() {
        return trainerMobs.entrySet().stream();
    }

    public TrainerBattleMemory getBattleMemory(TrainerMob mob) {
        return getBattleMemory(mob.getServer().overworld(), mob.getTrainerId());
    }

    public TrainerBattleMemory getBattleMemory(ServerLevel level, String trainerId) {
        var ds = level.getDataStorage();

        return ds.computeIfAbsent(
            TrainerBattleMemory::of,
            TrainerBattleMemoryLegacy.builder(trainerId, ds)::build,
            TrainerBattleMemory.filePath(trainerId));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        var dpm = RCTMod.get().getServerDataManager();
        dpm.init(resourceManager);
        this.trainerMobs.clear();

        dpm.listTrainerTeams((rl, io) -> {
            var trainerId = PathUtils.filename(rl.getPath());
            dpm.loadResource(trainerId, "mobs", tdm -> this.trainerMobs.put(trainerId, tdm), TrainerMobData.class);
        });

        dpm.close();
    }
}
