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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.data.pack.SeriesMetaData;
import com.gitlab.srcmc.rctmod.api.utils.PathUtils;

public class SeriesManager implements Serializable {
    private static final long serialVersionUID = 0L;
    public static final String EMPTY_SERIES_ID = "empty";

    private transient final SeriesGraph EMPTY_SERIES = new SeriesGraph(new SeriesMetaData("Empty Series", "", 0));
    private Map<String, SeriesGraph> seriesGraphs = new HashMap<>();

    public void copyFrom(SeriesManager sm) {
        this.seriesGraphs = sm.seriesGraphs;
    }

    public Set<String> getSeriesIds() {
        return this.seriesGraphs.keySet();
    }

    public SeriesGraph getGraph(String seriesId) {
        return seriesId == null || seriesId.isBlank() ? this.EMPTY_SERIES : this.seriesGraphs.getOrDefault(seriesId, new SeriesGraph(seriesId));
    }

    void onLoad(TrainerManager tm) {
        EMPTY_SERIES.clear();

        var seriesGraphs = new HashMap<String, SeriesGraph>();
        seriesGraphs.put(EMPTY_SERIES_ID, EMPTY_SERIES);

        // gather series ids
        tm.listSeries((rl, io) -> {
            var sid = PathUtils.filename(rl.getPath());

            if(sid.equals(EMPTY_SERIES_ID)) {
                throw new IllegalStateException(String.format("series id '%s' already occupied by empty series", EMPTY_SERIES_ID));
            }
            
            if(seriesGraphs.put(sid, new SeriesGraph(tm.loadSeries(sid).get())) != null) {
                ModCommon.LOG.error(String.format("duplicate series '%s'", sid));
            }
        });

        // gather series ids and create trainer nodes
        tm.getAllData().forEach(e -> {
            e.getValue().getSeries().forEach(sid -> {
                var sg = seriesGraphs.computeIfAbsent(sid, SeriesGraph::new);
                sg.map.put(e.getKey(), new TrainerNode(e.getKey(), e.getValue().isOptional()));
            });
        });

        // create nodes for trainers that belong to all series
        tm.getAllData()
            .filter(e -> e.getValue().getSeries().findFirst().isEmpty())
            .forEach(e -> seriesGraphs.values().stream().forEach(sd -> sd.map.put(e.getKey(), new TrainerNode(e.getKey(), e.getValue().isOptional()))));

        // build graph
        tm.getAllData().forEach(e -> {
            var seriesIds = e.getValue().getSeries().findFirst().isPresent()
                ? e.getValue().getSeries()
                : seriesGraphs.keySet().stream();

            seriesIds.forEach(sid -> {
                var sg = seriesGraphs.get(sid);
                var st = e.getValue().getSubstitutes();
                var rd = e.getValue().getRequiredDefeats();
                var nd = sg.map.get(e.getKey());

                st.forEach(tid -> {
                    var other = sg.map.get(tid);

                    if(other != null) {
                        nd.siblings.add(other);
                    }
                });

                rd.forEach(tids -> {
                    tids.forEach(tid -> {
                        var other = sg.map.get(tid);

                        if(other != null) {
                            nd.successors.add(other);
                            other.ancestors.add(nd);
                            tids.stream().filter(tid2 -> tid2 != tid)
                                .map(tid2 -> sg.map.get(tid2))
                                .forEach(other.siblings::add);
                        }
                    });
                });
            });
        });

        this.seriesGraphs = Collections.unmodifiableMap(seriesGraphs);
        this.seriesGraphs.values().forEach(SeriesGraph::sortGraph);
    }

    public class SeriesGraph implements Serializable {
        private static final long serialVersionUID = 0L;

        private Map<String, TrainerNode> map = new HashMap<>();
        private List<TrainerNode> list = new ArrayList<>();            
        private SeriesMetaData metaData;

        SeriesGraph(String seriesId) {
            this(new SeriesMetaData(seriesId));
        }

        SeriesGraph(SeriesMetaData metaData) {
            this.metaData = metaData;
        }

        SeriesGraph(SeriesGraph origin) {
            this.metaData = origin.metaData;
        }

        void clear() {
            this.map = new HashMap<>();
            this.list = new ArrayList<>();
        }

        public SeriesMetaData getMetaData() {
            return this.metaData;
        }

        public Stream<TrainerNode> stream() {
            return this.list.stream();
        }

        public TrainerNode get(String trainerId) {
            return this.map.get(trainerId);
        }

