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

public class LookAtPlayerAndWaitGoal extends LookAtPlayerGoal {
    private int minLookTime, maxLookTime, lookTime;

    public LookAtPlayerAndWaitGoal(Mob mob, Class<? extends LivingEntity> class_, float distance) {
        this(mob, class_, distance, 0.02F, 80, 160);
    }

    public LookAtPlayerAndWaitGoal(Mob mob, Class<? extends LivingEntity> class_, float distance, float propability, int minLookTime, int maxLookTime) {
        super(mob, class_, distance, propability);
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        this.minLookTime = minLookTime;
        this.maxLookTime = maxLookTime;
    }

    @Override
    public void start() {
        this.lookTime = this.adjustedTickDelay(this.minLookTime + this.mob.getRandom().nextInt(this.maxLookTime - this.minLookTime));
        this.mob.getNavigation().stop();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.lookAt.isAlive()) {
            return false;
        } else if (this.mob.distanceToSqr(this.lookAt) > (double) 2*(this.lookDistance*this.lookDistance)) {
            return false;
        } else {
            return this.lookTime > 0;
        }
    }

    @Override
    public void tick() {
        if (this.lookAt.isAlive()) {
            var d = this.lookAt.getEyeY();
            this.mob.getLookControl().setLookAt(this.lookAt.getX(), d, this.lookAt.getZ());
            this.lookTime--;
        }
    }
}
