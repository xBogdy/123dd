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

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.phys.Vec3;

public class RandomStrollThroughVillageGoal extends RandomStrollGoal {
    private static final int SECTION_SCAN_RADIUS = 6;
    private static final int MAX_IDLE_TIME = 400;
    private static final int MIN_IDLE_TIME = 20;
    private static final int MAX_STROLL_TIME = 800;
    private static final int MIN_STROLL_TIME = 100;

    private PathfinderMob mob;
    private BlockPos target;
    private int idleTime, idleTimer;
    private int strollTime, strollTimer;
    private int prevMobTickCount;
    private SectionPos prevSection;
    private Supplier<Float> probability;

    public RandomStrollThroughVillageGoal(PathfinderMob pathfinderMob, float d, Supplier<Float> p) {
        super(pathfinderMob, d, 1, false);
        this.mob = pathfinderMob;
        this.probability = p;
    }

    @Override
    public void start() {
        var targetV = this.target.getCenter();
        this.idleTime = this.mob.getRandom().nextInt(MIN_IDLE_TIME, MAX_IDLE_TIME);
        this.strollTime = this.mob.getRandom().nextInt(MIN_STROLL_TIME, MAX_STROLL_TIME);
        this.idleTimer = 0;
        this.strollTimer = 0;
        this.prevMobTickCount = this.mob.tickCount;
        this.mob.getNavigation().moveTo(targetV.x, targetV.y, targetV.z, this.speedModifier);
    }

    @Override
    public boolean canUse() {
        this.trigger(); // prevents cancel due to 'interval'
        return this.mob.getRandom().nextFloat() < this.probability.get() && this.tryTargetInRandomSection() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if(!this.mob.isInWater() && !this.mob.isInLava() && this.mob.getRandom().nextInt(600) == 0) {
            return false;
        }

        if(super.canContinueToUse()) {
            if(!this.mob.isInWater() && !this.mob.isInLava()) {
                if(this.strollTimer < this.strollTime) {
                    this.strollTimer += this.mob.tickCount - this.prevMobTickCount;
                } else {
                    this.mob.getNavigation().stop();
                }
            }
        } else {
            if(!this.mob.isInWater() && !this.mob.isInLava()) {
                if(this.idleTimer < this.idleTime) {
                    this.idleTimer += this.mob.tickCount - this.prevMobTickCount;
                    this.prevMobTickCount = this.mob.tickCount;
                    return true;
                }
            }

            if(this.tryTargetInRandomSection()) {
                this.start();
                return true;
            }


            return false;
        }

        this.prevMobTickCount = this.mob.tickCount;
        return true;
    }

    @Override
    protected Vec3 getPosition() {
        return this.target != null ? target.getCenter() : null;
    }

    private boolean tryTargetInRandomSection() {
        this.target = this.getRandomPosInVillage();
        return this.target != null;
    }

    // See GolemRandomStrollInVillageGoal
    private BlockPos getRandomPosInVillage() {
        var sectionPos = this.getRandomVillageSection();

        if(sectionPos == null) {
            return null;
        } else {
            var rng = this.mob.getRandom();

            if(rng.nextFloat() < 0.15) {
                var targetPoi = this.getRandomPoiWithinSection(sectionPos);

                if(targetPoi != null) {
                    return this.toGroundPos(targetPoi.west(rng.nextInt(-8, 8)).east(rng.nextInt(-8, 8)));
                }
            }

            return this.toGroundPos(this.getRandomPosWithinSection(sectionPos));
        }
    }

    private SectionPos getRandomVillageSection() {
        if(this.prevSection == null || this.mob.getRandom().nextFloat() < 0.15) {
            ServerLevel serverLevel = (ServerLevel)this.mob.level();

            var list = (List<SectionPos>)SectionPos
                .cube(SectionPos.of(this.mob), SECTION_SCAN_RADIUS)
                .filter(serverLevel::isVillage)
                .collect(Collectors.toList());

            this.prevSection = list.isEmpty() ? null : (SectionPos)list.get(this.mob.getRandom().nextInt(list.size()));
        }

        return this.prevSection;
    }

    private BlockPos getRandomPoiWithinSection(SectionPos sectionPos) {
        ServerLevel serverLevel = (ServerLevel)this.mob.level();
        PoiManager poiManager = serverLevel.getPoiManager();

        var list = (List<BlockPos>)poiManager
            .getInRange((holder) -> true, sectionPos.center(), 8, Occupancy.ANY)
            .map(PoiRecord::getPos).collect(Collectors.toList());

        return list.isEmpty() ? null : list.get(this.mob.getRandom().nextInt(list.size()));
    }

    private BlockPos getRandomPosWithinSection(SectionPos sectionPos) {
        var rng = this.mob.getRandom();
        return sectionPos.center().east(rng.nextInt(-8, 8)).west(rng.nextInt(-8, 8)).atY(this.mob.getBlockY());
    }

    private BlockPos toGroundPos(BlockPos pos) {
        var level = this.mob.level();
        var pUp = pos;
        var pDn = pos.below();
        BlockState bs;

        while(pUp.getY() - pos.getY() < 24 && pos.getY() - pDn.getY() < 24
        && pUp.getY() <= level.getMaxBuildHeight() && pDn.getY() >= level.getMinBuildHeight()) {
            bs = level.getBlockState(pUp);

            if(bs.entityCanStandOn(level, pUp, this.mob)) {
                return pUp;
            }
            
            bs = level.getBlockState(pDn);

            if(bs.entityCanStandOn(level, pDn, this.mob)) {
                return pDn;
            }

            pUp = pUp.above();
            pDn = pDn.below();
        }

        return pos;
    }

    @Override
    public void tick() {
        this.mob.getNavigation().setSpeedModifier(this.mob.isInWater() || this.mob.isInLava() ? 1 : this.speedModifier);
        super.tick();
    }
}
