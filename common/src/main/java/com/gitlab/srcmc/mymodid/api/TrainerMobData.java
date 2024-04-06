package com.gitlab.srcmc.mymodid.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;

public class TrainerMobData {
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

    private static final String PATH_MOB_DEFAULT = "mobs/trainers/default.json";

    public enum Type {
        NORMAL, BOSS, LEADER, E4, CHAMP, RIVAL, PROF, PLAYER
    }

    private Type type = Type.NORMAL;
    private int rewardLevelCap;
    private int requiredBadges;
    private int requiredBeatenE4;
    private int requiredBeatenChamps;

    private int maxPlayerWins = 1;
    private int maxPlayerLosses = 2;
    private int battleCooldownTicks = 2000;

    private Set<String> biomeWhitelist = new HashSet<>();
    private Set<String> biomeBlacklist = new HashSet<>();
    private Set<String> dimensionWhitelist = new HashSet<>();
    private Set<String> dimensionBlacklist = new HashSet<>();

    private transient Map<String, String[]> dialog = new HashMap<>();
    private transient ResourceLocation textureResource;
    private transient ResourceLocation lootTableResource;
    private transient TrainerTeam team;

    public static TrainerMobData loadFromOrThrow(ResourceLocation location) {
        var tmd = JsonUtils.loadFromOrThrow(location, TrainerMobData.class);
        tmd.initResources(location);
        return tmd;
    }

    public static TrainerMobData loadFromOrFallback(ResourceLocation location, Map<ResourceLocation, TrainerMobData> groups) {
        var rm = Minecraft.getInstance().getResourceManager();
        var trainerId = PathUtils.filename(location.getPath());
        var defaultLocation = new ResourceLocation(ModCommon.MOD_ID, PATH_MOB_DEFAULT);
        ResourceLocation groupLocation = null;
        TrainerMobData tmd = null;

        if(rm.getResource(location).isPresent()) {
            tmd = JsonUtils.loadFromOrThrow(location, TrainerMobData.class);
        }

        for(var groupEntry : groups.entrySet()) {
            var groupId = PathUtils.filename(groupEntry.getKey().getPath());

            if(trainerId.equals(groupId) || trainerId.contains("_" + groupId) || trainerId.contains(groupId + "_")) {
                if(tmd == null) {
                    tmd = new TrainerMobData(groupEntry.getValue());
                }

                groupLocation = groupEntry.getKey();
                break;
            }
        }

        if(tmd == null) {
            tmd = JsonUtils.loadFromOrThrow(defaultLocation, TrainerMobData.class);
        }

        tmd.initResources(defaultLocation);

        if(groupLocation != null) {
            tmd.initResources(groupLocation);
        }

        tmd.initResources(location);
        
        return tmd;
    }

    public TrainerMobData() {
        this.team = new TrainerTeam();
        this.textureResource = new ResourceLocation(ModCommon.MOD_ID, PATH_MOB_DEFAULT.replaceFirst("mobs/", "textures/").replace(".json", ".png"));
        this.lootTableResource = new ResourceLocation(ModCommon.MOD_ID, PATH_MOB_DEFAULT.replaceFirst("mobs/", "").replace(".json", "")); // "loot_tables/" is inferred from context
    }

    public TrainerMobData(TrainerMobData origin) {
        this.type = origin.type;
        this.rewardLevelCap = origin.rewardLevelCap;
        this.requiredBadges = origin.requiredBadges;
        this.requiredBeatenE4 = origin.requiredBeatenE4;
        this.requiredBeatenChamps = origin.requiredBeatenChamps;
        this.maxPlayerWins = origin.maxPlayerWins;
        this.maxPlayerLosses = origin.maxPlayerLosses;
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

    public int getMaxPlayerWins() {
        return this.maxPlayerWins;
    }

    public int getMaxPlayerLosses() {
        return this.maxPlayerLosses;
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

    public void setTeam(TrainerTeam team) {
        this.team = team;
    }

    public boolean canSpawnIn(DimensionType dimension, Holder<Biome> biome) {
        // TODO
        return false;
    }

    private void initResources(ResourceLocation mobLocation) {
        var textureResource = new ResourceLocation(ModCommon.MOD_ID, mobLocation.getPath().replaceFirst("mobs", "textures").replace(".json", ".png"));
        var lootTableResource = new ResourceLocation(ModCommon.MOD_ID, mobLocation.getPath().replaceFirst("mobs", "loot_tables"));
        var dialogResource = new ResourceLocation(ModCommon.MOD_ID, mobLocation.getPath().replaceFirst("mobs", "dialogs"));
        var rm = Minecraft.getInstance().getResourceManager();

        if(rm.getResource(textureResource).isPresent()) {
            this.textureResource = textureResource;
        }

        if(rm.getResource(lootTableResource).isPresent()) {
            this.lootTableResource = lootTableResource;
        }

        ModCommon.LOG.info("LOADING DIALOG FROM: " + dialogResource.getPath());
        if(rm.getResource(dialogResource).isPresent()) {
            ModCommon.LOG.info("SUCCESS");
            var dialog = JsonUtils.loadFromOrThrow(dialogResource, new TypeToken<Map<String, String[]>>() {});
            this.dialog = dialog != null ? dialog : new HashMap<>();
        } else ModCommon.LOG.info("FAILURE");
    }
}
