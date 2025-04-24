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
package com.gitlab.srcmc.rctmod.world.blocks.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.service.TrainerManager;
import com.gitlab.srcmc.rctmod.world.blocks.TrainerSpawnerBlock;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class TrainerSpawnerBlockEntity extends BlockEntity {
    private static final int OWNER_UPDATE_INTERVAL_TICKS = 40;
    private static final int SPAWN_INTERVAL_TICKS = 80;
    private static final int SCAN_INTERVAL_TICKS = 200;
    private static final double HOME_SWITCH_CHANCE = 0.1;

    public final RenderState renderState = new RenderState();
    private Set<String> trainerIds = new HashSet<>();
    private TrainerMob ownerTrainer;
    private UUID ownerUUID;

    private double minPlayerDistance;
    private double maxPlayerDistance;

    private Queue<Consumer<TrainerManager>> updateQueue = new LinkedList<>();
    private Set<Item> renderItems = new HashSet<>();
    private Timer ownerUpdateTimer = new Timer();
    private Timer spawnTimer = new Timer();
    private Timer scanTimer = new Timer();
    private AABB aabb;

    public TrainerSpawnerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModRegistries.BlockEntityTypes.TRAINER_SPAWNER.get(), blockPos, blockState);
        this.setPlayerDistanceThreshold(2, RCTMod.getInstance().getServerConfig().maxHorizontalDistanceToPlayers()*(2/3.0));
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

        if(this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }

        var tids = new CompoundTag();
        this.trainerIds.forEach(tid -> tids.putByte(tid, (byte)0));
        tag.put("TrainerIds", tids);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.trainerIds.clear();
        this.ownerUUID = tag.contains("OwnerUUID") ? tag.getUUID("OwnerUUID") : null;

        // pre 0.15
        if(tag.contains("renderItemKey")) {
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("renderItemKey")));
            this.addTrainerIdsFromItem(item.getDefaultInstance());
        }

        if(tag.contains("TrainerIds")) {
            this.trainerIds.addAll(tag.getCompound("TrainerIds").getAllKeys());
        }

        this.updateOwner();
        this.updateRenderItems();
    }

    private void update() {
        var tm = RCTMod.getInstance().getTrainerManager();

        if(!tm.isLoading()) {
            while(!this.updateQueue.isEmpty()) {
                this.updateQueue.poll().accept(tm);
            }
        }
    }

    private void updateRenderItems() {
        this.updateQueue.add(tm -> {
            this.renderItems.clear();

            this.trainerIds.forEach(tid -> {
                var renderItemKey = tm.getData(tid).getSignatureItem();

                if(renderItemKey != null && !renderItemKey.isBlank()) {
                    var rl = ResourceLocation.parse(renderItemKey);

                    if(!BuiltInRegistries.ITEM.containsKey(rl)) {
                        ModCommon.LOG.error(String.format("Invalid Trainer Spawner item for '%s': %s", tid, rl.toString()));
                    } else {
                        var item = BuiltInRegistries.ITEM.get(rl);
                        this.renderItems.add(item);
                    }
                }
            });
        });
    }

    private void syncToClients() {
        if(this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public void setOwner(TrainerMob ownerTrainer) {
        if(ownerTrainer != this.ownerTrainer) {
            if(this.ownerTrainer != null) {
                this.ownerTrainer.setHomeSpawner(null);
            }

            this.ownerTrainer = ownerTrainer;

            if(this.ownerTrainer != null) {
                var oldSpawner = this.ownerTrainer.getHomeSpawner();

                if(oldSpawner != this && oldSpawner != null) {
                    oldSpawner.setOwner(null);
                }

                this.ownerTrainer.setHomeSpawner(this);
                this.setOwnerUUID(this.ownerTrainer.getUUID());
            }
        }

        if(this.ownerTrainer == null) {
            this.setOwnerUUID(null);
        }
    }

    protected void setOwnerUUID(UUID ownerUUID) {
        if(!Objects.equals(this.ownerUUID, ownerUUID)) {
            this.ownerUUID = ownerUUID;
            this.setChanged();
            this.syncToClients();
        }
    }

    public TrainerMob getOwner() {
        return this.ownerTrainer;
    }

    protected void updateOwner() {
        if(this.ownerUUID != null) {
            if(this.ownerTrainer == null) {
                RCTMod.getInstance().getTrainerSpawner().getSpawns().stream()
                    .filter(tm -> tm.getUUID().equals(this.ownerUUID))
                    .findAny().ifPresent(tm -> this.ownerTrainer = tm);
            }
            
            if(this.ownerTrainer == null || !this.ownerTrainer.isAlive()) {
                this.setOwner(null);
            } else {
                this.setOwner(this.ownerTrainer);
            }
        }
    }

    private void addTrainerIdsFromItem(ItemStack item) {
        this.updateQueue.add(tm -> this.addTrainerIdsFromItem(tm, item));
    }

    public boolean addTrainerIdsFromItem(TrainerManager tm, ItemStack item) {
        var added = new boolean[]{false};

        if(!tm.isLoading()) {
            var itemId = item.getItem().arch$registryName().toString();

            tm.getAllData()
                .filter(e -> itemId.equals(e.getValue().getSignatureItem()))
                .forEach(e -> {
                    if(this.trainerIds.add(e.getKey())) {
                        added[0] = true;
                    }
                });

            if(added[0]) {
                this.setChanged();
                this.syncToClients();
            }
        }

        return added[0];
    }

    public Set<String> getTrainerIds() {
        return this.trainerIds;
    }

    public Set<Item> getRenderItems() {
        return this.renderItems;
    }

    public double getMinPlayerDistance() {
        return this.minPlayerDistance;
    }

    public double getMaxPlayerDistance() {
        return this.maxPlayerDistance;
    }

    protected void setPlayerDistanceThreshold(double min, double max) {
        this.minPlayerDistance = Math.min(min, max);
        this.maxPlayerDistance = max;
        this.aabb = new AABB(this.getBlockPos()).inflate(max);
    }

    private void attemptSpawn(Level level, BlockPos blockPos, BlockState blockState) {
        var pos = blockPos.getCenter();
        var nearest = level.getNearestPlayer(TargetingConditions.forNonCombat(), null, pos.x, pos.y, pos.z);

        if(nearest == null || nearest.distanceToSqr(pos) < Math.pow(this.minPlayerDistance, 2)/2) { // TODO: why divided by 2 again?
            return;
        }

        var guaruantee = TrainerSpawnerBlock.isPowered(blockState); /* && RCTMod.getInstance().getServerConfig().enableTrainerSpawnerRedstone() */
        var spawner = RCTMod.getInstance().getTrainerSpawner();
        var trainerIds = new ArrayList<>(this.getTrainerIds());
        Collections.shuffle(trainerIds);

        for(var player : level.getNearbyPlayers(TargetingConditions.forNonCombat(), null, this.aabb)) {
            for(var trainerId : trainerIds) {
                var m = spawner.attemptSpawnFor(player, trainerId, this.getBlockPos().above(), true, true, guaruantee, 1.0, 1.0);

                if(m != null) {
                    this.setOwner(m);
                    return;
                }
            }
        }
    }

    private void scanForTrainerNearby(Level level) {
        var trainerIds = this.getTrainerIds();

        level.getNearbyEntities(
                TrainerMob.class,
                TargetingConditions.forNonCombat(),
                null, this.aabb).stream()
            .filter(t -> trainerIds.contains(t.getTrainerId()))
            .filter(t -> t.getHomePos() == null || t.getRandom().nextDouble() < HOME_SWITCH_CHANCE)
            .findAny().ifPresent(t -> this.setOwner(t));
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, TrainerSpawnerBlockEntity be) {
        be.update();

        if(be.ownerUpdateTimer.passed(level.getGameTime()) >= OWNER_UPDATE_INTERVAL_TICKS) {
            be.updateOwner();
            be.ownerUpdateTimer.reset(level.getGameTime());
        }

        if(be.getTrainerIds().size() > 0) {
            if(be.ownerUUID == null && be.spawnTimer.passed(level.getGameTime()) >= SPAWN_INTERVAL_TICKS) {
                be.attemptSpawn(level, blockPos, blockState);
                be.spawnTimer.reset(level.getGameTime());
            }

            if(be.ownerUUID == null && be.scanTimer.passed(level.getGameTime()) >= SCAN_INTERVAL_TICKS) {
                be.scanForTrainerNearby(level);
                be.scanTimer.reset(level.getGameTime());
            }
        }
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TrainerSpawnerBlockEntity be) {
        be.update();
    }

    public class RenderState {
        public double p, targetP;
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
