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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.TrainerBattle;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.utils.ChatUtils;
import com.gitlab.srcmc.rctmod.world.entities.goals.LookAtPlayerAndWaitGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.MoveCloseToTargetGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.MoveToHomePosGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.PokemonBattleGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.RandomStrollAwayGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.RandomStrollThroughVillageGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
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
    private static final EntityDataAccessor<String> DATA_TRAINER_ID = SynchedEntityData.defineId(TrainerMob.class, EntityDataSerializers.STRING);
    private static final EntityType<TrainerMob> TYPE = EntityType.Builder
        .of(TrainerMob::new, MobCategory.MISC)
        .canSpawnFarFromPlayer()
        .sized(0.6F, 1.95F).build("trainer");

    private final static int TICKS_TO_DESPAWN = 600;
    private final static int DESPAWN_TICK_SCALE = 20;
    private final static int DESPAWN_DISTANCE = 128;
    private final static int MAX_PLAYER_TRACKING_RANGE = 128;
    private final static int TARGET_UPDATE_INTERVAL = 120;

    private Map<UUID, int[]> winsAndDefeats = new HashMap<>();
    private int cooldown;
    private Player opponent;
    private UUID originPlayer;
    private boolean persistent;
    private int despawnTicks;
    private BlockPos homePos;

    protected TrainerMob(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("Trainer"));
    }

    public static EntityType<TrainerMob> getEntityType() {
        return TYPE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.5)
            .add(Attributes.FOLLOW_RANGE, 48.0)
            .add(Attributes.MAX_HEALTH, 30);
    }

    public boolean canBattleAgainst(Entity e) {
        if(e instanceof Player player) {
            var rct = RCTMod.getInstance();
            var tm = rct.getTrainerManager();
            var cfg = rct.getServerConfig();
            var tpd = tm.getData(player);
            var tmd = tm.getData(this);

            return this.getCooldown() == 0
                && !this.isInBattle()
                && !rct.isInBattle(player)
                && tm.getActivePokemon(player) > 0
                && tpd.getLevelCap() >= tmd.getRequiredLevelCap()
                && (cfg.allowOverLeveling() || tm.getPlayerLevel(player) <= tpd.getLevelCap())
                && tmd.getMissingRequirements(tm.getData(player).getDefeatedTrainerIds()).findFirst().isEmpty()
                && this.couldBattleAgainst(e);
        }

        return false;
    }

    public boolean couldBattleAgainst(Entity e) {
        if(e instanceof Player player) {
            return this.isPersistenceRequired() || (!this.wasDefeatedBy(player.getUUID()) && !this.wasVictoriousAgainst(player.getUUID()));
        }

        return false;
    }

    public void startBattleWith(Player player) {
        if(this.canBattleAgainst(player)) {
            if(RCTMod.getInstance().makeBattle(this, player)) {
                this.setOpponent(player);
                RCTMod.getInstance().getTrainerManager().addBattle(player, this);
                ChatUtils.reply(this, player, "battle_start");
            }
        } else {
            this.replyTo(player);
        }
    }

    protected void replyTo(Player player) {
        var tm = RCTMod.getInstance().getTrainerManager();
        var cfg = RCTMod.getInstance().getServerConfig();
        var tmd = tm.getData(this);
        var tpd = tm.getData(player);
        var msr = tmd.getMissingRequirements(tm.getData(player).getDefeatedTrainerIds()).findFirst();

        if(this.isInBattle()) {
            ChatUtils.reply(this, player, "is_busy");
        } else if(!this.isPersistenceRequired() && this.wasDefeatedBy(player.getUUID())) {
            ChatUtils.reply(this, player, "done_looser");
        } else if(!this.isPersistenceRequired() && this.wasVictoriousAgainst(player.getUUID())) {
            ChatUtils.reply(this, player, "done_winner");
        } else if(this.getCooldown() > 0) {
            ChatUtils.reply(this, player, "on_cooldown");
        } else if(RCTMod.getInstance().isInBattle(player)) {
            ChatUtils.reply(this, player, "player_busy");
        } else if(msr.isPresent()) {
            var t = tm.getData(msr.get());

            switch(t.getType()) {
                case LEADER:
                    ChatUtils.reply(this, player, "missing_badges");
                    break;
                case E4:
                    ChatUtils.reply(this, player, "missing_beaten_e4");
                    break;
                case CHAMP:
                    ChatUtils.reply(this, player, "missing_beaten_champs");
                    break;
                case RIVAL:
                    ChatUtils.reply(this, player, "missing_beaten_rival");
                    break;
                case TEAM_ROCKET:
                    ChatUtils.reply(this, player, "missing_beaten_team_rocket");
                    break;
                default:
                    ModCommon.LOG.info("Unexpected reason for battle failure: " + player.getDisplayName().getString() + " vs " + this.getTrainerId());
                    break;
            }
        } else if(tpd.getLevelCap() < tmd.getRequiredLevelCap()) {
            ChatUtils.reply(this, player, "low_level_cap");
        } else if(tm.getPlayerLevel(player) > tpd.getLevelCap() && !cfg.allowOverLeveling()) {
            ChatUtils.reply(this, player, "over_level_cap");
        } else if(tm.getActivePokemon(player) == 0) {
            ChatUtils.reply(this, player, "missing_pokemon");
        } else {
            ChatUtils.reply(this, player, "done_generic");
        }
    }

    protected void setOpponent(Player player) {
        this.opponent = player;
    }

    public Player getOpponent() {
        return this.opponent;
    }

    public boolean isInBattle() {
        return this.opponent != null;
    }

    private void addWins(Player player, int wins) {
        this.winsAndDefeats.compute(player.getUUID(), (k, v) -> {
            if(v == null) {
                return new int[]{wins, 0};
            }

            v[0] += wins;
            return v;
        });
    }

    private void addDefeats(Player player, int defeats) {
        this.winsAndDefeats.compute(player.getUUID(), (k, v) -> {
            if(v == null) {
                return new int[]{0, defeats};
            }

            v[1] += defeats;
            return v;
        });
    }

    public boolean wasDefeatedBy(UUID opponentUUID) {
        var wd = this.winsAndDefeats.get(opponentUUID);
        return wd != null && wd[1] >= RCTMod.getInstance().getTrainerManager().getData(this).getMaxTrainerDefeats();
    }

    public boolean wasVictoriousAgainst(UUID opponentUUID) {
        var wd = this.winsAndDefeats.get(opponentUUID);
        return wd != null && wd[0] >= RCTMod.getInstance().getTrainerManager().getData(this).getMaxTrainerWins();
    }

    public boolean wasDefeated() {
        var maxDefeats = RCTMod.getInstance().getTrainerManager().getData(this).getMaxTrainerDefeats();

        if(maxDefeats > 0) {
            for(var wd : this.winsAndDefeats.values()) {
                if(wd[1] >= maxDefeats) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean wasVictorious() {
        var maxWins = RCTMod.getInstance().getTrainerManager().getData(this).getMaxTrainerWins();

        if(maxWins > 0) {
            for(var wd : this.winsAndDefeats.values()) {
                if(wd[0] >= maxWins) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean wasExhausted() {
        return !this.isPersistenceRequired() && (this.wasDefeated() || this.wasVictorious());
    }

    private void udpateCustomName() {
        var tmd = RCTMod.getInstance().getTrainerManager().getData(this);
        this.setCustomName(Component.literal(tmd.getTrainerTeam().getName()));
    }

    @Override
    public Component getDisplayName() {
        var tmd = RCTMod.getInstance().getTrainerManager().getData(this);
        var suffix = new StringBuilder();
        var cmp = this.getCustomName().copy();
        var localPlayer = ModCommon.getLocalPlayer();
        
        if(localPlayer.isPresent()) {
            var cfg = RCTMod.getInstance().getClientConfig();
            var player = localPlayer.get();

            if(cfg.showTrainerTypeSymbols()) {
                var sym = tmd.getType().toString();
                
                if(sym.length() > 0) {
                    suffix.append(' ').append(sym);
                }
            }

            if(cfg.showTrainerTypeColors()) {
                cmp.setStyle(cmp.getStyle().withColor(tmd.getType().toColor()));
            }

            if(PlayerState.get(player).getTrainerDefeatCount(this.getTrainerId()) == 0) {
                cmp.setStyle(cmp.getStyle().withItalic(true));
            }
        }

        return cmp.append(suffix.toString());
    }

    public void setTrainerId(String trainerId) {
        var level = this.level();

        if(!level.isClientSide) {
            var currentId = this.getTrainerId();

            if(!Objects.equals(currentId, trainerId)) {
                RCTMod.getInstance().getTrainerSpawner().notifyChangeTrainerId(this, trainerId);
                this.entityData.set(DATA_TRAINER_ID, trainerId);
                this.updateTrainerNPC(trainerId);
                this.udpateCustomName();
            }
        }
    }

    private void updateTrainerNPC(String trainerId) {
        try {
            var trainer = RCTApi.getInstance().getTrainerRegistry().getById(trainerId, TrainerNPC.class);

            if(trainer != null) {
                trainer.setEntity(this);
            } else {
                ModCommon.LOG.error(String.format("Invalid trainer id '%s' (%s): not found", trainerId, this.getStringUUID()));
            }
        } catch(IllegalArgumentException e) {
            ModCommon.LOG.error(String.format("Invalid trainer id '%s' (%s)", trainerId, this.getStringUUID()), e);
        }
    }

    public String getTrainerId() {
        return this.entityData.get(DATA_TRAINER_ID);
    }

    public void setHomePos(BlockPos blockPos) {
        var level = this.level();

        if(!level.isClientSide) {
            var currentHome = this.getHomePos();

            if(!Objects.equals(currentHome, blockPos)) {
                this.homePos = blockPos;
            }
        }
    }

    public BlockPos getHomePos() {
        return this.homePos;
    }

    public void finishBattle(TrainerBattle battle, boolean defeated) {
        var level = this.level();

        if(!level.isClientSide && this.isInBattle()) {
            var mobTr = RCTMod.getInstance().getTrainerManager().getData(this);
            var opponent = this.getOpponent();

            this.cooldown = mobTr.getBattleCooldownTicks();
            this.setOpponent(null);
            this.setTarget(null);

            if(defeated) {
                if(battle.getInitiatorSideMobs().contains(this)) {
                    battle.getTrainerSidePlayers().forEach(player -> {
                        ChatUtils.reply(this, player, "battle_lost");
                    });
                } else {
                    var tm = RCTMod.getInstance().getTrainerManager();
                    var initiatorSidePlayer = battle.getInitiatorSidePlayers();
                    Player minWinsPlayer = null;

                    for(var player : initiatorSidePlayer) {
                        ChatUtils.reply(this, player, "battle_lost");
                        minWinsPlayer = minWinsPlayer == null || tm.getBattleMemory(this).getDefeatByCount(player) < tm.getBattleMemory(this).getDefeatByCount(player) ? player : minWinsPlayer;
                    }

                    if(minWinsPlayer != null) {
                        this.dropBattleLoot(mobTr.getLootTableResource(), minWinsPlayer);
                    }
                }

                if(opponent != null) {
                    this.addDefeats(opponent, 1);
                }
            } else {
                if(battle.getInitiatorSideMobs().contains(this)) {
                    battle.getTrainerSidePlayers().forEach(player -> {
                        ChatUtils.reply(this, player, "battle_won");
                    });
                } else {
                    battle.getInitiatorSidePlayers().forEach(player -> {
                        ChatUtils.reply(this, player, "battle_won");
                    });
                }

                this.addWins(opponent, 1);
            }

            if(this.originPlayer != null && (this.wasDefeatedBy(this.originPlayer) || this.wasVictoriousAgainst(this.originPlayer))) {
                this.setOriginPlayer(null);
            }
        }
    }

    public void cancelBattle() {
        if(this.opponent != null) {
            RCTMod.getInstance().getTrainerManager().removeBattle(this.opponent.getUUID());
            RCTMod.getInstance().stopBattle(this);
            this.setOpponent(null);
        }
    }

    protected void dropBattleLoot(ResourceLocation lootTableResource, Player player) {
        var level = this.level();
        var lootTable = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTableResource));
        var builder = (new LootParams.Builder((ServerLevel)level))
            .withParameter(LootContextParams.THIS_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, this.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
            .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player);

        lootTable.getRandomItems(
            builder.create(LootContextParamSets.ENTITY),
            this.getLootTableSeed(), this::spawnAtLocation);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TRAINER_ID, "");
    }

    @Override
    public void tick() {
        super.tick();
        var level = this.level();

        if(!level.isClientSide) {
            if(this.cooldown > 0) {
                this.cooldown--;
            }

            if(this.isInBattle()) {
                var opponent = this.getOpponent();

                if(opponent != null && !opponent.isAlive()) {
                    this.addWins(opponent, 1);
                    this.cancelBattle();
                }
            } else {
                this.updateTarget();
            }

            if(this.isPersistenceRequired() && !RCTMod.getInstance().getTrainerSpawner().isRegistered(this)) {
                this.setPersistent(false);

                ModCommon.LOG.error(String.format(
                    "Disabled persistence for unregistered trainer '%s' (%s)",
                    this.getTrainerId(), this.getStringUUID()));
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        var level = this.level();

        if(!level.isClientSide) {
            this.startBattleWith(player);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void remove(RemovalReason reason) {
        if(RCTMod.getInstance().getServerConfig().logSpawning()) {
            ModCommon.LOG.info(String.format("Removed trainer '%s' (%s): %s", this.getTrainerId(), this.getStringUUID(), reason.toString()));
        }

        if(reason == RemovalReason.DISCARDED || reason == RemovalReason.KILLED) {
            RCTMod.getInstance().getTrainerSpawner().unregister(this);
            this.cancelBattle();
        }

        super.remove(reason);
    }

    private void updateTarget() {
        if(this.tickCount % TARGET_UPDATE_INTERVAL == 0) {
            var tm = RCTMod.getInstance().getTrainerManager();
            int reqLevelCap = tm.getData(this).getRequiredLevelCap();

            this.setTarget(this.level()
                .getNearbyPlayers(
                    TargetingConditions
                        .forNonCombat()
                        .ignoreLineOfSight()
                        .selector(p -> PlayerState.get((Player)p).getLevelCap() >= reqLevelCap
                            && !this.wasDefeatedBy(p.getUUID())
                            && !this.wasVictoriousAgainst(p.getUUID())),
                    this, this.getBoundingBox().inflate(MAX_PLAYER_TRACKING_RANGE))
                .stream()
                    .min((p1, p2) -> Integer.compare(Math.abs(tm.getPlayerLevel(p1) - reqLevelCap), Math.abs(tm.getPlayerLevel(p2) - reqLevelCap)))
                    .orElse(null));
        }
    }

    public void setPersistent(boolean persistent) {
        if(this.persistent != persistent) {
            RCTMod.getInstance().getTrainerSpawner().notifyChangePersistence(this, persistent);
            this.persistent = persistent;
        }
    }

    @Override
    public boolean isPersistenceRequired() {
        return this.persistent;
    }

    @Override
    public boolean shouldBeSaved() {
        return this.isPersistenceRequired();
    }

    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public boolean canChangeDimensions(Level level1, Level level2) {
        return false;
    }

    public boolean shouldDespawn() {
        if(++this.despawnTicks % DESPAWN_TICK_SCALE == 0) {
            if(!this.isInBattle() && this.level().getNearestPlayer(this, Math.max(DESPAWN_DISTANCE, RCTMod.getInstance().getServerConfig().maxHorizontalDistanceToPlayers())) == null) {
                return this.despawnTicks >= TICKS_TO_DESPAWN;
            }

            this.despawnTicks = 0;
        }

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

    public void setOriginPlayer(UUID originPlayer) {
        if((this.originPlayer == null && originPlayer != null) || (this.originPlayer != null && !this.originPlayer.equals(originPlayer))) {
            RCTMod.getInstance().getTrainerSpawner().notifyChangeOriginPlayer(this, originPlayer);
            this.originPlayer = originPlayer;
        }
    }

    public UUID getOriginPlayer() {
        return this.originPlayer;
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Cooldown", this.getCooldown());
        compoundTag.putString("TrainerId", this.getTrainerId());
        compoundTag.putBoolean("Persistent", this.isPersistenceRequired());

        if(this.homePos != null) {
            compoundTag.putLong("HomePos", this.homePos.asLong());
        }

        if(this.originPlayer != null) {
            compoundTag.putUUID("OriginPlayer", this.originPlayer);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if(compoundTag.contains("Cooldown")) {
            this.cooldown = compoundTag.getInt("Cooldown");
        }

        if(compoundTag.contains("TrainerId")) {
            this.setTrainerId(compoundTag.getString("TrainerId"));
        }

        if(compoundTag.contains("HomePos")) {
            this.setHomePos(BlockPos.of(compoundTag.getLong("HomePos")));
        }

        if(compoundTag.contains("OriginPlayer")) {
            this.setOriginPlayer(compoundTag.getUUID("OriginPlayer"));
        }

        if(compoundTag.contains("Persistent")) {
            this.setPersistent(compoundTag.getBoolean("Persistent"));
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        var level = this.level();

        if(!level.isClientSide && this.isAlive()) {
            if(this.random.nextInt(400) == 0 && this.deathTime == 0) {
               this.heal(1.0F);
            }
        }
    }

    @Override
    protected void registerGoals() {
        var maxTrackingDistance = 2*RCTMod.getInstance().getServerConfig().maxHorizontalDistanceToPlayers();

        this.getNavigation().setCanFloat(true);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new PokemonBattleGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Zombie>(this, Zombie.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Evoker>(this, Evoker.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Vindicator>(this, Vindicator.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Vex>(this, Vex.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Pillager>(this, Pillager.class, 15.0F, 0.5, 0.5));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Illusioner>(this, Illusioner.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Zoglin>(this, Zoglin.class, 10.0F, 0.5, 0.5));
        this.goalSelector.addGoal(2, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(3, new TemptGoal(this, 0.35, item -> item.is(ModRegistries.Items.TRAINER_CARD.get()), false));
        this.goalSelector.addGoal(4, new LookAtPlayerAndWaitGoal(this, LivingEntity.class, 2.0F, 0.04F, 160, 320));
        this.goalSelector.addGoal(4, new LookAtPlayerAndWaitGoal(this, LivingEntity.class, 4.0F, 0.004F, 80, 160));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));
        this.goalSelector.addGoal(5, new MoveToHomePosGoal(this));
        this.goalSelector.addGoal(6, new RandomStrollAwayGoal(this, 0.35, () -> 0.0025f, m -> { var tr = (TrainerMob)m; return !tr.wasExhausted(); }));
        this.goalSelector.addGoal(8, new MoveCloseToTargetGoal(this, 0.35, () -> this.requiredBy(this.getTarget()) ? 0.25f : 0.0025f, maxTrackingDistance));
        this.goalSelector.addGoal(10, new RandomStrollThroughVillageGoal(this, 0.35F, () -> 0.0025f));
        this.goalSelector.addGoal(12, new WaterAvoidingRandomStrollGoal(this, 0.35));
    }

    public boolean requiredBy(LivingEntity entity) {
        if(entity instanceof Player player) {
            var tmd = RCTMod.getInstance().getTrainerManager().getData(this);
            var tpd = RCTMod.getInstance().getTrainerManager().getData(player);

            return !tpd.getDefeatedTrainerIds().contains(this.getTrainerId())
                && tmd.getMissingRequirements(tpd.getDefeatedTrainerIds()).findFirst().isEmpty();
        }

        return false;
    }
}
