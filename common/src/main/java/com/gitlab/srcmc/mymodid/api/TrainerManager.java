package com.gitlab.srcmc.mymodid.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

public class TrainerManager {
    private Map<String, TrainerMobData> trainerGroups = new HashMap<>();
    private Map<String, TrainerMobData> trainerMobs = new HashMap<>();

    public void load() {
        trainerMobs.clear();
        loadTrainerGroups();
        loadTrainerMobs();
        dump();
        trainerGroups.clear();
    }

    // TODO: remove debug function
    private void dump() {
        ModCommon.LOG.info("TRAINER GROUPS:");
        for(var kv : trainerGroups.entrySet()) {
            ModCommon.LOG.info(kv.getKey() + ": " + kv.getValue().getTextureResource().getPath());
        }

        ModCommon.LOG.info("TRAINER MOBS:");
        for(var kv : trainerMobs.entrySet()) {
            ModCommon.LOG.info(kv.getKey() + ": " + kv.getValue().getTextureResource().getPath());
            ModCommon.LOG.info(" TEAM: " + kv.getValue().getTeam().getDisplayName());

            for(var p : kv.getValue().getTeam().getMembers()) {
                ModCommon.LOG.info("  poke: " + p.getSpecies());
            }
        }
    }

    public TrainerMobData getData(TrainerMob mob) {
        if(!trainerMobs.containsKey(mob.getTrainerId())) {
            ModCommon.LOG.error(String.format("Invalid trainer id '%s' for mob: %s", mob.getTrainerId(), mob.getDisplayName().getString()));
            return new TrainerMobData();
        }

        return trainerMobs.get(mob.getTrainerId());
    }

    public TrainerPlayerData getData(Player player) {
        var level = player.getServer().overworld();

        return level.getDataStorage().computeIfAbsent(
            TrainerPlayerData::of,
            () -> new TrainerPlayerData(player),
            trainerPlayerDataFile(player));
    }

    private String trainerPlayerDataFile(Player player) {
        return String.format("%s.trainer.%s", ModCommon.MOD_ID, player.getUUID().toString());
    }

    // TODO: DRAFT
    private Set<String> getSpawnableTrainerMobs(Player player, Holder<Biome> biome) {
        Set<String> result = new HashSet<>();

        for(var kv : trainerMobs.entrySet()) {
        }

        return result;
    }

    private void loadTrainerGroups() {
        Minecraft.getInstance().getResourceManager()
            .listResources("trainers/groups", rl -> rl.getPath().toLowerCase().endsWith(".json"))
            .forEach(this::loadTrainerGroup);
    }

    private void loadTrainerGroup(ResourceLocation rl, Resource rs) {
        var groupId = rl.getPath().substring(0, rl.getPath().length() - 5).replace("trainers/groups/", "");
        trainerGroups.put(groupId, TrainerMobData.loadFromOrThrow(rl));
    }

    private void loadTrainerMobs() {
        Minecraft.getInstance().getResourceManager()
            .listResources("trainers/teams", rl -> rl.getPath().toLowerCase().endsWith(".json"))
            .forEach(this::loadTrainerMob);
    }

    private void loadTrainerMob(ResourceLocation rl, Resource rs) {
        var rm = Minecraft.getInstance().getResourceManager();
        var team = TrainerTeam.loadFromOrThrow(rl);
        var trainerId = rl.getPath().substring(0, rl.getPath().length() - 5).replace("trainers/teams/", "");
        var mobRl = new ResourceLocation(ModCommon.MOD_ID, "trainers/mobs/" + trainerId + ".json");

        if(rm.getResource(mobRl).isPresent()) {
            var tmd = TrainerMobData.loadFromOrThrow(mobRl);
            trainerMobs.put(trainerId, tmd);
            tmd.setTeam(team);
        } else {
            for(var groupId : trainerGroups.keySet()) {
                if(trainerId.equals(groupId) || trainerId.contains("_" + groupId) || trainerId.contains(groupId + "_")) {
                    var tmd = new TrainerMobData(trainerGroups.get(groupId));
                    trainerMobs.put(trainerId, tmd);
                    tmd.setTeam(team);
                    return;
                }
            }

            trainerMobs.put(trainerId, new TrainerMobData(team)); // TODO: default trainer
        }
    }
}
