package com.gitlab.srcmc.mymodid.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gitlab.srcmc.mymodid.api.utils.JsonUtils;

import net.minecraft.resources.ResourceLocation;

public class TrainerTeam {
    public class Pokemon {
        public enum Gender {
            NONE, MALE, FEMALE
        };

        public class Stats {
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
        private String heldItem = "";
        
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

    private String displayName = "Trainer";
    private List<Pokemon> team = new ArrayList<>();
    private transient ResourceLocation resourceLocation;

    public static TrainerTeam loadFromOrThrow(ResourceLocation rl) {
        var tt = JsonUtils.loadFromOrThrow(rl, TrainerTeam.class);
        tt.resourceLocation = rl;
        return tt;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<Pokemon> getMembers() {
        return Collections.unmodifiableList(this.team);
    }

    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }
}
