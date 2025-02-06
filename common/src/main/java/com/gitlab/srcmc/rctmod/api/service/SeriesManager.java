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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.data.pack.SeriesMetaData;
import com.gitlab.srcmc.rctmod.api.utils.PathUtils;

public class SeriesManager {
    private Map<String, SeriesData> seriesData = new HashMap<>();

    public Set<String> getSeriesIds() {
        return this.seriesData.keySet();
    }

    public SeriesMetaData getData(String seriesId) {
        return this.seriesData.getOrDefault(seriesId, new SeriesData(seriesId)).metaData;
    }

    public Stream<String> getRequiredDefeats(String seriesId) {
        return this.getRequiredDefeats(seriesId, Set.of());
    }

    public Stream<String> getRequiredDefeats(String seriesId, Set<String> defeatedTrainers) {
        return this.getRequiredDefeats(seriesId, defeatedTrainers, false);
    }

    public Stream<String> getRequiredDefeats(String seriesId, Set<String> defeatedTrainers, boolean includeOptionals) {
        ModCommon.LOG.info("SERIES REQURIED: " + seriesId + ", " + String.valueOf(defeatedTrainers));
        var sd = this.seriesData.getOrDefault(seriesId, new SeriesData(seriesId));
        sd.list.reversed().forEach(tn -> ModCommon.LOG.info(" -> " + tn.trainerId));

        return this.seriesData
            .getOrDefault(seriesId, new SeriesData(seriesId)).list
            .stream().filter(tn -> !tn.isOptional(defeatedTrainers))
            .map(tn -> tn.trainerId);
    }

    void onLoad(TrainerManager tm) {
        var seriesData = new HashMap<String, SeriesData>();

        tm.listSeries((rl, io) -> {
            var sid = PathUtils.filename(rl.getPath());
            
            if(seriesData.put(sid, new SeriesData(tm.loadSeries(sid).get())) != null) {
                ModCommon.LOG.error(String.format("duplicate series '%s'", sid));
            }
        });

        tm.getAllData().forEach(e -> {
            e.getValue().getSeries().forEach(sid -> {
                var sd = seriesData.computeIfAbsent(sid, SeriesData::new);
                sd.map.put(e.getKey(), new TrainerNode(e.getKey(), e.getValue().isOptional()));
            });
        });

        tm.getAllData().forEach(e -> {
            e.getValue().getSeries().forEach(sid -> {
                var sd = seriesData.get(sid);
                var rd = e.getValue().getRequiredDefeats();
                var nd = sd.map.get(e.getKey());

                rd.forEach(tids -> {
                    tids.forEach(tid -> {
                        var other = sd.map.get(tid);

                        if(other != null) {
                            nd.ancestors.add(other);
                            other.successors.add(nd);
                        }

                        tids.stream().filter(tid2 -> tid2 != tid)
                            .map(tid2 -> sd.map.get(tid2))
                            .forEach(other.siblings::add);
                    });
                });

                sd.sortGraph();
            });
        });

        this.seriesData = Collections.unmodifiableMap(seriesData);
    }

    private class SeriesData {
        private SeriesMetaData metaData;
        private Map<String, TrainerNode> map = new HashMap<>();
        private List<TrainerNode> list = List.of();

        SeriesData(String seriesId) {
            this(new SeriesMetaData(seriesId));
        }

        SeriesData(SeriesMetaData metaData) {
            this.metaData = metaData;
        }

        void sortGraph() {
            var list = new ArrayList<TrainerNode>(this.map.values());
            Collections.sort(list);
            this.list = list;
        }
    }

    private class TrainerNode implements Comparable<TrainerNode> {
        private String trainerId;
        private boolean optional;
        private List<TrainerNode> ancestors = new ArrayList<>();
        private List<TrainerNode> siblings = new ArrayList<>();
        private List<TrainerNode> successors = new ArrayList<>();

        private Set<String> prevTrainerIds;
        private boolean cachedOptional;

        public TrainerNode(String trainerId, boolean optional) {
            this.trainerId = trainerId;
            this.optional = optional;
        }

        public boolean isOptional(Set<String> trainerIds) {
            if(trainerIds != this.prevTrainerIds) {
                this.prevTrainerIds = trainerIds;
                this.cachedOptional = this.optional || this.isOptionalFor(trainerIds) || this.successors.stream().anyMatch(successor -> successor.isOptional(trainerIds));
            }

            return this.cachedOptional;
        }

        boolean isOptionalFor(Set<String> trainerIds) {
            for(var sibling : this.siblings) {
                if(trainerIds.contains(sibling.trainerId)) {
                    return true;
                }
            }

            return false;
        }

        boolean isAncestor(TrainerNode other) {
            for(var anc : this.ancestors) {
                if(anc.isAncestor(other)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public int compareTo(TrainerNode other) {
            if(this.isAncestor(other)) {
                return 1;
            }

            if(other.isAncestor(this)) {
                return -1;
            }

            return this.trainerId.compareTo(other.trainerId);
        }
    }
}
