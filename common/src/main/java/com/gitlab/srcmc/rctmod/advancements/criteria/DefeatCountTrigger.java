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
package com.gitlab.srcmc.rctmod.advancements.criteria;

import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
// import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class DefeatCountTrigger extends SimpleCriterionTrigger<DefeatCountTriggerInstance> {
    // public static final DefeatCountTrigger INSTANCE = CriteriaTriggers.register("defeat_count", new DefeatCountTrigger());

    // private static DefeatCountTrigger instance;

    // public static DefeatCountTrigger get() {
    //     if(instance == null) {
    //         instance = new DefeatCountTrigger();
    //     }

    //     return instance;
    // }

    // @Override
    // public ResourceLocation getId() {
    //     return DefeatCountTriggerInstance.ID;
    // }

    // @Override
    // protected DefeatCountTriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate player, DeserializationContext deserializationContext) {
    //     var jsonTrainerId = jsonObject.get("trainer_id");
    //     var jsonTrainerType = jsonObject.get("trainer_type");
    //     var jsonCount = jsonObject.get("count");
    //     var trainerId = jsonTrainerId != null ? jsonTrainerId.getAsString() : null;
    //     var trainerType = jsonTrainerType != null ? jsonTrainerType.getAsString() : null;
    //     var count = jsonCount != null ? jsonCount.getAsInt() : 1;
    //     return new DefeatCountTriggerInstance(player, trainerId, trainerType, count);
    // }

    public void trigger(ServerPlayer player, TrainerMob mob) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, mob));
    }

    // @Override
    // public Criterion<DefeatCountTriggerInstance> createCriterion(DefeatCountTriggerInstance instance) {
    //     return super.createCriterion(instance);
    // }

    @Override
    public Codec<DefeatCountTriggerInstance> codec() {
        return DefeatCountTriggerInstance.CODEC;
    }
}