        public boolean contains(String trainerId) {
            return this.map.containsKey(trainerId);
        }

        public int size() {
            return this.list.size();
        }

        public SeriesGraph getNext(Set<String> defeats) {
            return this.getNext(defeats, false, false);
        }

        public SeriesGraph getNext(boolean includeOptionals) {
            return this.getNext(includeOptionals, false);
        }

        public SeriesGraph getNext(boolean includeOptionals, boolean includeSingles) {
            return this.getNext(Set.of(), includeOptionals, includeSingles);
        }

        public SeriesGraph getNext(Set<String> defeats, boolean includeOptionals) {
            return this.getNext(defeats, includeOptionals, false);
        }

        public SeriesGraph getNext(Set<String> defeats, boolean includeOptionals, boolean includeSingles) {
            var copy = new SeriesGraph(this);

            this.list.stream()
                .filter(tn -> (includeSingles || !tn.isAlone())
                    && (includeOptionals || !tn.isOptional()) && !tn.isDefeated(defeats)
                    && tn.successors.stream().allMatch(suc -> suc.isDefeated(defeats)))
                .forEach(tn -> {
                    copy.list.add(tn);
                    copy.map.put(tn.trainerId, tn);
                });

            return copy;
        }

        public SeriesGraph getRemaining(Set<String> defeats) {
            return this.getRemaining(defeats, false);
        }

        public SeriesGraph getRemaining(boolean includeOptionals) {
            return this.getRemaining(includeOptionals, false);
        }

        public SeriesGraph getRemaining(boolean includeOptionals, boolean includeSingles) {
            return this.getRemaining(Set.of(), includeOptionals, includeSingles);
        }

        public SeriesGraph getRemaining(Set<String> defeats, boolean includeOptionals) {
            return this.getRemaining(defeats, includeOptionals, false);
        }

        public SeriesGraph getRemaining(Set<String> defeats, boolean includeOptionals, boolean includeSingles) {
            var copy = new SeriesGraph(this);

            this.list.stream()
                .filter(tn -> (includeSingles || !tn.isAlone())
                    && (includeOptionals || !tn.isOptional()) && !tn.isDefeated(defeats))
                .forEach(tn -> {
                    copy.list.add(tn);
                    copy.map.put(tn.trainerId, tn);
                });

            return copy;
        }

        // topological
        private void sortGraph() {
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
            var sb = new StringBuilder();
            this.list.stream().forEach(tn -> sb.append(" -> ").append(tn.trainerId));
            return sb.toString();
        }
    }

    public class TrainerNode implements Serializable {
        private static final long serialVersionUID = 0L;
        
        private List<TrainerNode> ancestors = new ArrayList<>();
        private List<TrainerNode> successors = new ArrayList<>();
        private List<TrainerNode> siblings = new ArrayList<>();
        private String trainerId;
        private boolean optional;

        public TrainerNode(String trainerId, boolean optional) {
            this.trainerId = trainerId;
            this.optional = optional;
        }

        public String id() {
            return this.trainerId;
        }

        public Stream<TrainerNode> successors() {
            return this.successors.stream();
        }

        public Stream<TrainerNode> ancestors() {
            return this.ancestors.stream();
        }

        public Stream<TrainerNode> siblings() {
            return this.siblings.stream();
        }

        public boolean isAlone() {
            return this.isAlone(new HashSet<>());
        }

        public boolean isOptional() {
            return this.isOptional(new HashSet<>());
        }

        public boolean isDefeated(Set<String> trainerIds) {
            return this.isDefeated(trainerIds, new HashSet<>());
        }

        private boolean isAlone(Set<TrainerNode> visited) {
            visited.add(this);

            return this.ancestors.isEmpty() && this.successors.isEmpty() && this.siblings.stream()
                .filter(sib -> !visited.contains(sib))
                .allMatch(sib -> sib.isAlone(visited));
        }

        private boolean isOptional(Set<TrainerNode> visited) {
            return visited.add(this) && (this.optional
                || this.siblings.stream().anyMatch(sib -> sib.isOptional(visited))
                || this.successors.stream().anyMatch(suc -> suc.isOptional(visited)));
        }

        private boolean isDefeated(Set<String> trainerIds, Set<TrainerNode> visited) {
            return visited.add(this) && (trainerIds.contains(this.trainerId)
                || this.siblings.stream().anyMatch(sib -> sib.isDefeated(trainerIds, visited))
                || this.ancestors.stream().anyMatch(anc -> anc.isDefeated(trainerIds, visited)));
        }
    }
}
