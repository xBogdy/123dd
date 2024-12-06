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
package com.gitlab.srcmc.rctmod.world.blocks.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class TrainerSpawnerBlockEntity extends BlockEntity {
    private static final int SPAWN_INTERVAL_TICKS = 80;
    private static final int SCAN_INTERVAL_TICKS = 200;
    
    private Set<String> trainerIds = new HashSet<>();
    private String renderItemKey;
    private double minPlayerDistance;
    private double maxPlayerDistance;

    private Timer spawnTimer = new Timer();
    private Timer scanTimer = new Timer();
    private Item renderItem;
    private AABB aabb;

    public TrainerSpawnerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModRegistries.BlockEntityTypes.TRAINER_SPAWNER.get(), blockPos, blockState);

        this.setPlayerDistanceThreshold(
            RCTMod.getInstance().getServerConfig().minHorizontalDistanceToPlayers()*(2/3.0),
            RCTMod.getInstance().getServerConfig().maxHorizontalDistanceToPlayers()*(2/3.0));
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        var tag = super.getUpdateTag(provider);
        this.saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        byte b = 0;
        this.trainerIds.forEach(tid -> tag.putByte('_' + tid, b));

        if(this.renderItemKey != null) {
            tag.putString("renderItemKey", this.renderItemKey);
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.trainerIds.clear();
        tag.getAllKeys().stream().filter(key -> key.charAt(0) == '_').map(key -> key.substring(1)).forEach(this.trainerIds::add);

        if(tag.contains("renderItemKey")) {
            this.setRenderItemKey(tag.getString("renderItemKey"));
        } else {
            this.setRenderItemKey(null);
        }
    }

    public void setTrainerIds(String renderItemKey, List<String> trainerIds) {
        this.setRenderItemKey(renderItemKey);
        this.trainerIds.clear();
        this.trainerIds.addAll(trainerIds);
        this.spawnTimer.reset(this.level.getGameTime());
        this.scanTimer.reset(this.level.getGameTime());
        this.setChanged();

        if(!this.level.isClientSide) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public Set<String> getTrainerIds() {
        return Collections.unmodifiableSet(this.trainerIds);
    }

    public double getMinPlayerDistance() {
        return this.minPlayerDistance;
    }

    public double getMaxPlayerDistance() {
        return this.maxPlayerDistance;
    }

    public String getRenderItemKey() {
        return this.renderItemKey;
    }

    public Item getRenderItem() {
        return this.renderItem;
    }

    protected void setPlayerDistanceThreshold(double min, double max) {
        this.minPlayerDistance = min;
        this.maxPlayerDistance = max;
        this.aabb = new AABB(this.getBlockPos()).inflate(max);
    }

    protected void setRenderItemKey(String renderItemKey) {
        if(renderItemKey != null) {
            var rl = ResourceLocation.parse(renderItemKey);

            if(!BuiltInRegistries.ITEM.containsKey(rl)) {
                ModCommon.LOG.info("Invalid render item for Trainer Spawner: " + rl.toString());
                this.renderItem = null;
            } else {
                this.renderItem = BuiltInRegistries.ITEM.get(rl);
            }
        }

        this.renderItemKey = renderItemKey;
    }

    private void attemptSpawn(Level level, BlockPos blockPos) {
        var pos = blockPos.getCenter();
        var nearest = level.getNearestPlayer(TargetingConditions.forNonCombat(), null, pos.x, pos.y, pos.z);

        if(nearest == null || nearest.distanceToSqr(pos) < Math.pow(this.minPlayerDistance, 2)/2) {
            return;
        }

        var spawner = RCTMod.getInstance().getTrainerSpawner();
        var trainerIds = new ArrayList<>(this.trainerIds);
        Collections.shuffle(trainerIds);

        for(var player : level.getNearbyPlayers(TargetingConditions.forNonCombat(), null, this.aabb)) {
            for(var trainerId : trainerIds) {
                if(spawner.attemptSpawnFor(player, trainerId, this.getBlockPos().above(), true)) {
                    return;
                }
            }
        }
    }

    private void scanForTrainerNearby(Level level) {
        level.getNearbyEntities(
                TrainerMob.class,
                TargetingConditions.forNonCombat(),
                null, this.aabb)
            .stream().filter(t -> this.trainerIds.contains(t.getTrainerId()))
            .forEach(t -> t.setHomePos(this.getBlockPos().above()));
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, TrainerSpawnerBlockEntity be) {
        if(be.getRenderItemKey() != null) {
            if(be.spawnTimer.passed(level.getGameTime()) >= SPAWN_INTERVAL_TICKS) {
                be.attemptSpawn(level, blockPos);
                be.spawnTimer.reset(level.getGameTime());
            }

            if(be.scanTimer.passed(level.getGameTime()) >= SCAN_INTERVAL_TICKS) {
                be.scanForTrainerNearby(level);
                be.scanTimer.reset(level.getGameTime());
            }
        }
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TrainerSpawnerBlockEntity be) {
    }

    class Timer {
        private long prev, total;

        public void reset(long now) {
            this.prev = now;
            this.total = 0;
        }

        public long passed(long now) {
            this.total += (now - this.prev);
            this.prev = now;
            return this.total;
        }
    }
}
