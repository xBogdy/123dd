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

import java.util.function.Supplier;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class DefeatCountCondition implements LootItemCondition {
    private static Supplier<LootItemConditionType> TYPE;

    public static void init(Supplier<LootItemConditionType> type) {
        TYPE = type;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<DefeatCountCondition> {
        public Serializer() {
        }

        public void serialize(JsonObject jsonObject, DefeatCountCondition defeatCountCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("count", jsonSerializationContext.serialize(defeatCountCondition.count));
        }

        public DefeatCountCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            var count = GsonHelper.getAsInt(jsonObject, "count");
            return new DefeatCountCondition(count);
        }
    }

    final int count;

    DefeatCountCondition(int count) {
        this.count = count;
    }

    public LootItemConditionType getType() {
        return TYPE.get();
    }

    public boolean test(LootContext lootContext) {
        var player = lootContext.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);

        if(player != null && lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof TrainerMob mob) {
            return this.count == RCTMod.get().getTrainerManager().getBattleMemory(mob).getDefeatByCount(player);
        }
        
        return false;
    }

    public static LootItemCondition.Builder hasValue(int count) {
        return () -> {
            return new DefeatCountCondition(count);
        };
    }
}
