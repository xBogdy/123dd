package com.gitlab.srcmc.mymodid.api.trainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.api.data.DataPackManager;
import com.gitlab.srcmc.mymodid.api.data.IDataPackObject;
import com.google.gson.reflect.TypeToken;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;

public class TrainerMobData implements IDataPackObject {
    public enum Group {
        UNKNOWN,
        ACE_TRAINER,
        AROMA_LADY,
        BEAUTY,
        BIKER,
        BIRD_KEEPER,
        BLACK_BELT,
        BOSS, // stage 1
        BOSS_GIOVANNI,
        BOSS_THUG,
        BREEDER,
        BUG_CATCHER,
        BURGLAR,
        CAMPER,
        CHAMPION, // stage 13
        CHAMPION_LANCE,
        CHAMPION_TERRY,
        CHANNELER,
        COOL_COUPLE,
        CRUSH_GIRL,
        CRUSH_KIN,
        CUE_BALL,
        DUMBASS,
        ELITE_FOUR, // stage 9-12
        ELITE_FOUR_AGATHA,
        ELITE_FOUR_BRUNO,
        ELITE_FOUR_LANCE,
        ELITE_FOUR_LORELEI,
        ENGINEER,
        EXPERT,
        FISHERMAN,
        COACH,
        MANIAC,
        GAMBLER,
        GENTLEMAN,
        HIKER,
        JUGGLER,
        LADY,
        LASS,
        LEADER, // stage 1-8
        LEADER_BLAINE,
        LEADER_BROCK,
        LEADER_BUGSY,
        LEADER_CLAIR,
        LEADER_ERIKA,
        LEADER_FALKNER,
        LEADER_GIOVANNI,
        LEADER_JASMINE,
        LEADER_KOGA,
        LEADER_LT_SURGE,
        LEADER_MISTY,
        LEADER_PRYCE,
        LEADER_SABRINA,
        LEADER_WHITNEY,
        PAINTER,
        PICNICKER,
        PLAYER, // stage 14
        PLAYER_BRENDAN,
        PLAYER_LEAF,
        PLAYER_MAY,
        PLAYER_RED,
        POKEFAN,
        POKEMANIAC,
        PROF, // stage 14
        PROF_OAK,
        PSYCHIC,
        RANGER,
        RIVAL,
        ROCKER,
        RUIN_MAMOAC,
        SAILOR,
        SCHOOL_KID,
        SCIENTIST,
        SIS_AND_BRO,
        SUPER_NERD,
        SWIMMERF, // female
        SWIMMERM, // male
        TAMER,
        TEACHER,
        TEAM_ROCKET_ADMIN,
        TEAM_ROCKET_GRUNT,
        THUG,
        TRAINER,
        TUBER,
        TWINS,
        YOUNG_COUPLE,
        YOUNGSTER,
    }

    public enum Type {
        NORMAL, BOSS, LEADER, E4, CHAMP, RIVAL, PROF, PLAYER
    }

    private Type type = Type.NORMAL;
    private int rewardLevelCap;
    private int requiredBadges;
    private int requiredBeatenE4;
    private int requiredBeatenChamps;

    private int maxTrainerWins = 3;
    private int maxTrainerDefeats = 1;
    private int battleCooldownTicks = 2000;

    private Set<String> biomeWhitelist = new HashSet<>();
    private Set<String> biomeBlacklist = new HashSet<>();
    private Set<String> dimensionWhitelist = new HashSet<>();
    private Set<String> dimensionBlacklist = new HashSet<>();

    private transient Map<String, String[]> dialog = new HashMap<>();
    private transient ResourceLocation textureResource;
    private transient ResourceLocation lootTableResource;
    private transient TrainerTeam team;

    public TrainerMobData() {
        this.textureResource = new ResourceLocation(ModCommon.MOD_ID, "textures/" + DataPackManager.PATH_DEFAULT + ".png");
        this.lootTableResource = new ResourceLocation(ModCommon.MOD_ID, DataPackManager.PATH_DEFAULT);
        this.team = new TrainerTeam();
    }

