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
package com.gitlab.srcmc.rctmod.fabric.world.trainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.utils.PathUtils;
import com.google.gson.JsonParser;
import com.selfdot.cobblemontrainers.CobblemonTrainers;
import com.selfdot.cobblemontrainers.trainer.Trainer;
import com.selfdot.cobblemontrainers.trainer.TrainerPokemon;

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
	public void addSpecies(Species species, Set<String> aspects) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void setName(String name) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void setWinCommand(String winCommand) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void setGroup(String group) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void setLossCommand(String lossCommand) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void setCanOnlyBeatOnce(boolean canOnlyBeatOnce) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void setCooldownSeconds(long cooldownSeconds) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void addPokemon(Pokemon pokemon) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void updateLocation(String oldLocation) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void addDefeatRequirement(String defeatRequirement) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public boolean removeDefeatRequirement(String defeatRequirement) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
		return false;
	}

	@Override
	public void removeTrainerPokemon(TrainerPokemon trainerPokemon) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void setPartyMaximumLevel(int partyMaximumLevel) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void swap(int i, int j) {
		ModCommon.LOG.error("Trainers from this mod may not be modified! A data pack can be used to overwrite trainers.");
	}

	@Override
	public void save() {
	}

	@Override
	public void load() {
	}
}
