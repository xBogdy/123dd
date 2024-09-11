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
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TrainerCard extends Item {
    public TrainerCard() {
        super(new Properties().stacksTo(1));
    }

    public void setFoil(ItemStack stack, boolean foil) {
        if(foil) {
            var tag = stack.getOrCreateTag();
            tag.putBoolean("foil", true);
            stack.setTag(tag);
        } else if(stack.hasTag()) {
            var tag = stack.getTag();
            tag.remove("foil");
            stack.setTag(tag);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || (stack.hasTag() && stack.getTag().getBoolean("foil"));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int i, boolean bl) {
        if(level.isClientSide) {
            if(entity instanceof Player player) {
                var target = PlayerState.get(player).getTarget();

                if(target != null) {
                    this.setFoil(stack, true);
                } else {
                    this.setFoil(stack, false);
                }
            } else {
                this.setFoil(stack, false);
            }
        }

        super.inventoryTick(stack, level, entity, i, bl);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if(level.isClientSide) {
            ModCommon.SCREENS.openTrainerCardScreen();
        }

        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }
}
