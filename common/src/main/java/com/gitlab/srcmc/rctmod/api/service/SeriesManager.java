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
import java.util.stream.Collectors;
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
        // ModCommon.LOG.info("SERIES TRAINERS: " + seriesId + ", " + String.valueOf(defeatedTrainers));
        // ModCommon.LOG.info("REQUIRED: " + this.seriesData.getOrDefault(seriesId, new SeriesData(seriesId)).toString(defeatedTrainers));

        return this.seriesData
            .getOrDefault(seriesId, new SeriesData(seriesId)).list.stream()
            .filter(tn -> !defeatedTrainers.contains(tn.trainerId))
            .filter(tn -> !tn.isOptional(defeatedTrainers))
            .map(tn -> tn.trainerId);
    }

    void onLoad(TrainerManager tm) {
        var seriesData = new HashMap<String, SeriesData>();

        // gather series ids
        tm.listSeries((rl, io) -> {
            var sid = PathUtils.filename(rl.getPath());
            
            if(seriesData.put(sid, new SeriesData(tm.loadSeries(sid).get())) != null) {
                ModCommon.LOG.error(String.format("duplicate series '%s'", sid));
            }
        });

        // gather series ids and create trainer nodes
        tm.getAllData().forEach(e -> {
            e.getValue().getSeries().forEach(sid -> {
                var sd = seriesData.computeIfAbsent(sid, SeriesData::new);
                sd.map.put(e.getKey(), new TrainerNode(e.getKey(), e.getValue().isOptional()));
            });
        });

        // create nodes for trainers that belong to all series
        tm.getAllData()
            .filter(e -> e.getValue().getSeries().findFirst().isEmpty())
            .forEach(e -> seriesData.values().forEach(sd -> sd.map.put(e.getKey(), new TrainerNode(e.getKey(), e.getValue().isOptional()))));

        // build graph
        tm.getAllData().forEach(e -> {
            var seriesIds = e.getValue().getSeries().findFirst().isPresent()
                ? e.getValue().getSeries()
                : seriesData.keySet().stream();

            seriesIds.forEach(sid -> {
                var sd = seriesData.get(sid);
                var rd = e.getValue().getRequiredDefeats();
                var nd = sd.map.get(e.getKey());

                rd.forEach(tids -> {
                    tids.forEach(tid -> {
                        var other = sd.map.get(tid);

                        if(other != null) {
                            nd.ancestors.add(other);
                            other.successors.add(nd);
                            tids.stream().filter(tid2 -> tid2 != tid)
                                .map(tid2 -> sd.map.get(tid2))
                                .forEach(other.siblings::add);
                        }
                    });
                });
            });
        });

        this.seriesData = Collections.unmodifiableMap(seriesData);
        this.seriesData.values().forEach(SeriesData::sortGraph);
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
            var list = List.copyOf(this.map.values());
            var succ = new HashMap<TrainerNode, Integer>();

            // get rid of trainers that don't have any edges
            var alone = list.stream()
                .filter(TrainerNode::isAlone)
                .sorted((tn1, tn2) -> tn1.trainerId.compareTo(tn2.trainerId)).toList();

            list = list.stream()
                .filter(tn -> !tn.isAlone())
                .collect(Collectors.toList());

            // initialize edge counts
            list.stream()
                .filter(tn -> tn.ancestors.isEmpty())
                .forEach(tn -> this.initCount(tn, succ));

            var sorted = new ArrayList<TrainerNode>();
            var batch = new ArrayList<TrainerNode>();
            
            while(list.size() > 0) {
                var it = list.iterator();

                while(it.hasNext()) {
                    var next = it.next();

                    if(succ.compute(next, (k, v) -> v - 1) < 0) {
                        batch.add(next);
                        it.remove();
                    }
                }

                batch.sort((tn1, tn2) -> tn1.trainerId.compareTo(tn2.trainerId));
                sorted.addAll(batch);
                batch.clear();
            }

            list.addAll(sorted);
            list.addAll(alone);
            this.list = List.copyOf(list);
        }

        private void initCount(TrainerNode start, Map<TrainerNode, Integer> succ) {
            var c = succ.computeIfAbsent(start, k -> 0);

            start.successors.forEach(stn -> {
                succ.compute(stn, (k, v) -> v == null ? c + 1 : Math.max(c + 1, v));
                this.initCount(stn, succ);
            });
        }

        @Override
        public String toString() {
            return this.toString(Set.of());
        }

        public String toString(Set<String> trainerIds) {
            return this.toString(trainerIds, false);
        }

        public String toString(Set<String> trainerIds, boolean includeOptionals) {
            var sb = new StringBuilder();

            this.list.stream()
                .filter(tn -> !trainerIds.contains(tn.trainerId))
                .filter(tn -> includeOptionals || !tn.isOptional(trainerIds))
                .forEach(tn -> sb.append(" -> ").append(tn.trainerId));

            return sb.toString();
        }
    }

    private class TrainerNode {
        private String trainerId;
        private boolean optional;
        private List<TrainerNode> ancestors = new ArrayList<>();
        private List<TrainerNode> siblings = new ArrayList<>();
        private List<TrainerNode> successors = new ArrayList<>();

        public TrainerNode(String trainerId, boolean optional) {
            this.trainerId = trainerId;
            this.optional = optional;
        }

        public boolean isAlone() {
            return this.ancestors.isEmpty() && this.successors.isEmpty();
        }

        public boolean isOptional(Set<String> trainerIds) {
            return this.optional
                || this.isAlone()
                || this.isOptionalFor(trainerIds)
                || this.successors.stream().anyMatch(successor -> successor.isOptional(trainerIds));
        }

        boolean isOptionalFor(Set<String> trainerIds) {
            for(var sibling : this.siblings) {
                if(trainerIds.contains(sibling.trainerId)) {
                    return true;
                }
            }

            return false;
        }

        // boolean isAncestor(TrainerNode other) {
        //     if(other.ancestors.contains(this)) {
        //         return true;
        //     }

        //     for(var anc : other.ancestors) {
        //         if(this.isAncestor(anc)) {
        //             return true;
        //         }
        //     }

        //     return false;
        // }
    }
}
