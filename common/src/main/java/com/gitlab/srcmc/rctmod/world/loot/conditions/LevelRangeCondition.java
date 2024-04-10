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
package com.gitlab.srcmc.rctmod.world.loot.conditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class LevelRangeCondition implements LootItemCondition {
    private static Supplier<LootItemConditionType> TYPE;

    public static void init(Supplier<LootItemConditionType> type) {
        TYPE = type;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LevelRangeCondition> {
        public Serializer() {
        }

        public void serialize(JsonObject jsonObject, LevelRangeCondition LevelRangeCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("entity", jsonSerializationContext.serialize(LevelRangeCondition.entityTarget));
            jsonObject.add("range", jsonSerializationContext.serialize(LevelRangeCondition.range));
        }

        public LevelRangeCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            var entityTarget = (LootContext.EntityTarget)GsonHelper.getAsObject(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class);
            var intRange = (IntRange) GsonHelper.getAsObject(jsonObject, "range", jsonDeserializationContext, IntRange.class);
            return new LevelRangeCondition(entityTarget, intRange);
        }
    }

    final LootContext.EntityTarget entityTarget;
    final IntRange range;

    LevelRangeCondition(LootContext.EntityTarget entityTarget, IntRange intRange) {
        this.entityTarget = entityTarget;
        this.range = intRange;
    }

    public LootItemConditionType getType() {
        return TYPE.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        var params = new HashSet<>(this.range.getReferencedContextParams());
        params.add(this.entityTarget.getParam());
        return Collections.unmodifiableSet(params);
    }

    public boolean test(LootContext lootContext) {
        if(lootContext.getParamOrNull(this.entityTarget.getParam()) instanceof TrainerMob mob) {
            var teamLevel = RCTMod.get().getTrainerManager()
                .getData(mob).getTeam().getMembers().stream()
                .map(p -> p.getLevel()).max(Integer::compare).orElse(0);

            return this.range.test(lootContext, teamLevel);
        }

        return false;
    }

    public static LootItemCondition.Builder hasValue(LootContext.EntityTarget entityTarget, IntRange intRange) {
        return () -> {
            return new LevelRangeCondition(entityTarget, intRange);
        };
    }

    // Based of: import net.minecraft.world.level.storage.loot.predicates.LootItemConditions.*
    // private static LootItemConditionType register(net.minecraft.world.level.storage.loot.Serializer<? extends LootItemCondition> serializer) {
    //     return (LootItemConditionType) Registry.register(
    //         BuiltInRegistries.LOOT_CONDITION_TYPE,
    //         new ResourceLocation(ModCommon.MOD_ID, "level_range"),
    //         new LootItemConditionType(serializer));
    // }
}
