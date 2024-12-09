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
package com.gitlab.srcmc.rctmod.api.data.pack;

import java.util.List;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.gitlab.srcmc.rctapi.api.ai.RCTBattleAI;
import com.gitlab.srcmc.rctapi.api.battle.BattleFormat;
import com.gitlab.srcmc.rctapi.api.battle.BattleRules;
import com.gitlab.srcmc.rctapi.api.models.BagItemModel;
import com.gitlab.srcmc.rctapi.api.models.PokemonModel;
import com.gitlab.srcmc.rctapi.api.models.TrainerModel;
import com.gitlab.srcmc.rctapi.api.util.JTO;

public class TrainerTeam extends TrainerModel {
    private String identity;
    private BattleFormat battleFormat;
    private BattleRules battleRules;

    public TrainerTeam() {
        this(null, "Trainer", BattleFormat.GEN_9_SINGLES, new BattleRules(), JTO.of(RCTBattleAI::new), List.of(), List.of());
    }

    public TrainerTeam(String identity, String name, BattleFormat battleFormat, BattleRules battleRules, JTO<BattleAI> ai, List<BagItemModel> bag, List<PokemonModel> team) {
        super(name, ai, bag, team);
        this.identity = identity;
        this.battleFormat = battleFormat;
        this.battleRules = battleRules;
    }

    public String getIdentity() {
        return this.identity != null ? this.identity : this.getName();
    }

    public BattleFormat getBattleFormat() {
        return this.battleFormat;
    }

    public BattleRules getBattleRules() {
        return this.battleRules;
    }
}
