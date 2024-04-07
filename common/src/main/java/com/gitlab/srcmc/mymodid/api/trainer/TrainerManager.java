package com.gitlab.srcmc.mymodid.api.trainer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.api.utils.PathUtils;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

public class TrainerManager extends SimpleJsonResourceReloadListener {
    private static final String PATH_MOBS_GROUPS = "mobs/trainers/groups";
    private static final String PATH_MOBS_SINGLE = "mobs/trainers/single";
    private static final String PATH_TRAINERS = "trainers";
    private final static Gson GSON = new Gson();

    private Map<ResourceLocation, TrainerMobData> trainerGroups = new HashMap<>();
    private Map<String, TrainerMobData> trainerMobs = new HashMap<>();
    private Map<UUID, TrainerBattle> trainerBattles = new HashMap<>();
    private PackResources dataPack;

    /**
     * Constructs a new trainer manager. Be sure to register this instance as
     * ReloadListener and invoke registerTrainers before usage.
     */
    public TrainerManager() {
        super(GSON, ModCommon.MOD_ID);
    }

    /**
     * Invokes the given consumer for all trainer teams. Can be used to register
     * trainers to other registries (e.g. from CobblemonTrainers).
     * 
     * @param trainerConsumer Consumer invoked for each trainer team.
     */
    public void registerTrainers(ResourceOutput trainerConsumer) {
        if(this.dataPack != null) {
            this.dataPack.listResources(
                PackType.SERVER_DATA,
                ModCommon.MOD_ID, PATH_TRAINERS,
                trainerConsumer);

            this.dataPack.close();
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

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.load(resourceManager);
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

    private void initDataPack(ResourceManager resourceManager) {
        var it = resourceManager.listPacks().iterator();

        while(it.hasNext()) {
            var dp = it.next();

            if(dp.packId().equals("main")) {
                this.dataPack = dp;
                return;
            }
        }

        throw new IllegalStateException("missing internal data pack");
    }

    private void load(ResourceManager resourceManager) {
        this.initDataPack(resourceManager);
        this.trainerMobs.clear();
        this.loadTrainerGroups();
        this.loadTrainerMobs();
        this.trainerGroups.clear();
        this.dataPack.close();
    }

    private void loadTrainerGroups() {
        this.dataPack.listResources(
            PackType.SERVER_DATA,
            ModCommon.MOD_ID, PATH_MOBS_GROUPS,
            this::loadTrainerGroup);
    }

    private void loadTrainerGroup(ResourceLocation rl, IoSupplier<InputStream> io) {
        trainerGroups.put(rl, TrainerMobData.loadFromOrThrow(rl, io));
    }

    private void loadTrainerMobs() {
        this.dataPack.listResources(
            PackType.SERVER_DATA,
            ModCommon.MOD_ID, PATH_TRAINERS,
            this::loadTrainerMob);
    }

    private void loadTrainerMob(ResourceLocation rl, IoSupplier<InputStream> io) {
        var trainerId = PathUtils.filename(rl.getPath());
        var mobResource = new ResourceLocation(PATH_MOBS_SINGLE + "/" + trainerId + ".json");
        var tmd = TrainerMobData.loadFromOrFallback(this.dataPack, mobResource, this.dataPack.getResource(PackType.SERVER_DATA, mobResource), trainerGroups);
        tmd.setTeam(TrainerTeam.loadFromOrThrow(rl, io));
        trainerMobs.put(trainerId, tmd);
    }
}
