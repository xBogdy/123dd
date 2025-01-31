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
package com.gitlab.srcmc.rctmod.world.entities.goals;

import java.util.function.Supplier;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;

public class MoveCloseToTargetGoal extends MoveTowardsTargetGoal {
    private static final int DEFAULT_SOCIAL_DISTANCING = 24;

    private PathfinderMob mob;
    private double minDistanceSquared;
    private double speedModifier;
    private Supplier<Float> probability;

    public MoveCloseToTargetGoal(PathfinderMob pathfinderMob, double d, Supplier<Float> p, float f) {
        this(pathfinderMob, d, p, f, DEFAULT_SOCIAL_DISTANCING);
    }

    public MoveCloseToTargetGoal(PathfinderMob pathfinderMob, double d, Supplier<Float> p, float f, float g) {
        super(pathfinderMob, d, f);
        this.mob = pathfinderMob;
        this.probability = p;
        this.minDistanceSquared = g*g;
        this.speedModifier = d;
    }

    @Override
    public boolean canUse() {
        return this.mob.getRandom().nextFloat() < this.probability.get() && super.canUse() && !this.isNearbyTarget();
    }

    @Override
    public boolean canContinueToUse() {
        if(!this.mob.isInWater() && !this.mob.isInLava() && this.mob.getRandom().nextInt(600) == 0) {
            return false;
        }

        return super.canContinueToUse() && !this.isNearbyTarget();
    }

    private boolean isNearbyTarget() {
        var target = this.mob.getTarget();
        return target != null && target.distanceToSqr(this.mob) < this.minDistanceSquared;
    }
    
    @Override
    public void tick() {
        this.mob.getNavigation().setSpeedModifier(this.mob.isInWater() || this.mob.isInLava() ? 1 : this.speedModifier);
        super.tick();
    }
}
