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

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record DefeatCountCondition(Comparator comparator, int count) implements LootItemCondition {
    private static Supplier<LootItemConditionType> TYPE = ModRegistries.LootItemConditions.DEFEAT_COUNT;

    public static final MapCodec<DefeatCountCondition> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Comparator.CODEC.optionalFieldOf("comparator", Comparator.EQUAL).forGetter(DefeatCountCondition::comparator),
            Codec.INT.optionalFieldOf("count", 0).forGetter(DefeatCountCondition::count)
        ).apply(instance, DefeatCountCondition::new)
    );

    public LootItemConditionType getType() {
        return TYPE.get();
    }

    public boolean test(LootContext lootContext) {
        var player = lootContext.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);

        if(player != null && lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof TrainerMob mob) {
            return this.comparator.test(RCTMod.getInstance().getTrainerManager().getBattleMemory(mob).getDefeatByCount(player), this.count);
        }
        
        return false;
    }

    public enum Comparator implements StringRepresentable {
        EQUAL("equal", (a, b) -> a.equals(b)),
        SMALLER("smaller", (a, b) -> a < b),
        GREATER("greater", (a, b) -> a > b),
        MODULO("modulo", (a, b) -> a % b == 0);

        public static final Codec<Comparator> CODEC = StringRepresentable.fromEnum(Comparator::values);
        private BiFunction<Integer, Integer, Boolean> testFunc;
        private String name;

        Comparator(String name, BiFunction<Integer, Integer, Boolean> testFunc) {
            this.testFunc = testFunc;
        }

        public boolean test(int a, int b) {
            return this.testFunc.apply(a, b);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
