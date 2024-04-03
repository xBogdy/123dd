package com.gitlab.srcmc.mymodid.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

// TODO: DRAFT
public class TrainerSpawner {
    private final int MIN_DISTANCE_TO_PLAYERS = 30;
    private final int MAX_DISTANCE_TO_PLAYERS = 80;
    private final int MAX_VERTICAL_DISTAINCE_TO_PLAYERS = 30;
    private final int SPAWN_TICK_COOLDOWN = 20;
    private final int MAX_TRAINERS_PER_PLAYER = 20;
    private final int SPAWN_ATTEMPS = 3;

    private int trainers;
    private Player player;
    TrainerManager tm;

    public void tick() {
        if(player.tickCount % SPAWN_TICK_COOLDOWN == 0 && trainers < MAX_TRAINERS_PER_PLAYER) {
            for(int i = 0; i < SPAWN_ATTEMPS; i++) {
                if(attemptSpawn()) {
                    break;
                }
            }
        }
    }

    private boolean attemptSpawn() {
        var level = player.level();
        var pos = nextPos();

        if(pos == null) {
            return false;
        }

        if(level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), MIN_DISTANCE_TO_PLAYERS, false) != null) {
            return false;
        }

        var biome = level.getBiome(pos);

        return true;
    }

    private BlockPos nextPos() {
        var level = player.level();
        var rng = player.getRandom();
        int d = MAX_DISTANCE_TO_PLAYERS - MIN_DISTANCE_TO_PLAYERS;
        int dx = (MIN_DISTANCE_TO_PLAYERS + (d % rng.nextInt())) * (rng.nextInt() % 2 == 0 ? 1 : -1);
        int dz = (MIN_DISTANCE_TO_PLAYERS + (d % rng.nextInt())) * (rng.nextInt() % 2 == 0 ? 1 : -1);
        int dy = MAX_VERTICAL_DISTAINCE_TO_PLAYERS;

        int x = player.getBlockX() + dx;
        int z = player.getBlockZ() + dz;
        int y = player.getBlockY() + dy;
        int air = 0;
        
        for(int i = dy; i >= -dy; i--) {
            var pos = new BlockPos(x, y + i, z);

            if(level.getBlockState(pos).isAir()) {
                air++;
            } else {
                if(air > 1) {
                    return pos;
                } else {
                    air = 0;
                }
            }
        }

        return null;
    }
}
