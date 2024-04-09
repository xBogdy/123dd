package com.gitlab.srcmc.mymodid.api.data.save;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class TrainerBattleMemory extends SavedData {
    private Map<UUID, Integer> defeatedBy = new HashMap<>();

    public static TrainerBattleMemory of(CompoundTag tag) {
        var tbm = new TrainerBattleMemory();
        tag.getAllKeys().forEach(key -> tbm.defeatedBy.put(UUID.fromString(key), tag.getInt(key)));
        return tbm;
    }

    public static String filePath(TrainerMob mob) {
        return String.format("%s.trainers.%s.mem", ModCommon.MOD_ID, mob.getTrainerId());
    }

    public void addDefeatedBy(Player player) {
        var count = this.defeatedBy.get(player.getUUID());

        if(count == null) {
            count = 0;
        }

        if(count < Integer.MAX_VALUE) {
            this.defeatedBy.put(player.getUUID(), count + 1);
            this.setDirty();
        }
    }

    public int getDefeatByCount(Player player) {
        var count = this.defeatedBy.get(player.getUUID());
        return count == null ? 0 : count;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        this.defeatedBy.forEach((uuid, count) -> compoundTag.putInt(uuid.toString(), count));
        return compoundTag;
    }
}
