package com.gitlab.srcmc.mymodid.api.trainer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.api.utils.JsonUtils;
import com.gitlab.srcmc.mymodid.api.utils.PathUtils;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

public class TrainerManager {
    private Map<ResourceLocation, TrainerMobData> trainerGroups = new HashMap<>();
    private Map<String, TrainerMobData> trainerMobs = new HashMap<>();
    private Map<UUID, TrainerBattle> trainerBattles = new HashMap<>();

    private static final String PATH_MOBS_GROUPS = "mobs/trainers/groups";
    private static final String PATH_MOBS_SINGLE = "mobs/trainers/single";
    private static final String PATH_TRAINERS = "trainers";

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
            ModCommon.LOG.info("- [" + kv.getKey() + "]");
            ModCommon.LOG.info(JsonUtils.toJson(kv.getValue()));
        }

        ModCommon.LOG.info("TRAINER MOBS:");
        for(var kv : trainerMobs.entrySet()) {
            ModCommon.LOG.info("- [" + kv.getKey() + "]");
            ModCommon.LOG.info(kv.getValue().getTextureResource().getPath());
            ModCommon.LOG.info(JsonUtils.toJson(kv.getValue()));
        }
    }

    public void addBattle(Player initiator, TrainerMob opponent) {
        trainerBattles.put(initiator.getUUID(), new TrainerBattle(initiator, opponent));
    }

    public void addBattle(Player[] initiatorSidePlayers, TrainerMob[] initiatorSideMobs, Player[] trainerSidePlayers, TrainerMob[] trainerSideMobs) {
        var battle = new TrainerBattle(initiatorSidePlayers, initiatorSideMobs, trainerSidePlayers, trainerSideMobs);
        trainerBattles.put(battle.getInitiator().getUUID(), battle);
    }

    public Optional<TrainerBattle> getBattle(UUID initiatorId) {
        return trainerBattles.containsKey(initiatorId)
            ? Optional.of(trainerBattles.get(initiatorId))
            : Optional.empty();
    }

    public boolean removeBattle(UUID initiatorId) {
        return trainerBattles.remove(initiatorId) != null;
    }

    public TrainerMobData getData(TrainerMob mob) {
        if(!trainerMobs.containsKey(mob.getTrainerId())) {
            if(!mob.getTrainerId().isEmpty()) {
                ModCommon.LOG.error(String.format(
                    "Invalid trainer id '%s' for mob: %s",
                    mob.getTrainerId(), mob.getDisplayName().getString()));
            }

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
            .listResources(PATH_MOBS_GROUPS, rl -> rl.getPath().toLowerCase().endsWith(".json"))
            .forEach(this::loadTrainerGroup);
    }

    private void loadTrainerGroup(ResourceLocation rl, Resource rs) {
        trainerGroups.put(rl, TrainerMobData.loadFromOrThrow(rl));
    }

    private void loadTrainerMobs() {
        Minecraft.getInstance().getResourceManager()
            .listResources(PATH_TRAINERS, rl -> rl.getPath().toLowerCase().endsWith(".json"))
            .forEach(this::loadTrainerMob);
    }

    private void loadTrainerMob(ResourceLocation rl, Resource rs) {
        var trainerId = PathUtils.filename(rl.getPath());
        var mobResource = new ResourceLocation(ModCommon.MOD_ID, PATH_MOBS_SINGLE + "/" + trainerId + ".json");
        var tmd = TrainerMobData.loadFromOrFallback(mobResource, trainerGroups);
        tmd.setTeam(TrainerTeam.loadFromOrThrow(rl));
        trainerMobs.put(trainerId, tmd);
    }
}
