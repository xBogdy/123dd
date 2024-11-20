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

import java.util.Optional;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger.SimpleInstance;

import net.minecraft.server.level.ServerPlayer;

public record DefeatCountTriggerInstance(String trainerId, String trainerType, int count) implements SimpleInstance {
    public boolean matches(ServerPlayer player, TrainerMob mob) {
        ModCommon.LOG.info("MATCHING WITH: " + this.trainerId + ", " + this.trainerType + ", " + this.trainerId() + ", " + this.trainerType());
        var battleMem = RCTMod.getInstance().getTrainerManager().getBattleMemory(mob);
        var mobTr = RCTMod.getInstance().getTrainerManager().getData(mob);
        var playerState = PlayerState.get(player);

        if(!this.trainerId.isEmpty() && this.trainerId.equals(mob.getTrainerId())) {
            return battleMem.getDefeatByCount(player) >= this.count;
        }

        if(!this.trainerType.isEmpty() && this.trainerType.equals(mobTr.getType().name())) {
            return playerState.getTypeDefeatCount(mobTr.getType(), true) >= this.count;
        }

        return this.trainerId.isEmpty() && this.trainerType.isEmpty()
            && battleMem.getDefeatByCount(player) >= this.count;
    }

    @Override
    public Optional<ContextAwarePredicate> player() {
        return Optional.empty();
    }

    public static final Codec<DefeatCountTriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("trainer_id", "").forGetter(DefeatCountTriggerInstance::trainerId),
            Codec.STRING.optionalFieldOf("trainer_type", "").forGetter(DefeatCountTriggerInstance::trainerType),
            Codec.INT.optionalFieldOf("count", 1).forGetter(DefeatCountTriggerInstance::count)
        ).apply(instance, DefeatCountTriggerInstance::new));
}
