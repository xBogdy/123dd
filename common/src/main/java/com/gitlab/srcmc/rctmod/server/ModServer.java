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
package com.gitlab.srcmc.rctmod.server;

import com.gitlab.srcmc.rctmod.network.PlayerStatePayload;
import com.gitlab.srcmc.rctmod.network.TrainerTargetPayload;

import dev.architectury.networking.NetworkManager;

public class ModServer {
    public static void init() {
        NetworkManager.registerS2CPayloadType(PlayerStatePayload.TYPE, PlayerStatePayload.CODEC);
        NetworkManager.registerS2CPayloadType(TrainerTargetPayload.TYPE, TrainerTargetPayload.CODEC);
    }
}
