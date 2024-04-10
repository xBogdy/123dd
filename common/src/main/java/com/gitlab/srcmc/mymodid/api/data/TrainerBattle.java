package com.gitlab.srcmc.mymodid.api.data;

import java.util.List;
import com.gitlab.srcmc.mymodid.api.RCTMod;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;

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
