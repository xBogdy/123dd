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
package com.gitlab.srcmc.rctmod.forge.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.gitlab.srcmc.rctmod.api.utils.PathUtils;
import com.google.gson.JsonParser;
import com.selfdot.cobblemontrainers.CobblemonTrainers;
import com.selfdot.cobblemontrainers.trainer.Trainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;

public class VolatileTrainer extends Trainer {
	public VolatileTrainer(ResourceLocation location, IoSupplier<InputStream> io) {
		super(CobblemonTrainers.INSTANCE, PathUtils.filename(location.getPath()), "Radical");

        try(var rd = new BufferedReader(new InputStreamReader(io.get()))) {
			this.loadFromJson(JsonParser.parseReader(rd));
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
	}

	@Override
	public void addSpecies(Species species) {
	}

	@Override
	public void setName(String name) {
	}

	@Override
	public void setWinCommand(String winCommand) {
	}

	@Override
	public void setGroup(String group) {
	}

	@Override
	public void setLossCommand(String lossCommand) {
	}

	@Override
	public void setCanOnlyBeatOnce(boolean canOnlyBeatOnce) {
	}

	@Override
	public void setCooldownSeconds(long cooldownSeconds) {
	}

	@Override
	public void addPokemon(Pokemon pokemon) {
	}

	@Override
	public void save() {
	}

	@Override
	public void load() {
	}

	@Override
	public void updateLocation(String oldLocation) {
	}
}
