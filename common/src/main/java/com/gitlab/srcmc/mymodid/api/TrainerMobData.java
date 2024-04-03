package com.gitlab.srcmc.mymodid.api;

import java.util.HashSet;
import java.util.Set;

import com.gitlab.srcmc.mymodid.ModCommon;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

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

    private Set<String> biomeWhitelist = new HashSet<>();
    private Set<String> biomeBlacklist = new HashSet<>();
    private int requiredBadges = 0;
    private int requiredBeatenE4 = 0;
    private int requiredBeatenChamps = 0;

    private transient ResourceLocation textureResource;
    private transient TrainerTeam team;

    public static TrainerMobData loadFromOrThrow(ResourceLocation location) {
        var tmd = JsonUtils.loadFromOrThrow(location, TrainerMobData.class);
        var textureResource = new ResourceLocation(ModCommon.MOD_ID, "textures/" + location.getPath().replace(".json", ".png"));

        if(Minecraft.getInstance().getResourceManager().getResource(textureResource).isPresent()) {
            tmd.textureResource = textureResource;
        }

        return tmd;
    }

    public TrainerMobData() {
        this(new TrainerTeam());
    }

    public TrainerMobData(TrainerTeam team) {
        this.team = team;
        this.textureResource = new ResourceLocation(ModCommon.MOD_ID, "textures/trainers/trainer.png");
    }

    public TrainerMobData(TrainerMobData origin) {
        this.biomeBlacklist = Set.copyOf(origin.biomeBlacklist);
        this.biomeWhitelist = Set.copyOf(origin.biomeBlacklist);
        this.requiredBadges = origin.requiredBadges;
        this.requiredBeatenE4 = origin.requiredBeatenE4;
        this.requiredBeatenChamps = origin.requiredBeatenChamps;
        this.textureResource = origin.textureResource;
        this.team = origin.team;
    }

    public Set<String> getBiomeWhitelist() {
        return Set.copyOf(this.biomeWhitelist);
    }

    public Set<String> getBiomeBlacklist() {
        return Set.copyOf(this.biomeBlacklist);
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

    public ResourceLocation getTextureResource() {
        return this.textureResource;
    }

    public TrainerTeam getTeam() {
        return this.team;
    }

    public void setTeam(TrainerTeam team) {
        this.team = team;
    }

    public boolean canSpawnIn(Holder<Biome> biome) {
        return false;
    }
}
