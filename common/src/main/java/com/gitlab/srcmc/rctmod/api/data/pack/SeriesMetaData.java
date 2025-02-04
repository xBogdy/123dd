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

import java.util.List;

public record SeriesMetaData(String title, String description, int difficulty, List<List<String>> requiredSeries) implements Comparable<SeriesMetaData> {
    public static final int MIN_DIFFICULTY = 1;
    public static final int MAX_DIFFICULTY = 10;

    public SeriesMetaData(String title) {
        this(title, "", MIN_DIFFICULTY + (MAX_DIFFICULTY - MIN_DIFFICULTY) / 2);
    }

    public SeriesMetaData(String title, String description) {
        this(title, description, MIN_DIFFICULTY + (MAX_DIFFICULTY - MIN_DIFFICULTY) / 2);
    }

    public SeriesMetaData(String title, String description, int difficulty) {
        this(title, description, difficulty, List.of());
    }

    public SeriesMetaData(String title, String description, int difficulty, List<List<String>> requiredSeries) {
        this.title = title;
        this.description = description;
        this.difficulty = Math.max(MIN_DIFFICULTY, Math.min(MAX_DIFFICULTY, difficulty));
        this.requiredSeries = requiredSeries;
    }

    @Override
    public int compareTo(SeriesMetaData other) {
        var c = Integer.compare(this.difficulty, other.difficulty);
        return c == 0 ? this.title.compareTo(other.title) : c;
    }
}
