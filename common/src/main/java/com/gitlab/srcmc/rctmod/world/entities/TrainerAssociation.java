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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.cobblemon.mod.common.api.apricorn.Apricorn;
import com.gitlab.srcmc.rctmod.ModRegistries.Items;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.SeriesMetaData;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager;
import com.gitlab.srcmc.rctmod.api.utils.ChatUtils;
import com.gitlab.srcmc.rctmod.world.entities.goals.RandomStrollThroughVillageGoal;
import com.gitlab.srcmc.rctmod.world.items.TrainerCard;
import com.google.common.collect.Streams;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.Item;
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

    public static final List<Supplier<OfferBuilder>> OFFER_CANDIDATES = List.of(
        () -> new OfferBuilder(net.minecraft.world.item.Items.EMERALD, Items.TRAINER_CARD.get())
    );

    public static final int MIN_ITEM_OFFERS = 1;
    public static final int MAX_ITEM_OFFERS = 1;
    public static final int MIN_TRADE_USES = 2;
    public static final int MAX_TRADE_USES = 4;
    public static final int MIN_EMERALD_PRIZE = 1;
    public static final int MAX_EMERALD_PRIZE = 8;
    public static final int MIN_SECONDARY_PRIZE = 1;
    public static final int MAX_SECONDARY_PRIZE = 5;
    public static final int MAX_RESTOCK_DELAY_TICKS = 24000;
    public static final int MIN_RESTOCK_DELAY_TICKS = 6000;
    public static final Supplier<List<? extends Item>> SECONDARY_OPTIONS = () -> Streams.concat(
        // Berries.INSTANCE.all().stream().map(Berry::item), // too harsh
        Stream.of(Apricorn.values()).map(Apricorn::item)
    ).toList();

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
    private MerchantOffers itemOffers;
    private Player playerTarget;
    private int despawnTicks;
    private int lastRestock;

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
                if(this.itemOffers == null) {
                    this.updateTrades();
                }

                this.attemptRestock();
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

    public void attemptRestock() {
        if(this.itemOffers != null && !this.isTrading() && (this.tickCount - this.lastRestock) > this.random.nextInt(MIN_RESTOCK_DELAY_TICKS, MAX_RESTOCK_DELAY_TICKS)) {
            var it = this.itemOffers.iterator();

            while(it.hasNext()) {
                var offer = it.next();
                offer.updateDemand();
                offer.resetUses();
            }

            this.lastRestock = this.tickCount;
        }
    }

    @Override
    protected void updateTrades() {
        this.itemOffers = new MerchantOffers();
        this.addRandomOffers(this.itemOffers, OFFER_CANDIDATES.stream().map(Supplier::get).toList(), MIN_ITEM_OFFERS, MAX_ITEM_OFFERS);
        this.offers = null;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.itemOffers = this.offers;
        this.offers = null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        var offers = this.offers;
        this.offers = itemOffers;
        super.addAdditionalSaveData(tag);
        this.offers = offers;
    }

    protected void addRandomOffers(MerchantOffers target, Collection<OfferBuilder> candidates, int min, int max) {
        var list = new LinkedList<>(candidates);
        var sec = SECONDARY_OPTIONS.get();
        var m = this.random.nextInt(min, max + 1);

        for(; m > 0 && !list.isEmpty(); --m) {
            var it = list.iterator();
            float n = list.size();
            int i = 0;

            while(it.hasNext()) {
                var ob = it.next();

                if((++i)/n > this.random.nextFloat()) {
                    target.add(ob
                        .primaryCount(this.random.nextInt(MIN_EMERALD_PRIZE, MAX_EMERALD_PRIZE + 1))
                        .secondaryCount(this.random.nextInt(MIN_SECONDARY_PRIZE, MAX_SECONDARY_PRIZE + 1))
                        .secondary(sec.isEmpty() ? null : sec.get(this.random.nextInt(sec.size())))
                        .maxUses(this.random.nextInt(MIN_TRADE_USES, MAX_TRADE_USES + 1))
                        .build());

                    it.remove();
                    break;
                }
            }
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

        if(this.itemOffers != null) {
            this.itemOffers.forEach(this.offers::add);
        }

        sm.getSeriesIds()
            .stream().map(sid -> Map.entry(sid, sm.getGraph(sid).getMetaData()))
            .sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
            .filter(e -> !e.getKey().equals(SeriesManager.EMPTY_SERIES_ID))
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
                    Component.literal("Starting a new series will reset your current series progression! In return completing a series will permanently increase your luck for better loot from trainers.").withStyle(ChatFormatting.GOLD)
                ))).build());

            return card;
        }
    }

    public static class OfferBuilder {
        private Item primary, secondary, result;
        private int primaryCount = 1, secondaryCount = 1, resultCount = 1, maxUses = 1, xp = 2;
        private float priceMultiplier = 0.05f;

        public OfferBuilder(Item primary, Item result) {
            this.primary = primary;
            this.result = result;
        }

        public OfferBuilder secondary(Item item) {
            this.secondary = item;
            return this;
        }

        public OfferBuilder primaryCount(int value) {
            this.primaryCount = value;
            return this;
        }

        public OfferBuilder secondaryCount(int value) {
            this.secondaryCount = value;
            return this;
        }

        public OfferBuilder resultCount(int value) {
            this.resultCount = value;
            return this;
        }

        public OfferBuilder maxUses(int value) {
            this.maxUses = value;
            return this;
        }

        public OfferBuilder xp(int value) {
            this.xp = value;
            return this;
        }

        public OfferBuilder priceMultiplier(float value) {
            this.priceMultiplier = value;
            return this;
        }

        public MerchantOffer build() {
            return new MerchantOffer(
                new ItemCost(this.primary, this.primaryCount),
                this.secondary != null ? Optional.of(new ItemCost(this.secondary, this.secondaryCount)) : Optional.empty(),
                new ItemStack(this.result, this.resultCount),
                this.maxUses, this.xp, this.priceMultiplier);
        }
    }
}
