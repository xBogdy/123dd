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
package com.gitlab.srcmc.rctmod.api.data.pack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.utils.ChatUtils;
import com.gitlab.srcmc.rctmod.api.utils.JsonUtils;
import com.gitlab.srcmc.rctmod.api.utils.PathUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class DataPackManager extends SimpleJsonResourceReloadListener implements AutoCloseable {
    private final static Gson GSON = new Gson();
    
    public static final String PATH_GROUPS = "trainers/groups";
    public static final String PATH_SINGLE = "trainers/single";
    public static final String PATH_DEFAULT = "trainers/default";
    public static final String PATH_TRAINERS = "trainers";

    private static class DataLocator {
        public final Map<ResourceLocation, PackResources> groupData = new HashMap<>();
        public final Map<ResourceLocation, PackResources> singleData = new HashMap<>();
        public Map.Entry<ResourceLocation, PackResources> defaultData;
        public final PackType packType;
        public final String fileSuffix;

        public DataLocator(PackType packType, String fileSuffix) {
            this.packType = packType;
            this.fileSuffix = fileSuffix;
        }
    }

    private final Map<String, DataLocator> contextMap = Map.ofEntries(
        Map.entry("mobs", new DataLocator(PackType.SERVER_DATA, ".json")),
        Map.entry("dialogs", new DataLocator(PackType.SERVER_DATA, ".json")),
        Map.entry("loot_table", new DataLocator(PackType.SERVER_DATA, ".json")),
        Map.entry("textures", new DataLocator(PackType.CLIENT_RESOURCES, ".png"))
    );

    // trainer teams are not layered (in groups/single/default) and are therefore
    // handled a bit differently.
    private Map<ResourceLocation, PackResources> trainerTeams = new HashMap<>();
    private PackType packType;

    public DataPackManager(PackType packType) {
        super(GSON, ModCommon.MOD_ID);
        this.packType = packType;
    }

    public void init(ResourceManager resourceManager) {
        this.clear();

        resourceManager.listPacks()
            .filter(dp -> dp.getNamespaces(this.packType).contains(ModCommon.MOD_ID))
            .forEach(this::gather);
    }

    public void listTrainerTeams(ResourceOutput out) {
        this.trainerTeams.forEach((k, v) -> out.accept(k, v.getResource(this.packType, k)));
    }

    @Override
    public void close() {
        for(var dl : this.contextMap.values()) {
            for(var dp : dl.groupData.values()) {
                dp.close();
            }

            for(var dp : dl.singleData.values()) {
                dp.close();
            }

            if(dl.defaultData != null) {
                dl.defaultData.getValue().close();
            }
        }

        for(var dp : this.trainerTeams.values()) {
            dp.close();
        }
    }

    protected void clear() {
        for(var dl : this.contextMap.values()) {
            dl.groupData.clear();
            dl.singleData.clear();
            dl.defaultData = null;
        }

        this.trainerTeams.clear();
    }

    public Optional<ResourceLocation> findResource(String trainerId, String context) {
        var dl = this.contextMap.get(context);

        if(dl == null) {
            return Optional.empty();
        }

        var singlePath = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, context + "/" + PATH_SINGLE + "/" + trainerId + dl.fileSuffix);

        if(dl.singleData.containsKey(singlePath)) {
            return Optional.of(singlePath);
        } else {
            var groupsPath = findGroupsKey(trainerId, dl.groupData);
            
            if(groupsPath != null) {
                return Optional.of(groupsPath);
            }
        }

        if(dl.defaultData != null) {
            return Optional.of(dl.defaultData.getKey());
        }

        return Optional.empty();
    }

    public Optional<TrainerTeam> loadTrainerTeam(String trainerId) {
        var teamResource = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, PATH_TRAINERS + "/" + trainerId + ".json");
        
        if(this.trainerTeams.containsKey(teamResource)) {
            return Optional.of(JsonUtils.loadFromOrThrow(this.trainerTeams.get(teamResource).getResource(this.packType, teamResource), TrainerTeam.class));
        }

        return Optional.empty();
    }

    // TODO: buffer loaded resources
    public <T> void loadResource(String trainerId, String context, Consumer<T> consumer, Class<T> type) {
        this.loadResource(trainerId, context, consumer, TypeToken.get(type));
    }

    public <T> void loadResource(String trainerId, String context, Consumer<T> consumer, TypeToken<T> type) {
        var dl = contextMap.get(context);
        var singlePath = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, context + "/" + PATH_SINGLE + "/" + trainerId + dl.fileSuffix);
        T obj;

        if(dl.singleData.containsKey(singlePath)) {
            obj = JsonUtils.loadFromOrThrow(dl.singleData.get(singlePath).getResource(dl.packType, singlePath), type);
        } else {
            var groupsPath = findGroupsKey(trainerId, dl.groupData);

            if(groupsPath != null) {
                obj = JsonUtils.loadFromOrThrow(dl.groupData.get(groupsPath).getResource(dl.packType, groupsPath), type);
            } else {
                obj = JsonUtils.loadFromOrThrow(dl.defaultData.getValue().getResource(dl.packType, dl.defaultData.getKey()), type);
            }
        }

        if(obj instanceof IDataPackObject dpo) {
            dpo.onLoad(this, trainerId, context);
        }

        consumer.accept(obj);
    }
    
    private void gather(PackResources dataPack) {
        this.gatherTrainers(dataPack);
        this.gatherResources(dataPack);
        ModCommon.LOG.info("Data pack initialized: " + dataPack.packId());
    }

    private void gatherTrainers(PackResources dataPack) {
        dataPack.listResources(this.packType, ModCommon.MOD_ID, PATH_TRAINERS, (rl, io) -> {
            this.trainerTeams.put(rl, dataPack);
        });
    }

    private void gatherResources(PackResources dataPack) {
        for(var entry : this.contextMap.entrySet()) {
            this.scanDataPack(dataPack, entry.getKey(), entry.getValue());
        }
    }

    private void scanDataPack(PackResources dataPack, String context, DataLocator dl) {
        dataPack.listResources(dl.packType, ModCommon.MOD_ID, context + "/" + PATH_GROUPS, (rl, io) -> {
            dl.groupData.put(rl, dataPack);
        });

        dataPack.listResources(dl.packType, ModCommon.MOD_ID, context + "/" + PATH_SINGLE, (rl, io) -> {
            dl.singleData.put(rl, dataPack);
        });

        var defaultRl = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, context + "/" + PATH_DEFAULT + dl.fileSuffix);
        var defaultData = dataPack.getResource(dl.packType, defaultRl);

        if(defaultData != null) {
            dl.defaultData = Map.entry(defaultRl, dataPack);
        }
    }

    private ResourceLocation findGroupsKey(String trainerId, Map<ResourceLocation, PackResources> groups) {
        ResourceLocation key = null;
        var longestGroupId = "";

        for(var groupsEntry : groups.entrySet()) {
            var groupsId = PathUtils.filename(groupsEntry.getKey().getPath());

            if(groupsId.length() > longestGroupId.length() && (trainerId.equals(groupsId) || trainerId.startsWith(groupsId + "_"))) {
                key = groupsEntry.getKey();
                longestGroupId = groupsId;
            }
        }

        return key;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.init(resourceManager);

        // we'll load keep the default dialog for general purpose chats
        this.loadResource("", "dialogs",
            ChatUtils::initDefault,
            new TypeToken<Map<String, String[]>>() {});

        this.close();
    }
}
