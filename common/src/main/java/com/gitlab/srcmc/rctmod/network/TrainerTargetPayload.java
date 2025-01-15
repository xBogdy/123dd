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
