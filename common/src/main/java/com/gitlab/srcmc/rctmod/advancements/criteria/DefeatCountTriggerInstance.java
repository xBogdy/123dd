package com.gitlab.srcmc.rctmod.advancements.criteria;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class DefeatCountTriggerInstance extends AbstractCriterionTriggerInstance {
    public static final ResourceLocation ID = new ResourceLocation(ModCommon.MOD_ID, "defeat_count");
    private String trainerId;
    private int count = 1;

    public DefeatCountTriggerInstance(ContextAwarePredicate player, String trainerId, int count) {
        super(ID, player);
        this.trainerId = trainerId;
        this.count = count;
    }

    public static DefeatCountTriggerInstance instance(ContextAwarePredicate player, String trainerId, int count) {
        return new DefeatCountTriggerInstance(player, trainerId, count);
    }

    @Override
    public JsonObject serializeToJson(SerializationContext context) {
      var obj = super.serializeToJson(context);

      if(this.trainerId != null) {
          obj.addProperty("trainer", this.trainerId);
      }

      obj.addProperty("count", this.count);
      return obj;
    }

    public boolean matches(ServerPlayer player, TrainerMob mob) {
        var battleMem = RCTMod.get().getTrainerManager().getBattleMemory(mob);

        return (this.trainerId == null || mob.getTrainerId().equals(this.trainerId))
            && battleMem.getDefeatByCount(player) == this.count;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player);
    }
}
