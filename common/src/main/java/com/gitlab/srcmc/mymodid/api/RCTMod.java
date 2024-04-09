package com.gitlab.srcmc.mymodid.api;

import java.util.function.Supplier;

import com.gitlab.srcmc.mymodid.api.data.pack.DataPackManager;
import com.gitlab.srcmc.mymodid.api.service.TrainerManager;
import com.gitlab.srcmc.mymodid.world.loot.conditions.LevelRangeCondition;

import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class RCTMod {
    private TrainerManager trainerManager;
    private DataPackManager dataPackManager;

    private static Supplier<RCTMod> instance = () -> {
        throw new RuntimeException(RCTMod.class.getName() + " not initialized");
    };

    public static RCTMod get() {
        return instance.get();
    }

    public static void init(Supplier<LootItemConditionType> levelRangeConditon) {
        var local = new RCTMod();
        instance = () -> local;
        LevelRangeCondition.init(levelRangeConditon);
    }

    private RCTMod() {
        this.trainerManager = new TrainerManager();
        this.dataPackManager = new DataPackManager();
    }

    public TrainerManager getTrainerManager() {
        return this.trainerManager;
    }

    public DataPackManager getDataPackManager() {
        return this.dataPackManager;
    }
}
