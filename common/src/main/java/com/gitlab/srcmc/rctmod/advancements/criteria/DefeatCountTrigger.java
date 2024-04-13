package com.gitlab.srcmc.rctmod.advancements.criteria;

import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class DefeatCountTrigger extends SimpleCriterionTrigger<DefeatCountTriggerInstance> {
    private static DefeatCountTrigger instance;

    public static DefeatCountTrigger get() {
        if(instance == null) {
            instance = new DefeatCountTrigger();
        }

        return instance;
    }

    @Override
    public ResourceLocation getId() {
        return DefeatCountTriggerInstance.ID;
    }

    @Override
    protected DefeatCountTriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate player, DeserializationContext deserializationContext) {
        var jsonTrainerId = jsonObject.get("trainer");
        var jsonCount = jsonObject.get("count");
        var trainerId = jsonTrainerId != null ? jsonTrainerId.getAsString() : null;
        var count = jsonCount != null ? jsonCount.getAsInt() : 1;
        return new DefeatCountTriggerInstance(player, trainerId, count);
    }

    public void trigger(ServerPlayer player, TrainerMob mob) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, mob));
    }
}
