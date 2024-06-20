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

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;

public class MoveCloseToTargetGoal extends MoveTowardsTargetGoal {
    private static final int DEFAULT_SOCIAL_DISTANCING = 16;

    private PathfinderMob mob;
    private double minDistanceSquared;

    public MoveCloseToTargetGoal(PathfinderMob pathfinderMob, double d, float f) {
        this(pathfinderMob, d, f, DEFAULT_SOCIAL_DISTANCING);
    }

    public MoveCloseToTargetGoal(PathfinderMob pathfinderMob, double d, float f, float g) {
        super(pathfinderMob, d, f);
        this.mob = pathfinderMob;
        this.minDistanceSquared = g*g;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !this.isNearbyTarget();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && !this.isNearbyTarget();
    }

    private boolean isNearbyTarget() {
        var target = this.mob.getTarget();
        return target != null && target.distanceToSqr(this.mob) < this.minDistanceSquared;
    }
}
