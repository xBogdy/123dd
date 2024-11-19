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
package com.gitlab.srcmc.rctmod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.gitlab.srcmc.rctmod.advancements.criteria.DefeatCountTrigger;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.client.screens.IScreenManager;
import com.gitlab.srcmc.rctmod.commands.PlayerCommands;
import com.gitlab.srcmc.rctmod.commands.TrainerCommands;
import com.gitlab.srcmc.rctmod.config.ClientConfig;
import com.gitlab.srcmc.rctmod.config.ServerConfig;
import com.gitlab.srcmc.rctmod.platform.CobblemonHandler;
import com.gitlab.srcmc.rctmod.platform.ModRegistries;
import com.gitlab.srcmc.rctmod.platform.util.Configs;
import com.gitlab.srcmc.rctmod.platform.util.PlayerController;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.advancements.CriteriaTriggers;

public class ModCommon {
    public static final String MOD_ID = "rctmod";
    public static final String MOD_NAME = "Radical Cobblemon Trainers";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static IScreenManager SCREENS = new IScreenManager() {}; // needs to be set on client side

    public static void init() {
        ModCommon.LOG.info("INITIALIZING COMMON");
        ModRegistries.init();
        ModCommon.registerCommands();
        // ModCommon.registerAdvancementCriteria();
        ModCommon.registerCobblemonEvents();
        RCTMod.init(new PlayerController(), new Configs(new ClientConfig(), new ServerConfig(), null));
    }

    static void registerCommands() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> {
            PlayerCommands.register(dispatcher);
            TrainerCommands.register(dispatcher);
        });
    }

    // static void registerAdvancementCriteria() {
    //     CriteriaTriggers.register("defeat_count", DefeatCountTrigger.get()); // TODO: registry frozen (neoforge)
    // }

    static void registerCobblemonEvents() {
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, CobblemonHandler::handleBattleVictory);
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(Priority.HIGHEST, CobblemonHandler::handleExperienceGained);
    }
}
