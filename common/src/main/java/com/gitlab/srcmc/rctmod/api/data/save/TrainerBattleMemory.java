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
package com.gitlab.srcmc.rctmod.api.data.save;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.service.TrainerManager;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;

public class TrainerBattleMemory extends SavedData {
    private static final int GROUPS = 42; // change to update (GROUPS > 0)
    private Map<String, Map<UUID, Integer>> defeatedBy = new HashMap<>();

    public static TrainerBattleMemory of(CompoundTag tag, Provider provider) {
        var tbm = new TrainerBattleMemory();
        var defeats = tag.getCompound("defeats");

        defeats.getAllKeys().forEach(tid -> {
            var defeatedBy = new HashMap<UUID, Integer>();
            var defeatsTag = defeats.getCompound(tid);
            defeatsTag.getAllKeys().forEach(pid -> defeatedBy.put(UUID.fromString(pid), defeatsTag.getInt(pid)));
            tbm.defeatedBy.put(tid, defeatedBy);
        });

        return tbm;
    }

    public static String filePath(String trainerId, Version ver) {
        return filePath(trainerId, ver.groups);
    }

    protected static String filePath(String trainerId, int groups) {
        return String.format("%s.trainers.%d.mem", ModCommon.MOD_ID, groupdId(trainerId, groups));
    }

    protected static int groupdId(String trainerId, int groups) {
        return groups > 0 ? (trainerId.hashCode() & 0xffffff) % groups : 0;
    }

    public void addDefeatedBy(String trainerId, Player player) {
        var count = this.defeatedBy.getOrDefault(trainerId, Map.of()).get(player.getUUID());

        if(count == null) {
            count = 0;
        }

        if(count < Integer.MAX_VALUE) {
            var rct = RCTMod.getInstance();

            if(rct.getSeriesManager().getGraph(rct.getTrainerManager().getData(player).getCurrentSeries()).contains(trainerId)) {
                PlayerState.get(player).addDefeat(trainerId);
            }

            this.defeatedBy.computeIfAbsent(trainerId, key -> new HashMap<>()).put(player.getUUID(), count + 1);
            this.setDirty();
        }
    }

    public void setDefeatedBy(String trainerId, Player player, int count) {
        var prevCount = this.defeatedBy.computeIfAbsent(trainerId, key -> new HashMap<>()).put(player.getUUID(), count);

        if(prevCount == null || prevCount != count) {
            var rct = RCTMod.getInstance();

            if(rct.getSeriesManager().getGraph(rct.getTrainerManager().getData(player).getCurrentSeries()).contains(trainerId)) {
                PlayerState.get(player).setDefeats(trainerId, count);
            }

            this.setDirty();
        }
    }

    public int getDefeatByCount(String trainerId, Player player) {
        var count = this.defeatedBy.getOrDefault(trainerId, Map.of()).get(player.getUUID());
        return count == null ? 0 : count;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, Provider provider) {
        var defeatsTag = new CompoundTag();

        this.defeatedBy.forEach((tid, defeatedBy) -> {
            var tag = new CompoundTag();
            defeatedBy.forEach((pid, count) -> tag.putInt(pid.toString(), count));
            defeatsTag.put(tid, tag);
        });

        compoundTag.put("defeats", defeatsTag);
        return compoundTag;
    }

    // VERSION MIGRATION (from different GROUPS or legacy format (pre 0.14.2))

    private static final List<File> LEGACY_FILES = new ArrayList<>();

    public static class Version extends SavedData {
        private int groups;

        private static String filePath() {
            return String.format("%s.trainers.ver", ModCommon.MOD_ID);
        }

        private static Version of(CompoundTag tag, Provider provider) {
            var ver = new Version();
            ver.groups = tag.getInt("groups");
            return ver;
        }

        public boolean isOutdated() {
            return this.groups != GROUPS;
        }

        public void update() {
            this.groups = GROUPS;
            this.setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag tag, Provider provider) {
            tag.putInt("groups", this.groups);
            return tag;
        }

        @Override
        public int hashCode() {
            return this.groups;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Version other) && other.groups == this.groups;
        }
    }

    public static Version getVersion(DimensionDataStorage dds) {
        return dds.computeIfAbsent(
            new Factory<>(Version::new, Version::of, DataFixTypes.LEVEL),
            Version.filePath());
    }

    public static void clearLegacyFiles() {
        LEGACY_FILES.forEach(f -> f.delete());
        LEGACY_FILES.clear();
    }

    public static void migrate(MinecraftServer server, TrainerManager tm) {
        var dds = server.overworld().getDataStorage();
        var ver = getVersion(dds);
        
        if(ver.isOutdated()) {
            var cache = new HashMap<String, TrainerBattleMemory>();
            var oldPaths = new HashSet<String>();

            tm.getAllData().map(e -> e.getKey()).forEach(tid -> {
                // migration from different version (groups)
                if(ver.groups > 0) {
                    var path = TrainerBattleMemory.filePath(tid, ver);
                    var oldTbm = dds.get(new Factory<>(() -> { throw new IllegalStateException("nope"); }, TrainerBattleMemory::of, DataFixTypes.LEVEL), path);

                    if(oldTbm != null) {
                        oldTbm.defeatedBy.forEach((tid2, defeatedBy) -> {
                            ModCommon.LOG.info(String.format("migrating trainer data: v%02d -> v%02d, %s", ver.groups, GROUPS, tid2));
                            var newTbm = cache.computeIfAbsent(filePath(tid2, GROUPS), t -> new TrainerBattleMemory());
                            newTbm.defeatedBy.put(tid2, defeatedBy);
                            newTbm.setDirty();
                        });

                        oldPaths.add(path);
                        dds.set(path, null);
                    }
                }

                // migration from legacy format
                var path = TrainerBattleMemoryLegacy.filePath(tid);
                var legTbm = dds.get(new Factory<>(() -> { throw new IllegalStateException("nope"); }, TrainerBattleMemoryLegacy::of, DataFixTypes.LEVEL), path);

                if(legTbm != null) {
                    ModCommon.LOG.info(String.format("migrating trainer data: legacy -> v%02d, %s", GROUPS, tid));
                    var newTbm = cache.computeIfAbsent(filePath(tid, GROUPS), t -> new TrainerBattleMemory());
                    newTbm.defeatedBy.put(tid, legTbm.defeatedBy);
                    newTbm.setDirty();
                    oldPaths.add(path);
                    dds.set(path, null);
                }
            });

            cache.forEach((path, tbm) -> dds.set(path, tbm));
            oldPaths.removeAll(cache.keySet());
            oldPaths.forEach(path -> LEGACY_FILES.add(Paths.get(server.getWorldPath(LevelResource.ROOT).toString(), "data", path + ".dat").toFile()));
            ver.update();
        }
    }
}
