/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2025, HDainester, All rights reserved.
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gitlab.srcmc.rctmod.api.utils.JsonUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;

public class TrainerTeam {
    public static class Pokemon {
        public enum Gender {
            NONE, MALE, FEMALE
        };

        public static class Stats {
            private int hp;
            private int attack;
            private int defence;
            private int special_attack;
            private int special_defence;
            private int speed;

            public int getHp() { return this.hp; }
            public int getAttack() { return this.attack; }
            public int getDefence() { return this.defence; }
            public int getSpecialAttack() { return this.special_attack; }
            public int getSpecialDefence() { return this.special_defence; }
            public int getSpeed() { return this.speed; }
        }

        private String species = "";
        private Gender gender = Gender.NONE;
        private int level = 0;
        private String nature = "";
        private String ability = "";
        private List<String> moveset = new ArrayList<>();
        private Stats ivs = new Stats();
        private Stats evs = new Stats();
        private boolean shiny = false;
        private String heldItem = "minecraft:air";

        public String getSpecies() { return this.species; }
        public Gender getGender() { return this.gender; }
        public int getLevel() { return this.level; }
        public String getNature() { return this.nature; }
        public String getAbility() { return this.ability; }
        public List<String> getMoveset() { return Collections.unmodifiableList(this.moveset); }
        public Stats getIVs() { return this.ivs; }
        public Stats getEVs() { return this.evs; }
        public boolean getShiny() { return this.shiny; }
        public String getHeldItem() { return this.heldItem; }
    }

    private String displayName = "Trainer", identity;
    private List<Pokemon> team = new ArrayList<>();
    private transient ResourceLocation resourceLocation;

    public static TrainerTeam loadFromOrThrow(ResourceLocation rl, IoSupplier<InputStream> io) {
        var tt = JsonUtils.loadFromOrThrow(io, TrainerTeam.class);
        tt.resourceLocation = rl;
        return tt;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getIdentity() {
        return this.identity != null ? this.identity : this.displayName;
    }

    public List<Pokemon> getMembers() {
        return Collections.unmodifiableList(this.team);
    }

    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }
}
