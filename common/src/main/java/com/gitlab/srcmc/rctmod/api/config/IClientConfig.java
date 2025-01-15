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
package com.gitlab.srcmc.rctmod.api.config;

public interface IClientConfig extends IModConfig {
    /**
     * Determines if symbols for trainer types are shown next to trainer names.
     * 
     * range [true or false]
     * default false
     */
    default boolean showTrainerTypeSymbols() { return false; }

    /**
     * Determines if trainer names are colored based of their type.
     * 
     * range [true or false]
     * default true
     */
    default boolean showTrainerTypeColors() { return true; }

    /**
     * Padding of the trainer card gui.
     * 
     * default 8
     */
    default int trainerCardPadding() { return 8; }

    /**
     * Horizontal alignment of the trainer card gui, i.e. 0=left, 0.5=center, 1=right.
     * 
     * range [0, 1]
     * default 0
     */
    default float trainerCardAlignmentX() { return 0; }

    /**
     * Vertical alignment of the trainer card gui, i.e. 0=top, 0.5=center, 1=bottom.
     * 
     * range [0, 1]
     * default 0
     */
    default float trainerCardAlignmentY() { return 0; }
}
