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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctapi.api.util.Text;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.utils.JsonUtils.Exclude;
import com.google.gson.reflect.TypeToken;

import net.minecraft.resources.ResourceLocation;

public class TrainerMobData implements IDataPackObject, Serializable {
    private static final long serialVersionUID = 0L;

    private String type = "normal";
    private String signatureItem = "";
    private List<Set<String>> requiredDefeats = new ArrayList<>();
    private Set<String> series = new HashSet<>();
    private Set<String> substitutes = new HashSet<>();
    private boolean optional;
    
    private int maxTrainerWins = 3;
    private int maxTrainerDefeats = 1;
    private int battleCooldownTicks = 240;
    
    private float spawnWeightFactor = 1F; // >= 0
    private Set<String> biomeTagBlacklist = new HashSet<>();
    private Set<String> biomeTagWhitelist = new HashSet<>();

    @Exclude private int rewardLevelCap;
    @Exclude private Set<String> followdBy = new HashSet<>();
    @Exclude private Map<String, Text[]> dialog = new HashMap<>();
    @Exclude private TrainerTeam trainerTeam;
    @Exclude private RLWrapper textureResource;
    @Exclude private RLWrapper lootTableResource;

    private transient Supplier<TrainerType> lazyType;

    public TrainerMobData() {
        this.textureResource = new RLWrapper(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/" + DataPackManager.PATH_DEFAULT + ".png"));
        this.lootTableResource = new RLWrapper(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, DataPackManager.PATH_DEFAULT));
        this.trainerTeam = new TrainerTeam();
        this.init();
    }

    public TrainerMobData(TrainerMobData origin) {
        this.type = origin.type;
        this.signatureItem = origin.signatureItem;
        this.rewardLevelCap = origin.rewardLevelCap;
        this.requiredDefeats = List.copyOf(origin.requiredDefeats);
        this.followdBy = Set.copyOf(origin.followdBy);
        this.optional = origin.optional;
        this.maxTrainerWins = origin.maxTrainerWins;
        this.maxTrainerDefeats = origin.maxTrainerDefeats;
        this.battleCooldownTicks = origin.battleCooldownTicks;
        this.biomeTagBlacklist = Set.copyOf(origin.biomeTagBlacklist);
        this.biomeTagWhitelist = Set.copyOf(origin.biomeTagWhitelist);
        this.dialog = Map.copyOf(origin.dialog);
        this.textureResource = origin.textureResource;
        this.lootTableResource = origin.lootTableResource;
        this.trainerTeam = origin.trainerTeam; // no need for deep copy since immutable
        this.init();
    }

    private void init() {
        this.lazyType = () -> {
            var type = TrainerType.valueOf(this.type.toLowerCase());
            this.lazyType = () -> type;
            return this.lazyType.get();
        };
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(this.getType());
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        var type = TrainerType.registerOrGet(this.type, (TrainerType)ois.readObject());
        this.lazyType = () -> type;
    }

    public TrainerType getType() {
        return this.lazyType.get();
    }

    public void setRewardLevelCap(int levelCap) {
        this.rewardLevelCap = Math.min(100, Math.max(1, Math.max(this.getRequiredLevelCap(), levelCap)));
    }

    public int getRewardLevelCap() {
        return this.rewardLevelCap;
    }

