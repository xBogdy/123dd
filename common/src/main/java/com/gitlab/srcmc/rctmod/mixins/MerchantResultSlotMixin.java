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
package com.gitlab.srcmc.rctmod.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.utils.ChatUtils;
import com.gitlab.srcmc.rctmod.world.entities.TrainerAssociation;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin {
    @Shadow @Final private Merchant merchant;
    @Shadow @Final private MerchantContainer slots;

    @Inject(method = "onTake", at = @At("RETURN"), remap = true)
    public void inject_onTake(Player player, ItemStack itemStack, CallbackInfo ci) {
        if(this.merchant instanceof TrainerAssociation ta) {
            ModCommon.LOG.info("ONTAKE: " + itemStack.getDisplayName().getString());
            offerSeriesSwitch(ta, player, itemStack);
        }
    }

    @Inject(method = "onQuickCraft", at = @At("RETURN"), remap = true)
    protected void inject_onQuickCraft(ItemStack itemStack, int i, CallbackInfo ci) {
        if(this.merchant instanceof TrainerAssociation ta) {
            ModCommon.LOG.info("ONQUICKCRAFT: " + itemStack.getDisplayName().getString());
            offerSeriesSwitch(ta, ta.getTradingPlayer(), itemStack);
        }
    }

    private static void offerSeriesSwitch(TrainerAssociation ta, Player player, ItemStack itemStack) {
        if(!itemStack.isEmpty()) {
            var offer = ta.takeOffer();

            if(offer != null) {
                ModCommon.LOG.info("ITEM -> OFFER: " + itemStack.getDisplayName().getString() + " -> " + offer.getValue().getDisplayName().getString());

                if(!replaceDummy(player, itemStack, offer.getValue())) {
                    itemStack.remove(DataComponents.CUSTOM_NAME);
                    itemStack.remove(DataComponents.LORE);
                    itemStack.applyComponents(offer.getValue().getComponents());
                }

                RCTMod.getInstance().getTrainerManager().getData(player).setCurrentSeries(offer.getKey());
                ChatUtils.sendTitle(player, "A new Journey", RCTMod.getInstance().getSeriesManager().getData(offer.getKey()).title());
                ta.updateOffersFor(player);
            }
        }
    }

    private static boolean replaceDummy(Player player, ItemStack dummy, ItemStack replacement) {
        var inv = player.getInventory();
        var i = inv.findSlotMatchingItem(dummy);

        if(i >= 0) {
            ModCommon.LOG.info("DUMMY ITEM REPLACED");
            inv.setItem(i, replacement);
            return true;
        }

        return false;
    }
}
