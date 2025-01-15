/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2025, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctmod.network;

import com.gitlab.srcmc.rctmod.ModCommon;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TrainerTargetPayload(double targetX, double targetY, double targetZ, boolean otherDim) implements CustomPacketPayload {
    public static final Type<TrainerTargetPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "target_trainer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TrainerTargetPayload> CODEC = StreamCodec.of(
        (b, v) -> {
            b.writeDouble(v.targetX);
            b.writeDouble(v.targetY);
            b.writeDouble(v.targetZ);
            b.writeBoolean(v.otherDim);
        },
        b -> new TrainerTargetPayload(
            b.readDouble(),
            b.readDouble(),
            b.readDouble(),
            b.readBoolean()
        )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TrainerTargetPayload.TYPE;
    }
}
