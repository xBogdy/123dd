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

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStrollAwayGoal extends RandomStrollGoal {
    private static final float STORLL_CHANCE = 0.0015f;

    private int[] direction;
    private Supplier<Float> probability;
    private Function<PathfinderMob, Boolean> predicate;

    public RandomStrollAwayGoal(PathfinderMob pathfinderMob, double d, Supplier<Float> p) {
        this(pathfinderMob, d, p, m -> true);
    }

    public RandomStrollAwayGoal(PathfinderMob pathfinderMob, double d, Supplier<Float> p, Function<PathfinderMob, Boolean> predicate) {
        super(pathfinderMob, d);
        this.probability = p;
        this.predicate = predicate;

        switch(pathfinderMob.getRandom().nextInt(4)) {
            case 0:
                direction = new int[]{100, 0}; // north
                break;
            case 1:
                direction = new int[]{100, 100}; // north east
                break;
            case 2:
                direction = new int[]{0, 100}; // east
                break;
            case 3:
                direction = new int[]{-100, 100}; // south east
                break;
            case 4:
                direction = new int[]{-100, 0}; // south
                break;
            case 5:
                direction = new int[]{-100, -100}; // south west
                break;
            case 6:
                direction = new int[]{0, -100}; // west
                break;
            case 7:
                direction = new int[]{100, -100}; // north west
                break;
        }
    }

    @Override
    public boolean canUse() {
        return this.mob.getRandom().nextFloat() < this.probability.get() && this.predicate.apply(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        if(!this.mob.isInWater() && !this.mob.isInLava() && this.mob.getRandom().nextInt(600) == 0) {
            return false;
        }

        return super.canContinueToUse();
    }

    protected Vec3 getPosition() {
        if(this.mob.isInWaterOrBubble() || this.mob.isInLava()) {
            Vec3 vec3 = LandRandomPos.getPosTowards(this.mob, 15, 7, this.mob.getPosition(1F).add(this.direction[0], 0, this.direction[1]));
            return vec3 == null ? super.getPosition() : vec3;
        } else {
            return this.mob.getRandom().nextFloat() > STORLL_CHANCE
                ? LandRandomPos.getPosTowards(this.mob, 10, 7, this.mob.getPosition(1F).add(this.direction[0], 0, this.direction[1]))
                : super.getPosition();
        }
    }
    
    @Override
    public void tick() {
        this.mob.getNavigation().setSpeedModifier(this.mob.isInWater() || this.mob.isInLava() ? 1 : this.speedModifier);
        super.tick();
    }
}
