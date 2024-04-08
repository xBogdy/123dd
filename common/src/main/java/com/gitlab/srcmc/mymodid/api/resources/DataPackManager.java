package com.gitlab.srcmc.mymodid.api.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.api.trainer.TrainerTeam;
import com.gitlab.srcmc.mymodid.api.utils.JsonUtils;
import com.gitlab.srcmc.mymodid.api.utils.PathUtils;
import com.google.gson.reflect.TypeToken;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.resources.ResourceManager;

public class DataPackManager implements AutoCloseable {
    private static final String PATH_GROUPS = "trainers/groups";
    private static final String PATH_SINGLE = "trainers/single";
    private static final String PATH_DEFAULT = "trainers/default";
    private static final String PATH_TRAINERS = "trainers";

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

    private Map<String, DataLocator> contextMap = Map.ofEntries(
        Map.entry("mobs", new DataLocator(PackType.SERVER_DATA, ".json")),
        Map.entry("dialogs", new DataLocator(PackType.SERVER_DATA, ".json")),
        Map.entry("loot_tables", new DataLocator(PackType.SERVER_DATA, ".json")),
        Map.entry("textures", new DataLocator(PackType.CLIENT_RESOURCES, ".png"))
    );

    // trainer teams are not layered (in groups/single/default) and are therefore
    // handled a bit differently.
    private Map<ResourceLocation, PackResources> trainerTeams = new HashMap<>();

    public void init(ResourceManager resourceManager) {
        this.clear();

        resourceManager.listPacks()
            .filter(dp -> dp.getNamespaces(PackType.SERVER_DATA).contains(ModCommon.MOD_ID))
            .forEach(this::gather);
    }

    public void listTrainerTeams(ResourceOutput out) {
        this.trainerTeams.forEach((k, v) -> out.accept(k, v.getResource(PackType.SERVER_DATA, k)));
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

            dl.defaultData.getValue().close();
        }

        for(var dp : trainerTeams.values()) {
            dp.close();
        }
    }

    protected void clear() {
        for(var dl : this.contextMap.values()) {
            dl.groupData.clear();
            dl.singleData.clear();
            dl.defaultData = null;
        }

        trainerTeams.clear();
    }

    public Optional<ResourceLocation> findResource(String trainerId, String context) {
        var dl = contextMap.get(context);
        var singlePath = new ResourceLocation(ModCommon.MOD_ID, context + "/" + PATH_SINGLE + "/" + trainerId + dl.fileSuffix);

        if(dl.singleData.containsKey(singlePath)) {
            return Optional.of(singlePath);
        } else {
            var groupsPath = findGroupsKey(trainerId, dl.groupData);
            
            if(groupsPath != null) {
                return Optional.of(groupsPath);
            }
        }

        return Optional.of(dl.defaultData.getKey());
    }

    public Optional<TrainerTeam> loadTrainerTeam(String trainerId) {
        var teamResource = new ResourceLocation(ModCommon.MOD_ID, PATH_TRAINERS + "/" + trainerId + ".json");
        
        if(trainerTeams.containsKey(teamResource)) {
            return Optional.of(JsonUtils.loadFromOrThrow(trainerTeams.get(teamResource).getResource(PackType.SERVER_DATA, teamResource), TrainerTeam.class));
        }

        return Optional.empty();
    }
    
    public <T> void loadResource(String trainerId, String context, Consumer<T> consumer, Class<T> type) {
        loadResource(trainerId, context, consumer, TypeToken.get(type));
    }

    public <T> void loadResource(String trainerId, String context, Consumer<T> consumer, TypeToken<T> type) {
        var dl = contextMap.get(context);
        var singlePath = new ResourceLocation(ModCommon.MOD_ID, context + "/" + PATH_SINGLE + "/" + trainerId + dl.fileSuffix);
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

        consumer.accept(obj);

        if(obj instanceof IDataPackObject dpo) {
            dpo.onLoad(this, trainerId, context);
        }
    }

    private void gather(PackResources dataPack) {
        this.gatherTrainers(dataPack);
        this.gatherResources(dataPack);
    }

    private void gatherTrainers(PackResources dataPack) {
        dataPack.listResources(PackType.SERVER_DATA, ModCommon.MOD_ID, PATH_TRAINERS, (rl, io) -> {
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

        var defaultRl = new ResourceLocation(ModCommon.MOD_ID, context + "/" + PATH_DEFAULT + dl.fileSuffix);
        var defaultData = dataPack.getResource(dl.packType, defaultRl);

        if(defaultData != null) {
            dl.defaultData = Map.entry(defaultRl, dataPack);
        }
    }

    private ResourceLocation findGroupsKey(String trainerId, Map<ResourceLocation, PackResources> groups) {
        for(var groupsEntry : groups.entrySet()) {
            var groupsId = PathUtils.filename(groupsEntry.getKey().getPath());

            if(trainerId.equals(groupsId) || trainerId.startsWith(groupsId + "_")) {
                return groupsEntry.getKey();
            }
        }

        return null;
    }
}
