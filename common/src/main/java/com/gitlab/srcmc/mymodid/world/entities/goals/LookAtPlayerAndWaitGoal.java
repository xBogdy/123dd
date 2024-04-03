package com.gitlab.srcmc.mymodid.world.entities.goals;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class LookAtPlayerAndWaitGoal extends LookAtPlayerGoal {
    public LookAtPlayerAndWaitGoal(Mob mob, Class<? extends LivingEntity> class_, float f) {
        this(mob, class_, f, 1F);
    }

    public LookAtPlayerAndWaitGoal(Mob mob, Class<? extends LivingEntity> class_, float f, float g) {
        this(mob, class_, f, g, false);
    }

    public LookAtPlayerAndWaitGoal(Mob mob, Class<? extends LivingEntity> class_, float f, float g, boolean bl) {
        super(mob, class_, f, g, bl);
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }
}
