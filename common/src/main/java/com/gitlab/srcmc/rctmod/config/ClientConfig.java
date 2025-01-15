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
package com.gitlab.srcmc.rctmod.config;

import com.gitlab.srcmc.rctmod.api.config.IClientConfig;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class ClientConfig implements IClientConfig {
    // trainers
    private ConfigValue<Boolean> showTrainerTypeSymbolsValue;
    private ConfigValue<Boolean> showTrainerTypeColorsValue;

    // trainer card
    private ConfigValue<Integer> trainerCardPaddingValue;
    private ConfigValue<Double> trainerCardAlignmentXValue;
    private ConfigValue<Double> trainerCardAlignmentYValue;

    private ModConfigSpec spec;

    public ClientConfig() {
        var builder = new ModConfigSpec.Builder();
        builder.push("Trainers");

        this.showTrainerTypeSymbolsValue = builder
            .comment("Determines if symbols for trainer types are shown next to trainer names.")
            .define("showTrainerTypeSymbols", IClientConfig.super.showTrainerTypeSymbols());

        this.showTrainerTypeColorsValue = builder
            .comment("Determines if trainer names are colored based of their type.")
            .define("showTrainerTypeColors", IClientConfig.super.showTrainerTypeColors());

        builder.pop();
        builder.push("Trainer Card");

        this.trainerCardPaddingValue = builder
            .comment("Padding of the trainer card gui.")
            .defineInRange("trainerCardPadding", IClientConfig.super.trainerCardPadding(), 0, Integer.MAX_VALUE - 1);

        this.trainerCardAlignmentXValue = builder
            .comment("Horizontal alignment of the trainer card gui, i.e. 0=left, 0.5=center, 1=right.")
            .defineInRange("trainerCardAlignmentX", IClientConfig.super.trainerCardAlignmentX(), 0, 1);

        this.trainerCardAlignmentYValue = builder
            .comment("Vertical alignment of the trainer card gui, i.e. 0=top, 0.5=center, 1=bottom.")
            .defineInRange("trainerCardAlignmentY", IClientConfig.super.trainerCardAlignmentY(), 0, 1);

        this.spec = builder.build();
    }

    @Override
    public ModConfigSpec getSpec() {
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

    @Override
    public int trainerCardPadding() {
        return this.trainerCardPaddingValue.get();
    }

    @Override
    public float trainerCardAlignmentX() {
        return this.trainerCardAlignmentXValue.get().floatValue();
    }

    @Override
    public float trainerCardAlignmentY() {
        return this.trainerCardAlignmentYValue.get().floatValue();
    }
}
