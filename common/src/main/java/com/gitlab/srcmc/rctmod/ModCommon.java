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
package com.gitlab.srcmc.rctmod;

import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctmod.server.ModServer;
import net.minecraft.world.entity.player.Player;

public class ModCommon {
    public static final String MOD_ID = "rctmod";
    public static final String MOD_NAME = "Radical Cobblemon Trainers";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final RCTApi RCT = RCTApi.initInstance(MOD_ID);
    public static Supplier<Player> player;

    public static void init() {
        ModServer.initCommon();
    }

    // call on client to supply Minecraft.getInstance().player
    public static void initPlayer(Supplier<Player> player) {
        ModCommon.player = player;
    }

    public static Player localPlayer() {
        if(ModCommon.player == null) {
            throw new IllegalStateException("Local player not initialized, call ModCommon.initPlayer on the client side");
        }

        return ModCommon.player.get();
    }
}
