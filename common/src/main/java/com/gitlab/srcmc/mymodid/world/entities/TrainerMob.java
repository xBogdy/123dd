package com.gitlab.srcmc.mymodid.world.entities;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.api.ChatUtils;
import com.gitlab.srcmc.mymodid.world.entities.goals.LookAtPlayerAndWaitGoal;
import com.gitlab.srcmc.mymodid.world.entities.goals.PokemonBattleGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.ServerLevelAccessor;

public class TrainerMob extends PathfinderMob implements Npc {
    private static final EntityDataAccessor<String> DATA_TRAINER_ID = SynchedEntityData.defineId(TrainerMob.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_DEFEATED = SynchedEntityData.defineId(TrainerMob.class, EntityDataSerializers.BOOLEAN);
    private static final EntityType<TrainerMob> TYPE = EntityType.Builder
        .of(TrainerMob::new, MobCategory.MISC)
        .canSpawnFarFromPlayer()
        .sized(0.6F, 1.95F).build("trainer");

    private int initDespawnDelay, despawnDelay;
    private BlockPos wanderTarget;
    private Player opponent;

    protected TrainerMob(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomNameVisible(true);
    }

    public static EntityType<TrainerMob> getEntityType() {
        return TYPE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 48.0);
    }

    public boolean canBattle(Entity e) {
        if(e instanceof Player player) {
            var trPlayer = ModCommon.TRAINER_MANAGER.getData(player);
            var trMob = ModCommon.TRAINER_MANAGER.getData(this);

            return trPlayer.getBadges() >= trMob.getRequiredBadges()
                && trPlayer.getBeatenE4() >= trMob.getRequiredBeatenE4()
                && trPlayer.getBeatenChamps() >= trMob.getRequiredBeatenChamps()
                && trPlayer.getLevelCap() >= trMob.getRequiredLevelCap();
        }

        return false;
    }

    public void startBattleWith(Player player) {
        if(!this.isInBattle() && !this.isDefeated()) {
            var trPlayer = ModCommon.TRAINER_MANAGER.getData(player);
            var trMob = ModCommon.TRAINER_MANAGER.getData(this);

            if(trPlayer.getBadges() < trMob.getRequiredBadges()) {
                ChatUtils.reply(this, player, "missing_badges");
            } else if(trPlayer.getBeatenE4() < trMob.getRequiredBeatenE4()) {
                ChatUtils.reply(this, player, "missing_beaten_e4");
            } else if(trPlayer.getBeatenChamps() < trMob.getRequiredBeatenChamps()) {
                ChatUtils.reply(this, player, "missing_beaten_champs");
            } else if(trPlayer.getLevelCap() < trMob.getTeam().getMembers().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0)) {
                ChatUtils.reply(this, player, "low_level_cap");
            } else {
                this.setOpponent(player);
                ChatUtils.reply(this, player, "battle_start");
                ChatUtils.battle(this, player);
            }
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

    protected void setDefeated(boolean defeated) {
        var level = this.level();

        if(!level.isClientSide) {
            this.entityData.set(DATA_DEFEATED, defeated);
        }
    }

    public boolean isDefeated() {
        return this.entityData.get(DATA_DEFEATED);
    }

    public void setTrainerId(String trainerId) {
        var level = this.level();

        if(!level.isClientSide) {
            this.entityData.set(DATA_TRAINER_ID, trainerId);
            var tmd = ModCommon.TRAINER_MANAGER.getData(this);
            this.setCustomName(Component.literal(tmd.getTeam().getDisplayName()));
        }
    }

    public String getTrainerId() {
        return this.entityData.get(DATA_TRAINER_ID);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRAINER_ID, "trainer");
        this.entityData.define(DATA_DEFEATED, false);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if(!this.isInBattle() && !this.isDefeated()) {
            var level = this.level();

            if(!level.isClientSide) {
                this.startBattleWith(player);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            // TEST
            var level = this.level();
            if(!level.isClientSide) {
                this.setDefeated(true);
                this.setOpponent(null);
                this.setTarget(null);
                ChatUtils.reply(this, player, "battle_lost");
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
            /////

            // return super.mobInteract(player, interactionHand);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance,
        MobSpawnType mobSpawnType, SpawnGroupData spawnGroupData, CompoundTag compoundTag)
    {
        if(spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(false);
        }

        return super.finalizeSpawn(
            serverLevelAccessor, difficultyInstance, mobSpawnType,
            (SpawnGroupData) spawnGroupData, compoundTag);
    }

    @Override
    public void onPathfindingStart() {
        if(!this.isInBattle() && !this.isDefeated()) {
            var level = this.level();
            var target = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 128, this::canBattle);
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

    public void setDespawnDelay(int i) {
        this.despawnDelay = i;
        this.initDespawnDelay = i;
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
        if(this.despawnDelay > 0 && !this.isInBattle() && --this.despawnDelay == 0) {
            if(!this.isDefeated() && (this.initDespawnDelay /= 2) > 0) {
                this.despawnDelay = this.initDespawnDelay;
            } else {
                ModCommon.LOG.info("DESPAWNING: " + this.getName().getString());
                this.discard();
            }
        }
    }

    public void setWanderTarget(BlockPos blockPos) {
        this.wanderTarget = blockPos;
    }

    BlockPos getWanderTarget() {
        return this.wanderTarget;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("DespawnDelay", this.despawnDelay);
        compoundTag.putBoolean("Defeated", this.isDefeated());
        compoundTag.putString("TrainerId", this.getTrainerId());

        if(this.wanderTarget != null) {
            compoundTag.put("WanderTarget", NbtUtils.writeBlockPos(this.wanderTarget));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if(compoundTag.contains("DespawnDelay", 99)) {
            this.despawnDelay = compoundTag.getInt("DespawnDelay");
            this.initDespawnDelay = this.despawnDelay;
        }

        if(compoundTag.contains("Defeated")) {
            this.setDefeated(compoundTag.getBoolean("Defeated"));
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
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 0.35, 64F));
        // this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
        // this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F,
        // 1.0F));
    }
}
