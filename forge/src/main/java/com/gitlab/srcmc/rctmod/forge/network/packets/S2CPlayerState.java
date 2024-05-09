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
package com.gitlab.srcmc.rctmod.forge.network.packets;

import java.util.function.Supplier;

import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class S2CPlayerState {
    private byte[] bytes;

    public S2CPlayerState(byte[] bytes) {
        this.bytes = bytes;
    }

    public static void encoder(S2CPlayerState msg, FriendlyByteBuf buffer) {
        buffer.writeByteArray(msg.bytes);
    }

    public static S2CPlayerState decoder(FriendlyByteBuf buffer) {
        return new S2CPlayerState(buffer.readByteArray());
    }

    public static class Handler {
        public static void handle(S2CPlayerState msg, Supplier<NetworkEvent.Context> ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleOnClient(msg, ctx));
        }
    }

    private static void handleOnClient(S2CPlayerState msg, Supplier<NetworkEvent.Context> ctx) {
        var mc = Minecraft.getInstance();
        PlayerState.get(mc.player).deserializeUpdate(msg.bytes);
    }
}
