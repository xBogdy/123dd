/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2024, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctmod.api.data;

import java.util.List;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;

import net.minecraft.world.entity.player.Player;

public class TrainerBattle {
    private List<Player> initiatorSidePlayers;
    private List<TrainerMob> initiatorSideMobs;
    private List<Player> trainerSidePlayers;
    private List<TrainerMob> trainerSideMobs;

    public TrainerBattle(Player initiator, TrainerMob opponent) {
        this(new Player[]{initiator}, new TrainerMob[]{}, new Player[]{}, new TrainerMob[]{opponent});
    }

    public TrainerBattle(Player[] initiatorSidePlayers, TrainerMob[] initiatorSideMobs, Player[] trainerSidePlayers, TrainerMob[] trainerSideMobs) {
        if(initiatorSidePlayers.length == 0) {
            throw new UnsupportedOperationException("battle must have atleast 1 initiator player");
        }

        if(trainerSidePlayers.length == 0 && trainerSideMobs.length == 0) {
            throw new UnsupportedOperationException("battle must have atleast 1 trainer mob or player opponent");
        }

        this.initiatorSidePlayers = List.of(initiatorSidePlayers);
        this.initiatorSideMobs = List.of(initiatorSideMobs);
        this.trainerSidePlayers = List.of(trainerSidePlayers);
        this.trainerSideMobs = List.of(trainerSideMobs);
    }

    public Player getInitiator() {
        return this.initiatorSidePlayers.get(0);
    }

    public void distributeRewards(boolean initiatorWins) {
        var winnerPlayers = initiatorWins ? initiatorSidePlayers : trainerSidePlayers;
        var looserMobs = initiatorWins ? trainerSideMobs : initiatorSideMobs;
        var tm = RCTMod.get().getTrainerManager();

        for(var player : winnerPlayers) {
            for(var mob : looserMobs) {
                var mobTr = tm.getData(mob);
                var playerTr = tm.getData(player);
                var battleMem = tm.getBattleMemory(mob);

                if(battleMem.getDefeatByCount(player) == 0) {
                    // TODO: advancement trigger here (player.awardStat())
                    switch (mobTr.getType()) {
                        case LEADER:
                            playerTr.addBadge();
                            break;
                        case E4:
                            playerTr.addBeatenE4();
                            break;
                        case CHAMP:
                            playerTr.addBeatenChamp();
                            break;
                        default:
                            break;
                    }
                }

                playerTr.setLevelCap(Math.max(mobTr.getRewardLevelCap(), playerTr.getLevelCap()));
                battleMem.addDefeatedBy(player);
                mob.finishBattle(true);
            }
        }

        for(var mob : initiatorWins ? initiatorSideMobs : trainerSideMobs) {
            mob.finishBattle(false);
        }
    }
}