    public int getRequiredLevelCap() {
        var cfg = RCTMod.getInstance().getServerConfig();
        return Math.max(1, Math.min(100, this.getTrainerTeam().getTeam().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0) + cfg.additiveLevelCapRequirement()));
    }

    public boolean isOfSeries(String seriesId) {
        return this.series.isEmpty() || this.series.contains(seriesId);
    }

    public Stream<String> getSeries() {
        return this.series.stream();
    }

    public List<Set<String>> getRequiredDefeats() {
        return Collections.unmodifiableList(this.requiredDefeats);
    }

    public Set<String> getSubstitutes() {
        return Collections.unmodifiableSet(this.substitutes);
    }

    public Stream<String> getMissingRequirements(Set<String> defeatedTrainerIds) {
        return this.getMissingRequirements(defeatedTrainerIds, false);
    }

    public Stream<String> getMissingRequirements(Set<String> defeatedTrainerIds, boolean includeAlternatives) {
        return this.requiredDefeats.stream().mapMulti((set, cons) -> {
            if(includeAlternatives) {
                if(set.stream().noneMatch(defeatedTrainerIds::contains)) {
                    set.stream().forEach(cons);
                }
            } else if(!set.isEmpty() && set.stream().noneMatch(defeatedTrainerIds::contains)) {
                cons.accept(set.stream().findFirst().get());
            }
        });
    }

    public boolean isOptional() {
        return this.optional;
    }

    public int getMaxTrainerWins() {
        return this.maxTrainerWins;
    }

    public int getMaxTrainerDefeats() {
        return this.maxTrainerDefeats;
    }

    public int getBattleCooldownTicks() {
        return this.battleCooldownTicks;
    }

    public float getSpawnWeightFactor() {
        return this.spawnWeightFactor;
    }

    public Set<String> getBiomeTagBlacklist() {
        return Collections.unmodifiableSet(this.biomeTagBlacklist);
    }

    public Set<String> getBiomeTagWhitelist() {
        return Collections.unmodifiableSet(this.biomeTagWhitelist);
    }

    public Set<String> getFollowedBy() {
        return Collections.unmodifiableSet(this.followdBy);
    }
    
    public boolean addFollowedBy(String trainerId) {
        return this.followdBy.add(trainerId);
    }

    public boolean removeFollowdBy(String trainerId) {
        return this.followdBy.remove(trainerId);
    }

    public void clearFollowedBy(String trainerId) {
        this.followdBy.clear();
    }

    public Map<String, Text[]> getDialog() {
        return Collections.unmodifiableMap(this.dialog);
    }

    public ResourceLocation getTextureResource() {
        return this.textureResource.resourceLocation();
    }

    public ResourceLocation getLootTableResource() {
        return this.lootTableResource.resourceLocation();
    }

    public TrainerTeam getTrainerTeam() {
        return this.trainerTeam;
    }

    public String getSignatureItem() {
        return this.signatureItem;
    }

    @Override
    public void onLoad(DataPackManager dpm, String trainerId, String context) {
        var lootTableResource = dpm.findResource(trainerId, "loot_table");
        var textureResource = dpm.findResource(trainerId, "textures");

        if(textureResource.isPresent()) {
            this.textureResource = new RLWrapper(textureResource.get());
        }

        if(lootTableResource.isPresent()) {
            // the loot table is loaded by net.minecraft.world.level.storage.loot.LootDataManager
            // which resolves a 'shorthand' resource location automatically.
            this.lootTableResource = new RLWrapper(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, lootTableResource.get().getPath()
                .replace("loot_table/", "")
                .replace(".json", "")));
        }

        dpm.loadResource(trainerId, "dialogs",
            dialog -> this.dialog = dialog,
            new TypeToken<Map<String, Text[]>>() {});

        if(this.dialog == null) {
            this.dialog = new HashMap<>();
        }

        var trainerTeam = dpm.loadTrainerTeam(trainerId);

        if(trainerTeam.isPresent()) {
            this.trainerTeam = trainerTeam.get();
        }
    }

    class RLWrapper implements Serializable {
        private static final long serialVersionUID = 0L;
        
        private transient Supplier<ResourceLocation> rlSup;
        private transient ResourceLocation rl;
        private String namespace, path;

        public RLWrapper() {
            this(null);
        }

        public RLWrapper(ResourceLocation rl) {
            if(rl != null) {
                this.namespace = rl.getNamespace();
                this.path = rl.getPath();
            }

            this.rl = rl;
            this.init();
        }

        private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
            ois.defaultReadObject();
            this.init();
        }

        public ResourceLocation resourceLocation() {
            return this.rlSup.get();
        }

        private void init() {
            this.rlSup = () -> {
                if(this.rl == null) {
                    this.rl = ResourceLocation.fromNamespaceAndPath(this.namespace, this.path);
                }
                
                this.rlSup = () -> this.rl;
                return this.rl;
            };
        }
    }
}
