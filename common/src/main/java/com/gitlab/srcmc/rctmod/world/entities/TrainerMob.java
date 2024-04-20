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
package com.gitlab.srcmc.rctmod.world.entities;

import java.util.UUID;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData.Type;
import com.gitlab.srcmc.rctmod.api.utils.ChatUtils;
import com.gitlab.srcmc.rctmod.world.entities.goals.LookAtPlayerAndWaitGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.PokemonBattleGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class TrainerMob extends PathfinderMob implements Npc {
    private static final int DISCARD_DELAY = 100;
    private static final EntityType<TrainerMob> TYPE = EntityType.Builder
        .of(TrainerMob::new, MobCategory.MISC)
        .canSpawnFarFromPlayer()
        .sized(0.6F, 1.95F).build("trainer");

    private String trainerId = "";
    private int despawnDelay, discardDelay, cooldown, wins, defeats;
    private BlockPos wanderTarget;
    private Player opponent;
    private UUID originPlayer;

    protected TrainerMob(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        var config = RCTMod.get().getServerConfig();
        this.despawnDelay = config.despawnDelayTicks();
        this.discardDelay = DISCARD_DELAY;
        this.udpateCustomName();
    }

    public static EntityType<TrainerMob> getEntityType() {
        return TYPE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 48.0);
    }

    public boolean canBattleAgainst(Entity e) {
        if(e instanceof Player player) {
            var trPlayer = RCTMod.get().getTrainerManager().getData(player);
            var trMob = RCTMod.get().getTrainerManager().getData(this);

            if(trPlayer.getLevelCap() < trMob.getRequiredLevelCap()) {
                return false;
            }

            for(var type : Type.values()) {
                if(trPlayer.getDefeats(type) < trMob.getRequiredDefeats(type)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public void startBattleWith(Player player) {
        if(this.canBattleAgainst(player)) {
            if(ChatUtils.makebattle(this, player)) {
                RCTMod.get().getTrainerManager().addBattle(player, this);
                ChatUtils.reply(this, player, "battle_start");
                this.setOpponent(player);
            }
        } else {
            this.replyTo(player);
        }
    }

    protected void replyTo(Player player) {
        var trPlayer = RCTMod.get().getTrainerManager().getData(player);
        var trMob = RCTMod.get().getTrainerManager().getData(this);

        if(trPlayer.getDefeats(Type.LEADER) < trMob.getRequiredDefeats(Type.LEADER)) {
            ChatUtils.reply(this, player, "missing_badges");
        } else if(trPlayer.getDefeats(Type.E4) < trMob.getRequiredDefeats(Type.E4)) {
            ChatUtils.reply(this, player, "missing_beaten_e4");
        } else if(trPlayer.getDefeats(Type.CHAMP) < trMob.getRequiredDefeats(Type.CHAMP)) {
            ChatUtils.reply(this, player, "missing_beaten_champs");
        } else if(trPlayer.getLevelCap() < trMob.getTeam().getMembers().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0)) {
            ChatUtils.reply(this, player, "low_level_cap");
        }
    }

    protected void setOpponent(Player player) {
        this.opponent = player;
    }

    public Player getOpponent() {
        return this.opponent;
    }

    public boolean isInBattle() {
        return opponent != null;
    }

    public int getDefeats() {
        return this.defeats;
    }

    public int getWins() {
        return this.wins;
    }

    public boolean canBattle() {
        var mobTr = RCTMod.get().getTrainerManager().getData(this);

        return !this.isInBattle()
            && this.getCooldown() == 0
            && this.getDefeats() < mobTr.getMaxTrainerDefeats()
            && this.getWins() < mobTr.getMaxTrainerWins();
    }

    private void udpateCustomName() {
        var tmd = RCTMod.get().getTrainerManager().getData(this);
        this.setCustomName(Component.literal(tmd.getTeam().getDisplayName()));
    }

    public void setTrainerId(String trainerId) {
        var level = this.level();

        if(!level.isClientSide) {
            var currentId = this.getTrainerId();

            if((currentId == null && trainerId != null) || !currentId.equals(trainerId)) {
                var spawner = RCTMod.get().getTrainerSpawner();
                spawner.unregisterMob(this);
                this.trainerId = trainerId;
                this.udpateCustomName();
                spawner.registerMob(this);
            }
        }
    }

    public String getTrainerId() {
        return this.trainerId;
    }

    public void finishBattle(boolean defeated) {
        var level = this.level();

        if(!level.isClientSide && isInBattle()) {
            ChatUtils.reply(this, this.getOpponent(), defeated ? "battle_lost" : "battle_won");
            var mobTr = RCTMod.get().getTrainerManager().getData(this);
            this.cooldown = mobTr.getBattleCooldownTicks();
            this.setOpponent(null);
            this.setTarget(null);

            if(defeated) {
                this.defeats++;
                this.dropBattleLoot(mobTr.getLootTableResource());
            } else {
                this.wins++;
            }
        }
    }

    protected void dropBattleLoot(ResourceLocation lootTableResource) {
        var level = this.level();
        var lootTable = level.getServer().getLootData().getLootTable(lootTableResource);
        var builder = (new LootParams.Builder((ServerLevel)level))
            .withParameter(LootContextParams.THIS_ENTITY, this)
            .withParameter(LootContextParams.DAMAGE_SOURCE, this.getLastDamageSource())
            .withParameter(LootContextParams.ORIGIN, this.position());

        lootTable.getRandomItems(
            builder.create(LootContextParamSets.ENTITY),
            this.getLootTableSeed(), this::spawnAtLocation);
    }

    @Override
    public void tick() {
        super.tick();
        var level = this.level();

        if(!level.isClientSide) {
            if(this.cooldown > 0) {
                this.cooldown--;
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        var level = this.level();

        if(!level.isClientSide) {
            if(this.canBattle()) {
                this.startBattleWith(player);
            } else {
                var mobTr = RCTMod.get().getTrainerManager().getData(this);

                if(this.isInBattle()) {
                    ChatUtils.reply(this, player, "is_busy");
                } else if(this.getDefeats() >= mobTr.getMaxTrainerDefeats()) {
                    ChatUtils.reply(this, player, "done_looser");
                } else if(this.getWins() >= mobTr.getMaxTrainerWins()) {
                    ChatUtils.reply(this, player, "done_winner");
                } else if(this.getCooldown() > 0) {
                    ChatUtils.reply(this, player, "on_cooldown");
                } else {
                    ChatUtils.reply(this, player, "done_generic");
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void remove(RemovalReason reason) {
        RCTMod.get().getTrainerSpawner().unregisterMob(this);
        super.remove(reason);
    }

    @Override
    public void onPathfindingStart() {
        if(canBattle()) {
            var level = this.level();
            var target = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 128, this::canBattleAgainst);
            this.setTarget(target);
        }
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public int getDespawnDelay() {
        return this.despawnDelay;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        var level = this.level();

        if(!level.isClientSide) {
            this.maybeDespawn();
        }
    }

    private void maybeDespawn() {
        if(!this.isInBattle()) {
            if(this.despawnDelay == 0 || (this.despawnDelay > 0 && --this.despawnDelay == 0) || !this.canBattle()) {
                var config = RCTMod.get().getServerConfig();
                var level = this.level();

                if(level.getNearestPlayer(this, config.maxHorizontalDistanceToPlayers()) == null) {
                    if(this.discardDelay < 0 || --this.discardDelay < 0) {
                        this.discard();
                    }
                } else {
                    this.discardDelay = DISCARD_DELAY;
                }
            }
        }
    }

    public void setOriginPlayer(UUID originPlayer) {
        if((this.originPlayer == null && originPlayer != null) || !this.originPlayer.equals(originPlayer)) {
            RCTMod.get().getTrainerSpawner().unregisterMob(this);
            this.originPlayer = originPlayer;
            RCTMod.get().getTrainerSpawner().registerMob(this);
        }
    }

    public UUID getOriginPlayer() {
        return this.originPlayer;
    }

    public void setWanderTarget(BlockPos blockPos) {
        this.wanderTarget = blockPos;
    }

    public BlockPos getWanderTarget() {
        return this.wanderTarget;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("DespawnDelay", this.getDespawnDelay());
        compoundTag.putInt("Defeats", this.getDefeats());
        compoundTag.putInt("Wins", this.getWins());
        compoundTag.putInt("Cooldown", this.getCooldown());
        compoundTag.putString("TrainerId", this.getTrainerId());

        if(this.originPlayer != null) {
            compoundTag.putUUID("OriginPlayer", this.originPlayer);
        }

        if(this.wanderTarget != null) {
            compoundTag.put("WanderTarget", NbtUtils.writeBlockPos(this.wanderTarget));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if(compoundTag.contains("DespawnDelay", 99)) {
            this.despawnDelay = compoundTag.getInt("DespawnDelay");
        }

        if(compoundTag.contains("Defeats")) {
            this.defeats = compoundTag.getInt("Defeats");
        }

        if(compoundTag.contains("Wins")) {
            this.wins =  compoundTag.getInt("Wins");
        }

        if(compoundTag.contains("Cooldown")) {
            this.cooldown = compoundTag.getInt("Cooldown");
        }

        if(compoundTag.contains("OriginPlayer")) {
            this.setOriginPlayer(compoundTag.getUUID("OriginPlayer"));
        }

        if(compoundTag.contains("WanderTarget")) {
            this.wanderTarget = NbtUtils.readBlockPos(compoundTag.getCompound("WanderTarget"));
        }

        if(compoundTag.contains("TrainerId")) {
            this.setTrainerId(compoundTag.getString("TrainerId"));
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new PokemonBattleGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Zombie>(this, Zombie.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Evoker>(this, Evoker.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Vindicator>(this, Vindicator.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Vex>(this, Vex.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Pillager>(this, Pillager.class, 15.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Illusioner>(this, Illusioner.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Zoglin>(this, Zoglin.class, 10.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(2, new LookAtPlayerAndWaitGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 0.35, 1.5F*RCTMod.get().getServerConfig().maxHorizontalDistanceToPlayers()));
        // this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
        // this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, // 1.0F));
    }
}
