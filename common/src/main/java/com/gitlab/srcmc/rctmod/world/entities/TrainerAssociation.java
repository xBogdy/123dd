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
package com.gitlab.srcmc.rctmod.world.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries.Items;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.SeriesMetaData;
import com.gitlab.srcmc.rctmod.api.utils.ChatUtils;
import com.gitlab.srcmc.rctmod.world.entities.goals.RandomStrollThroughVillageGoal;
import com.gitlab.srcmc.rctmod.world.items.TrainerCard;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

public class TrainerAssociation extends WanderingTrader {
    public static final int SPAWN_INTERVAL_TICKS = 400;
    public static final int UPDATE_INTERVAL_TICKS = 4;
    public static final int POI_SCAN_RANGE = 48;

    private static final EntityType<TrainerAssociation> TYPE = EntityType.Builder
        .of(TrainerAssociation::new, MobCategory.MISC)
        .canSpawnFarFromPlayer()
        .sized(0.6F, 1.95F).build("trainer_association");

    public static EntityType<TrainerAssociation> getEntityType() {
        return TYPE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.5)
            .add(Attributes.FOLLOW_RANGE, 48.0)
            .add(Attributes.MAX_HEALTH, 20);
    }

    private static Set<UUID> playerSpawns = new HashSet<>();

    public static void init(Level level) {
        playerSpawns = new HashSet<>();
    }

    public static boolean trySpawnFor(Player player) {
        if(canSpawnNearby(player)) {
            if(shouldSpawnNearby(player)) {
                return spawnFor(player, false);
            }

            if(shouldSpawnFor(player)) {
                return spawnFor(player, true);
            }
        }

        return false;
    }

    public static boolean spawnFor(Player player, boolean target) {
        var pos = RCTMod.getInstance().getTrainerSpawner().nextPos(player);
        
        if(pos != null) {
            var level = player.level();
            var ta = TrainerAssociation.TYPE.create(level);

            if(target) {
                ta.setPlayerTarget(player);
            }

            ta.setPos(pos.getCenter());
            level.addFreshEntity(ta);
            return true;
        }

        return false;
    }

    protected static boolean canSpawnNearby(Player player) {
        var cfg = RCTMod.getInstance().getServerConfig();
        var dim = player.level().dimension().location().toString();

        return cfg.spawnTrainerAssociation()
            && (cfg.dimensionWhitelist().isEmpty() || cfg.dimensionWhitelist().contains(dim)) && (!cfg.dimensionBlacklist().contains(dim))
            && player.level().getNearestEntity(
                TrainerAssociation.class, TargetingConditions.forCombat(),
                player, player.getX(), player.getY(), player.getZ(),
                player.getBoundingBox().inflate(cfg.maxHorizontalDistanceToPlayers())) == null;
    }

    protected static boolean shouldSpawnNearby(Player player) {
        var poim = ((ServerLevel)player.level()).getPoiManager();
        var bell = poim.findClosestWithType(poit -> poit.is(PoiTypes.MEETING), player.blockPosition(), POI_SCAN_RANGE, Occupancy.ANY);
        var beds = poim.findAllWithType(poit -> poit.is(PoiTypes.HOME), bp -> true, player.blockPosition(), POI_SCAN_RANGE, Occupancy.IS_OCCUPIED);
        return bell.isPresent() && beds.skip(2).findFirst().isPresent();
    }

    protected static boolean shouldSpawnFor(Player player) {
        var tpd = RCTMod.getInstance().getTrainerManager().getData(player);

        return !TrainerAssociation.playerSpawns.contains(player.getUUID())
            && TrainerCard.has(player)
            && (tpd.isSeriesCompleted() || tpd.getCurrentSeries().isEmpty());
    }

    private Map.Entry<String, ItemStack> offer;
    private Player playerTarget;
    private int despawnTicks;

    public TrainerAssociation(EntityType<? extends WanderingTrader> entityType, Level level) {
        super(entityType, level);
    }

    public Player getPlayerTarget() {
        return this.playerTarget;
    }

    public void setPlayerTarget(Player player) {
        if(player != this.playerTarget) {
            if(this.playerTarget != null) {
                TrainerAssociation.playerSpawns.remove(this.playerTarget.getUUID());
            }

            this.playerTarget = player;

            if(this.playerTarget != null) {
                TrainerAssociation.playerSpawns.add(this.playerTarget.getUUID());
            }

            this.setTarget(this.playerTarget);
        }
    }

    @Override
    public void tick() {
        if(!this.level().isClientSide) {
            if(this.tickCount % UPDATE_INTERVAL_TICKS == 0) {
                RCTMod.getInstance().getTrainerSpawner().register(this);
            }
        }

        super.tick();
    }

    @Override
    public void remove(RemovalReason reason) {
        ModCommon.LOG.info("TA REMOVED: " + reason);
        RCTMod.getInstance().getTrainerSpawner().unregister(this);
        this.setPlayerTarget(null);
        super.remove(reason);
    }

    @Override
    public boolean shouldBeSaved() {
        return this.isPersistenceRequired();
    }

    public boolean shouldDespawn() {
        if(++this.despawnTicks % TrainerMob.DESPAWN_TICK_SCALE == 0) {
            if(this.level().getNearestPlayer(this, Math.max(TrainerMob.DESPAWN_DISTANCE, RCTMod.getInstance().getServerConfig().maxHorizontalDistanceToPlayers())) == null) {
                return this.despawnTicks >= TrainerMob.TICKS_TO_DESPAWN;
            }

            this.despawnTicks = 0;
        }

        return false;
    }

    @Override
    public void setPersistenceRequired() {
        ModCommon.LOG.info("PRESISTENCE REQUIRED");
        if(!this.isPersistenceRequired()) {
            super.setPersistenceRequired();
            this.setPlayerTarget(null);
            var ts = RCTMod.getInstance().getTrainerSpawner();
            ts.unregister(this);
            ts.register(this);
        }
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(5, new RandomStrollThroughVillageGoal(this, 0.35));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        var itemStack = player.getItemInHand(interactionHand);

        if(!itemStack.is(net.minecraft.world.item.Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if(!this.level().isClientSide) {
                // TODO: THIS IS A TEST
                var should = TrainerAssociation.shouldSpawnFor(player);
                ModCommon.LOG.info("SHOULD SPAWN: " + should);
                ///////////////////////

                this.updateOffersFor(player);
                this.offer = null;

                if(this.getOffers().isEmpty()) {
                    // TODO: probably some custom context, e.g. 'missing_series_completion'
                    ChatUtils.reply(this, player, "missing_beaten_champ");
                }
            }

            return super.mobInteract(player, interactionHand);
        } else {
            return super.mobInteract(player, interactionHand);
        }
    }

    public Map.Entry<String, ItemStack> takeOffer() {
        var o = this.offer;
        this.offer = null;
        return o;
    }

    public void updateOffersFor(Player player) {
        var tpd = RCTMod.getInstance().getTrainerManager().getData(player);
        var sm = RCTMod.getInstance().getSeriesManager();
        this.offers = new MerchantOffers();

        sm.getSeriesIds()
            .stream().map(sid -> Map.entry(sid, sm.getData(sid)))
            .sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
            .forEach(e -> {
                if(e.getValue().requiredSeries() != null) {
                    for(var reqs : e.getValue().requiredSeries()) {
                        var ok = false;

                        for(var s : reqs) {
                            if(tpd.getCompletedSeries().containsKey(s)) {
                                ok = true;
                                break;
                            }
                        }

                        if(!ok) {
                            return;
                        }
                    }
                }

                this.offers.add(new SeriesSwitchOffer(e.getKey(), e.getValue()));
            });

        this.stopTrading();
    }

    private class SeriesSwitchOffer extends MerchantOffer {
        private String seriesId;
        private SeriesMetaData seriesData;

        public SeriesSwitchOffer(String seriesId, SeriesMetaData seriesData) {
            super(new ItemCost(Items.TRAINER_CARD.get()), createOfferFor(TrainerAssociation.this.getTradingPlayer(), seriesId, seriesData), Integer.MAX_VALUE, Integer.MAX_VALUE, 1f);
            this.seriesId = seriesId;
            this.seriesData = seriesData;
        }

        private SeriesSwitchOffer(SeriesSwitchOffer origin) {
            this(origin.seriesId, origin.seriesData);
        }

        @Override
        public boolean satisfiedBy(ItemStack arg1, ItemStack arg2) {
            if(super.satisfiedBy(arg1, arg2)) {
                TrainerAssociation.this.offer = Map.entry(this.seriesId, arg1.copy());
                ModCommon.LOG.info("SATISFIED BY: " + TrainerAssociation.this.offer.getValue().getDisplayName().getString());
                return true;
            }
            
            return false;
        }

        @Override
        public SeriesSwitchOffer copy() {
            return new SeriesSwitchOffer(this);
        }

        // full_star: ★, left_half: ⯪, empty_star: ☆
        private static String makeStars(int n, int m) {
            n = Math.min(n, m);
            var full = n / 2;
            var half = n > 0 && n % 2 != 0;
            var empty = (m - n) / 2;
            var sb = new StringBuilder();

            for(int i = 0; i < full; i++) {
                sb.append('★'); // full_star
            }

            if(half) {
                sb.append('⯪'); // left_half
            }

            for(int i = 0; i < empty; i++) {
                sb.append('☆'); // empty star
            }

            return sb.toString();
        }

        private static ItemStack createOfferFor(Player player, String seriesId, SeriesMetaData seriesData) {
            var card = new ItemStack(Items.TRAINER_CARD.get(), 1);
            var completions = player != null ? RCTMod.getInstance().getTrainerManager().getData(player).getCompletedSeries().getOrDefault(seriesId, 0) : 0;

            card.applyComponents(DataComponentMap.builder()
                .set(DataComponents.CUSTOM_NAME, Component.literal(seriesData.title()))
                .set(DataComponents.LORE, new ItemLore(List.of(
                    Component.literal(seriesData.description()),
                    Component.literal(""), // empty line
                    Component.literal(String.format("Difficulty: %s", makeStars(seriesData.difficulty(), SeriesMetaData.MAX_DIFFICULTY))),
                    Component.literal(String.format("Completed: %d", completions)),
                    Component.literal(""), // empty line
                    Component.literal("Important").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD),
                    Component.literal("Starting a new series will reset your current series progression! In return completing a series will grant a permanent increase of better odds for more valuable loot from trainers.").withStyle(ChatFormatting.GOLD)
                ))).build());

            return card;
        }
    }
}
