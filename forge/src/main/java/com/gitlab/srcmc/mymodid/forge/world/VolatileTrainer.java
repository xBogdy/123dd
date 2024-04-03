package com.gitlab.srcmc.mymodid.forge.world;

import java.io.IOException;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.JsonParser;
import com.selfdot.cobblemontrainers.CobblemonTrainers;
import com.selfdot.cobblemontrainers.trainer.Trainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public class VolatileTrainer extends Trainer {
	public VolatileTrainer(ResourceLocation location, Resource resource) {
		super(CobblemonTrainers.INSTANCE, location.getPath()
			.substring(0, location.getPath().length() - 5)
			.replaceFirst(location.getNamespace(), ""), "radical");

        try(var rd = resource.openAsReader()) {
			this.loadFromJson(JsonParser.parseReader(rd));
        } catch(IOException e) {
            throw new RuntimeException(e);
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
