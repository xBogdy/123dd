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

import java.util.List;
import java.util.Map;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries.Items;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.utils.ChatUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

public class TrainerAssociation extends WanderingTrader {
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

    private Map.Entry<String, ItemStack> offer;

    public TrainerAssociation(EntityType<? extends WanderingTrader> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        var itemStack = player.getItemInHand(interactionHand);

        if(!itemStack.is(net.minecraft.world.item.Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if(!this.level().isClientSide) {
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
        var ps = PlayerState.get(player);
        this.offers = new MerchantOffers();

        // TODO: retrieve available seriesIds based of player progression (sorted by: difficulty (ascending) -> name (ascending))
        if(!ps.getCurrentSeries().equals("radicalred")) {
            this.offers.add(new SeriesSwitchOffer("radicalred"));
        }

        if(!ps.getCurrentSeries().equals("bdsp")) {
            this.offers.add(new SeriesSwitchOffer("bdsp"));
        }

        this.stopTrading();
    }

    private class SeriesSwitchOffer extends MerchantOffer {
        private String seriesId;

        public SeriesSwitchOffer(String seriesId) {
            super(new ItemCost(Items.TRAINER_CARD.get()), createOfferFor(seriesId), Integer.MAX_VALUE, Integer.MAX_VALUE, 1f);
            this.seriesId = seriesId;
        }

        private SeriesSwitchOffer(SeriesSwitchOffer origin) {
            this(origin.seriesId);
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

        private static ItemStack createOfferFor(String seriesId) {
            var card = new ItemStack(Items.TRAINER_CARD.get(), 1);

            // TODO: retrieve series metadata by seriesId
            if(seriesId.equals("radicalred")) {
                card.applyComponents(DataComponentMap.builder()
                    .set(DataComponents.CUSTOM_NAME, Component.literal("Radical Red"))
                    .set(DataComponents.LORE, new ItemLore(List.of(
                        Component.literal("A difficult series taking place in the Kanto region. Are you up to the challenge?"),
                        Component.literal("Difficulty: ★★★★⯨"), // ★⯨☆
                        Component.literal(""), // empty line
                        Component.literal("Important").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD),
                        Component.literal("Switching a series will reset your progression and level cap!").withStyle(ChatFormatting.GOLD)
                    ))).build());
            } else {
                card.applyComponents(DataComponentMap.builder()
                    .set(DataComponents.CUSTOM_NAME, Component.literal("Brilliant Diamond/Shining Pearl"))
                    .set(DataComponents.LORE, new ItemLore(List.of(
                        Component.literal("A casual series but stil with decent difficulty. Are you ready to conquer Unova?"),
                        Component.literal("Difficulty: ★★★⯨☆"), // ★⯨☆
                        Component.literal(""), // empty line
                        Component.literal("Important").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD),
                        Component.literal("Switching a series will reset your progression and level cap!").withStyle(ChatFormatting.GOLD)
                    ))).build());
            }

            return card;
        }
    }
}
