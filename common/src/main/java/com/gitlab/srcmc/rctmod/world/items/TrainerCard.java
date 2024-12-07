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
package com.gitlab.srcmc.rctmod.world.items;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.client.renderer.TargetArrowRenderer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class TrainerCard extends Item {
    public TrainerCard() {
        super(new Properties().stacksTo(1).component(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())));
    }

    public void setFoil(ItemStack stack, boolean foil) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()), d -> d.update(tag -> tag.putBoolean("foil", foil)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean("foil");
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int i, boolean bl) {
        if(level.isClientSide) {
            if(level.getGameTime() % 60 == 0) {
                if(entity instanceof Player player) {
                    var cfg = RCTMod.getInstance().getServerConfig();
                    var ps = PlayerState.get(player);
                    var keyTrainers = level.getEntities(
                        ModRegistries.Entities.TRAINER.get(),
                        player.getBoundingBox().inflate(
                            cfg.maxHorizontalDistanceToPlayers(),
                            cfg.maxVerticalDistanceToPlayers(),
                            cfg.maxHorizontalDistanceToPlayers()),
                        t -> t.couldBattleAgainst(player) && ps.isKeyTrainer(t.getTrainerId()));

                    if(keyTrainers.size() > 0) {
                        var t = keyTrainers.get(0);
                        this.setFoil(stack, true);
                        TargetArrowRenderer.getInstance().setTarget(player, t);
                    } else {
                        this.setFoil(stack, false);
                        TargetArrowRenderer.getInstance().setTarget(null, null);
                    }
                } else {
                    this.setFoil(stack, false);
                }
            }
        }

        super.inventoryTick(stack, level, entity, i, bl);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if(level.isClientSide) {
            if(interactionHand == InteractionHand.MAIN_HAND && player.getOffhandItem().isEmpty() || player.getMainHandItem().isEmpty()) {
                var screens = ModCommon.getScreenManager();

                if(screens.isPresent()) {
                    screens.get().openTrainerCardScreen();
                }
            }
        }

        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }
}
