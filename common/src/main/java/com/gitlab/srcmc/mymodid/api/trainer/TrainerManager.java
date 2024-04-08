package com.gitlab.srcmc.mymodid.api.trainer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.api.RCTMod;
import com.gitlab.srcmc.mymodid.api.utils.PathUtils;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

public class TrainerManager extends SimpleJsonResourceReloadListener {
    private final static Gson GSON = new Gson();

    private Map<String, TrainerMobData> trainerMobs = new HashMap<>();
    private Map<UUID, TrainerBattle> trainerBattles = new HashMap<>();

    public TrainerManager() {
        super(GSON, ModCommon.MOD_ID);
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

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        var dpm = RCTMod.get().getDataPackManager();
        dpm.init(resourceManager);

        dpm.listTrainerTeams((rl, io) -> {
            var trainerId = PathUtils.filename(rl.getPath());
            dpm.loadResource(trainerId, "mobs", tdm -> this.trainerMobs.put(trainerId, tdm), TrainerMobData.class);
        });

        dpm.close();
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
}
