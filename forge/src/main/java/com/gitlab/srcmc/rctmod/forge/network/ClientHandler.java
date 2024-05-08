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
package com.gitlab.srcmc.rctmod.forge.network;

import java.util.function.Supplier;

import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.forge.network.packets.S2CPlayerState;

import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

public class ClientHandler {
    public static void handlePlayerState(S2CPlayerState msg, Supplier<NetworkEvent.Context> ctx) {
        var mc = Minecraft.getInstance();
        PlayerState.get(mc.player).deserializeUpdate(msg.getBytes());
    }
}
