/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2024, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctmod.world.entities.goals;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

// TODO: deprecated
public class LookAtPlayerAndWaitGoal extends LookAtPlayerGoal {
    public LookAtPlayerAndWaitGoal(Mob mob, Class<? extends LivingEntity> class_, float f) {
        this(mob, class_, f, 1F);
    }

    public LookAtPlayerAndWaitGoal(Mob mob, Class<? extends LivingEntity> class_, float f, float g) {
        this(mob, class_, f, g, false);
    }

    public LookAtPlayerAndWaitGoal(Mob mob, Class<? extends LivingEntity> class_, float f, float g, boolean bl) {
        super(mob, class_, f, g, bl);
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }
}
