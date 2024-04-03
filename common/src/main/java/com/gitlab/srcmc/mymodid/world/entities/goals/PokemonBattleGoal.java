package com.gitlab.srcmc.mymodid.world.entities.goals;

import java.util.EnumSet;

import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class PokemonBattleGoal extends LookAtPlayerGoal {
    private TrainerMob trainer;
    private boolean lookAtPlayer;

    public PokemonBattleGoal(TrainerMob trainer) {
        super(trainer, Mob.class, 8.0F, 1F, false);
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        this.trainer = trainer;
    }

    @Override
    public boolean canUse() {
        if(this.trainer.isInBattle()) {
            if(lookAtPlayer) {
                this.lookAt = this.mob.level().getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            } else {
                this.lookAt = this.mob.level().getNearestEntity(
                    this.mob.level().getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0, (double)this.lookDistance), (livingEntity) -> true),
                    this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            }

            if(this.lookAt == null || this.mob.getRandom().nextFloat() < 0.02) {
                this.lookAtPlayer = !this.lookAtPlayer;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }

    @Override
    public void tick() {
        if(this.lookAt != null && this.lookAt.isAlive()) {
            this.mob.getLookControl().setLookAt(this.lookAt.getX(), this.lookAt.getEyeY(), this.lookAt.getZ());
        }
    }
}
