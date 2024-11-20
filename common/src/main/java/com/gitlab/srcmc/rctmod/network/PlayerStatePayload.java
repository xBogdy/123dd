package com.gitlab.srcmc.rctmod.network;

import com.gitlab.srcmc.rctmod.ModCommon;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlayerStatePayload(byte[] bytes) implements CustomPacketPayload {
    public static final Type<PlayerStatePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "player_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStatePayload> CODEC = StreamCodec.of(
        (b, v) -> { b.writeByteArray(v.bytes()); },
        b -> PlayerStatePayload.of(b.readByteArray())
    );

    public static PlayerStatePayload of(byte[] bytes) {
        return new PlayerStatePayload(bytes);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PlayerStatePayload.TYPE;
    }
}
