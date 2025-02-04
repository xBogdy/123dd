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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.data.pack.SeriesMetaData;
import com.gitlab.srcmc.rctmod.api.utils.PathUtils;

public class SeriesManager {
    private Map<String, SeriesMetaData> metaData = new HashMap<>();
    private Set<String> seriesIds = new HashSet<>();

    public Set<String> getSeriesIds() {
        return this.seriesIds;
    }

    public SeriesMetaData getData(String seriesId) {
        return this.metaData.getOrDefault(seriesId, new SeriesMetaData(seriesId));
    }

    void onLoad(TrainerManager tm) {
        var metaData = new HashMap<String, SeriesMetaData>();
        var seriesIds = new HashSet<String>();

        tm.listSeries((rl, io) -> {
            var seriesId = PathUtils.filename(rl.getPath());
            
            if(seriesIds.add(seriesId)) {
                metaData.put(seriesId, tm.loadSeries(seriesId).get());
            } else {
                ModCommon.LOG.error(String.format("duplicate series '%s'", rl.toString()));
            }
        });

        tm.getAllData().forEach(e -> e.getValue().getSeries().forEach(seriesIds::add));
        this.metaData = Collections.unmodifiableMap(metaData);
        this.seriesIds = Collections.unmodifiableSet(seriesIds);
    }
}
