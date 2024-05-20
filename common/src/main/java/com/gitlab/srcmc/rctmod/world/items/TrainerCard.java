package com.gitlab.srcmc.rctmod.world.items;

import com.gitlab.srcmc.rctmod.client.screens.TrainerCardScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TrainerCard extends Item {
    public TrainerCard() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if(level.isClientSide) {
            Minecraft.getInstance().setScreen(new TrainerCardScreen());
        }

        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }
}
