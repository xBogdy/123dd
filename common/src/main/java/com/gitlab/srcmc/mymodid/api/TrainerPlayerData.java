package com.gitlab.srcmc.mymodid.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class TrainerPlayerData extends SavedData {
    public static final int MAX_LEVEL_CAP = 100;
    public static final int MAX_BADGES = 8;
    public static final int MAX_BEATEN_E4 = 4;
    public static final int MAX_BEATEN_CHAMPS = 1;

    private UUID playerUUID;
    private int levelCap;
    private int badges;
    private int beatenE4;
    private int beatenChamps;
    private Set<Integer> activeTrainers = new HashSet<>();

    public static TrainerPlayerData of(CompoundTag tag) {
        var tpd = new TrainerPlayerData();
        tpd.playerUUID = tag.getUUID("playerUUID");
        tpd.levelCap = tag.getInt("levelCap");
        tpd.badges = tag.getInt("badges");
        tpd.beatenE4 = tag.getInt("beatenE4");
        tpd.beatenChamps = tag.getInt("beatenChamps");
        tpd.activeTrainers = Arrays.stream(tag.getIntArray("activeTrainers")).boxed().collect(Collectors.toSet());
        return tpd;
    }

    public TrainerPlayerData(Player player) {
        this.playerUUID = player.getUUID();
    }

    private TrainerPlayerData() {
        // default data not associated to any player yet
    }

    public int getLevelCap() {
        return this.levelCap;
    }

    public int getBadges() {
        return this.badges;
    }

    public int getBeatenE4() {
        return this.beatenE4;
    }

    public int getBeatenChamps() {
        return this.beatenChamps;
    }

    public void setLevelCap(int levelCap) {
        if(this.levelCap != levelCap) {
            this.levelCap = levelCap;
            setDirty();
        }
    }

    public void setBadges(int badges) {
        if(this.badges != badges) {
            this.badges = badges;
            setDirty();
        }
    }

    public void setBeatenE4(int beatenE4) {
        if(this.beatenE4 != beatenE4) {
            this.beatenE4 = beatenE4;
            setDirty();
        }
    }

    public void setBeatenChamps(int beatenChamps) {
        if(this.beatenChamps != beatenChamps) {
            this.beatenChamps = beatenChamps;
            setDirty();
        }
    }

    public void addBadge() {
        if(this.badges < MAX_BADGES) {
            this.badges++;
            this.setDirty();
        }
    }

    public void addBeatenE4() {
        if(this.beatenE4 < MAX_BEATEN_E4) {
            this.beatenE4++;
            this.setDirty();
        }
    }

    public void addBeatenChamps() {
        if(this.beatenChamps < MAX_BEATEN_CHAMPS) {
            this.beatenChamps++;
            this.setDirty();
        }
    }

    public boolean isActiveTrainer(int entityId) {
        return this.activeTrainers.contains(entityId);
    }

    public List<Integer> getActiveTrainers() {
        return List.copyOf(this.activeTrainers);
    }

    public boolean addActiveTrainer(int entityId) {
        var added = this.activeTrainers.add(entityId);

        if(added) {
            this.setDirty();
        }

        return added;
    }

    public boolean removeActiveTrainer(int entityId) {
        var removed = this.activeTrainers.remove(entityId);

        if(removed) {
            this.setDirty();
        }

        return removed;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putUUID("playerUUID", this.playerUUID);
        compoundTag.putInt("levelCap", this.levelCap);
        compoundTag.putInt("badges", this.badges);
        compoundTag.putInt("beatenE4", this.beatenE4);
        compoundTag.putInt("beatenChamps", this.beatenChamps);
        compoundTag.putIntArray("activeTrainer", List.copyOf(this.activeTrainers));
        return compoundTag;
    }
}
