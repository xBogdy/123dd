package com.gitlab.srcmc.mymodid.api.config;

// TODO: DRAFT
public interface IModConfig {
    // levels
    boolean levelCapLimitsExp();

    // trainers
    boolean weakDontFight();

    // spawning
    int maxTrainersPerPlayer();
    int maxTrainersTotal();
    int spawnIntervalTicks();
    int despawnDelay(); // -1 => no despawn until beaten
}
