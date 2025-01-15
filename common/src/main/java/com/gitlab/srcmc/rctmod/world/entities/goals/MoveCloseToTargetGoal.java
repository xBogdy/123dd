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

import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;

import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;

public class MoveCloseToTargetGoal extends MoveTowardsTargetGoal {
    private static final int DEFAULT_SOCIAL_DISTANCING = 36;
    private static final int PAUSE_MIN_TICKS = 0;
    private static final int PAUSE_MAX_TICKS = 200;

    private TrainerMob mob;
    private double minDistanceSquared;
    private double speedModifier;
    private float probability;
    private int pauseTicks;

    public MoveCloseToTargetGoal(TrainerMob pathfinderMob, double d, float f) {
        this(pathfinderMob, d, 0.5f, f, DEFAULT_SOCIAL_DISTANCING);
    }

    public MoveCloseToTargetGoal(TrainerMob pathfinderMob, double d, float p, float f) {
        this(pathfinderMob, d, p, f, DEFAULT_SOCIAL_DISTANCING);
    }

    public MoveCloseToTargetGoal(TrainerMob pathfinderMob, double d, float p, float f, float g) {
        super(pathfinderMob, d, f);
        this.mob = pathfinderMob;
        this.probability = p;
        this.minDistanceSquared = g*g;
        this.speedModifier = d;
    }

    @Override
    public boolean canUse() {
        var can = this.mob.getRandom().nextFloat() < this.probability
            && this.mob.isRequiredBy(mob.getTarget())
            && super.canUse() && !this.isNearbyTarget();

        if(can) {
            this.pauseTicks = this.mob.getRandom().nextInt(PAUSE_MIN_TICKS, PAUSE_MAX_TICKS);
            this.probability *= 0.75;
        }
        
        return can;
    }

    @Override
    public boolean canContinueToUse() {
        if(!this.mob.isInWater() && !this.mob.isInLava() && this.mob.getRandom().nextInt(600) == 0) {
            return false;
        }

        if(super.canContinueToUse()) {
            if(!this.isNearbyTarget()) {
                return true;
            }
        }

        return false;
    }

    private boolean isNearbyTarget() {
        var target = this.mob.getTarget();
        return target != null && target.distanceToSqr(this.mob) < this.minDistanceSquared;
    }
    
    @Override
    public void tick() {
        if(this.pauseTicks < 0) {
            this.mob.getNavigation().setSpeedModifier(this.mob.isInWater() || this.mob.isInLava() ? 1 : this.speedModifier);
            super.tick();
        } else {
            this.mob.getNavigation().setSpeedModifier(0);
            this.pauseTicks--;
        }
    }
}
