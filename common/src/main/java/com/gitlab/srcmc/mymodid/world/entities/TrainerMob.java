package com.gitlab.srcmc.mymodid.world.entities;

import com.gitlab.srcmc.mymodid.api.RCTMod;
import com.gitlab.srcmc.mymodid.api.utils.ChatUtils;
import com.gitlab.srcmc.mymodid.world.entities.goals.LookAtPlayerAndWaitGoal;
import com.gitlab.srcmc.mymodid.world.entities.goals.PokemonBattleGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class TrainerMob extends PathfinderMob implements Npc {
    private static final EntityDataAccessor<String> DATA_TRAINER_ID = SynchedEntityData.defineId(TrainerMob.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_DEFEATS = SynchedEntityData.defineId(TrainerMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_WINS = SynchedEntityData.defineId(TrainerMob.class, EntityDataSerializers.INT);
    private static final EntityType<TrainerMob> TYPE = EntityType.Builder
        .of(TrainerMob::new, MobCategory.MISC)
        .canSpawnFarFromPlayer()
        .sized(0.6F, 1.95F).build("trainer");

    private static final int DISCARD_DELAY = 100;
    private static final int DESPAWN_DISTANCE = 128; // TODO: configurable despawn distance? (or based of render distance? how?)
    private static final int DESPAWN_DELAY = 24000; // TODO: RCTMod.getConfig()... (default: ~24000 (20 min))

    private int despawnDelay, discardDelay;
    private BlockPos wanderTarget;
    private Player opponent;

    protected TrainerMob(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.despawnDelay = DESPAWN_DELAY;
        this.discardDelay = DISCARD_DELAY;
        this.setCustomNameVisible(true);
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

            return trPlayer.getBadges() >= trMob.getRequiredBadges()
                && trPlayer.getBeatenE4() >= trMob.getRequiredBeatenE4()
                && trPlayer.getBeatenChamps() >= trMob.getRequiredBeatenChamps()
                && trPlayer.getLevelCap() >= trMob.getRequiredLevelCap();
        }

        return false;
    }

    public void startBattleWith(Player player) {
        if(this.canBattle()) {
            var trPlayer = RCTMod.get().getTrainerManager().getData(player);
            var trMob = RCTMod.get().getTrainerManager().getData(this);

            if(trPlayer.getBadges() < trMob.getRequiredBadges()) {
                ChatUtils.reply(this, player, "missing_badges");
            } else if(trPlayer.getBeatenE4() < trMob.getRequiredBeatenE4()) {
                ChatUtils.reply(this, player, "missing_beaten_e4");
            } else if(trPlayer.getBeatenChamps() < trMob.getRequiredBeatenChamps()) {
                ChatUtils.reply(this, player, "missing_beaten_champs");
            } else if(trPlayer.getLevelCap() < trMob.getTeam().getMembers().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0)) {
                ChatUtils.reply(this, player, "low_level_cap");
            } else {
                if(ChatUtils.makebattle(this, player)) {
                    RCTMod.get().getTrainerManager().addBattle(player, this);
                    ChatUtils.reply(this, player, "battle_start");
                    this.setOpponent(player);
                }
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

    protected void updateBattleResult(boolean defeated) {
        var level = this.level();

        if(!level.isClientSide) {
            if(defeated) {
                var defeats = this.getDefeats();
                this.entityData.set(DATA_DEFEATS, defeats < Integer.MAX_VALUE ? defeats + 1 : defeats);
            } else {
                var wins = this.getWins();
                this.entityData.set(DATA_WINS, wins < Integer.MAX_VALUE ? wins + 1 : wins);
            }
        }
    }

    public void setDefeats(int defeats) {
        var level = this.level();

        if(!level.isClientSide) {
            this.entityData.set(DATA_DEFEATS, defeats);
        }
    }

    public int getDefeats() {
        return this.entityData.get(DATA_DEFEATS);
    }

    public void setWins(int wins) {
        var level = this.level();

        if(!level.isClientSide) {
            this.entityData.set(DATA_WINS, wins);
        }
    }

    public int getWins() {
        return this.entityData.get(DATA_WINS);
    }

    public boolean canBattle() {
        var mobTr = RCTMod.get().getTrainerManager().getData(this);

        return !this.isInBattle()
            && this.getDefeats() < mobTr.getMaxTrainerDefeats()
            && this.getWins() < mobTr.getMaxTrainerWins();
    }

    public void setTrainerId(String trainerId) {
        var level = this.level();

        if(!level.isClientSide) {
            this.entityData.set(DATA_TRAINER_ID, trainerId);
            this.udpateCustomName();
        }
    }

    private void udpateCustomName() {
        var tmd = RCTMod.get().getTrainerManager().getData(this);
        this.setCustomName(Component.literal(tmd.getTeam().getDisplayName()));
    }

    public String getTrainerId() {
        return this.entityData.get(DATA_TRAINER_ID);
    }

    public void finishBattle(boolean defeated) {
        var level = this.level();

        if(!level.isClientSide && isInBattle()) {
            ChatUtils.reply(this, this.getOpponent(), defeated ? "battle_lost" : "battle_win");
            this.updateBattleResult(defeated);
            this.setOpponent(null);
            this.setTarget(null);

            if(defeated) {
                this.dropBattleLoot(RCTMod.get()
                    .getTrainerManager().getData(this)
                    .getLootTableResource());
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRAINER_ID, "");
        this.entityData.define(DATA_DEFEATS, 0);
        this.entityData.define(DATA_WINS, 0);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if(this.canBattle()) {
            var level = this.level();

            if(!level.isClientSide) {
                this.startBattleWith(player);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            // TODO: "defeated" or "busy/cooldown" response
            return super.mobInteract(player, interactionHand);
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
                var level = this.level();

                if(level.getNearestPlayer(this, DESPAWN_DISTANCE) == null) {
                    if(this.discardDelay < 0 || --this.discardDelay < 0) {
                        this.discard();
                    }
                } else {
                    this.discardDelay = DISCARD_DELAY;
                }
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
        compoundTag.putInt("DespawnDelay", this.getDespawnDelay());
        compoundTag.putInt("Defeats", this.getDefeats());
        compoundTag.putInt("Wins", this.getWins());
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
        }

        if(compoundTag.contains("Defeats")) {
            this.setDefeats(compoundTag.getInt("Defeats"));
        }

        if(compoundTag.contains("Wins")) {
            this.setWins(compoundTag.getInt("Wins"));
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
        // this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, // 1.0F));
    }
}
