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

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record LevelRangeCondition(IntRange range) implements LootItemCondition {
    private static Supplier<LootItemConditionType> TYPE = ModRegistries.LootItemConditions.LEVEL_RANGE;

    public static final MapCodec<LevelRangeCondition> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            IntRange.CODEC.fieldOf("range").forGetter(LevelRangeCondition::range)
        ).apply(instance, LevelRangeCondition::new)
    );

    public LootItemConditionType getType() {
        return TYPE.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Collections.unmodifiableSet(new HashSet<>(this.range.getReferencedContextParams()));
    }

    public boolean test(LootContext lootContext) {
        if(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof TrainerMob mob) {
            var teamLevel = RCTMod.getInstance().getTrainerManager()
                .getData(mob).getTrainerTeam().getTeam().stream()
                .map(p -> p.getLevel()).max(Integer::compare).orElse(0);

            return this.range.test(lootContext, teamLevel);
        }

        return false;
    }
}
