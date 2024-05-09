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
package com.gitlab.srcmc.rctmod.config;

import com.gitlab.srcmc.rctmod.api.config.IClientConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig;

public class ClientConfig extends ForgeConfig implements IClientConfig {
    // trainers
    private ConfigValue<Boolean> showTrainerTypeSymbolsValue;
    private ConfigValue<Boolean> showTrainerTypeColorsValue;

    private ForgeConfigSpec spec;

    public ClientConfig() {
        super(ModConfig.Type.CLIENT);

        var builder = createBuilder();
        builder.push("Trainers");

        this.showTrainerTypeSymbolsValue = builder
            .comment("Determines if symbols for trainer types are shown next to trainer names.")
            .define("showTrainerTypeSymbols", IClientConfig.super.showTrainerTypeSymbols());

        this.showTrainerTypeColorsValue = builder
            .comment("Determines if trainer names are colored based of their type.")
            .define("showTrainerTypeColors", IClientConfig.super.showTrainerTypeColors());

        this.spec = builder.build();
    }

    @Override
    public ForgeConfigSpec getSpec() {
        return this.spec;
    }

    @Override
    public boolean showTrainerTypeSymbols() {
        return this.showTrainerTypeSymbolsValue.get();
    }

    @Override
    public boolean showTrainerTypeColors() {
        return this.showTrainerTypeColorsValue.get();
    }
}