    public TrainerMobData(TrainerMobData origin) {
        this.type = origin.type;
        this.rewardLevelCap = origin.rewardLevelCap;
        this.requiredBadges = origin.requiredBadges;
        this.requiredBeatenE4 = origin.requiredBeatenE4;
        this.requiredBeatenChamps = origin.requiredBeatenChamps;
        this.maxTrainerWins = origin.maxTrainerWins;
        this.maxTrainerDefeats = origin.maxTrainerDefeats;
        this.battleCooldownTicks = origin.battleCooldownTicks;
        this.biomeBlacklist = Set.copyOf(origin.biomeBlacklist);
        this.biomeWhitelist = Set.copyOf(origin.biomeBlacklist);
        this.dimensionBlacklist = Set.copyOf(origin.dimensionBlacklist);
        this.dimensionWhitelist = Set.copyOf(origin.dimensionWhitelist);
        this.dialog = Map.copyOf(origin.dialog);
        this.textureResource = origin.textureResource;
        this.lootTableResource = origin.lootTableResource;
        this.team = origin.team; // no need for deep copy since immutable
    }

    public Type getType() {
        return this.type;
    }

    public int getRewardLevelCap() {
        return this.rewardLevelCap;
    }

    public int getRequiredLevelCap() {
        return this.getTeam().getMembers().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0);
    }

    public int getRequiredBadges() {
        return this.requiredBadges;
    }

    public int getRequiredBeatenE4() {
        return this.requiredBeatenE4;
    }

    public int getRequiredBeatenChamps() {
        return this.requiredBeatenChamps;
    }

    public int getMaxTrainerWins() {
        return this.maxTrainerWins;
    }

    public int getMaxTrainerDefeats() {
        return this.maxTrainerDefeats;
    }

    public int getBattleCooldownTicks() {
        return this.battleCooldownTicks;
    }

    public Set<String> getBiomeBlacklist() {
        return Collections.unmodifiableSet(this.biomeBlacklist);
    }

    public Set<String> getBiomeWhitelist() {
        return Collections.unmodifiableSet(this.biomeWhitelist);
    }

    public Set<String> getDimensionBlacklist() {
        return Collections.unmodifiableSet(this.dimensionBlacklist);
    }

    public Set<String> getDimensionWhitelist() {
        return Collections.unmodifiableSet(this.dimensionWhitelist);
    }

    public Map<String, String[]> getDialog() {
        return Collections.unmodifiableMap(this.dialog);
    }

    public ResourceLocation getTextureResource() {
        return this.textureResource;
    }

    public ResourceLocation getLootTableResource() {
        return this.lootTableResource;
    }

    public TrainerTeam getTeam() {
        return this.team;
    }

    public boolean canSpawnIn(DimensionType dimension, Holder<Biome> biome) {
        // TODO
        return false;
    }

    @Override
    public void onLoad(DataPackManager dpm, String trainerId, String context) {
        var lootTableResource = dpm.findResource(trainerId, "loot_tables");
        var textureResource = dpm.findResource(trainerId, "textures");

        if(textureResource.isPresent()) {
            this.textureResource = textureResource.get();
        }

        if(lootTableResource.isPresent()) {
            // the loot table is loaded by net.minecraft.world.level.storage.loot.LootDataManager
            // which resolves a 'shorthand' resource location automatically.
            this.lootTableResource = new ResourceLocation(ModCommon.MOD_ID, lootTableResource.get().getPath()
                .replace("loot_tables/", "")
                .replace(".json", ""));
        }

        dpm.loadResource(trainerId, "dialogs",
            dialog -> this.dialog = dialog,
            new TypeToken<Map<String, String[]>>() {});

        if(this.dialog == null) {
            this.dialog = new HashMap<>();
        }

        var team = dpm.loadTrainerTeam(trainerId);

        if(team.isPresent()) {
            this.team = team.get();
        }
    }
}
