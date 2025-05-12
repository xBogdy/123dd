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
package com.gitlab.srcmc.rctmod.api.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.gitlab.srcmc.rctapi.api.util.Text;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ChatUtils {
    private static Map<String, Text[]> defaultDialog = new HashMap<>();

    public static void initDefault(Map<String, Text[]> defaultDialog) {
        ChatUtils.defaultDialog = defaultDialog == null ? Map.of() : defaultDialog;
    }

    public static void reply(TrainerMob source, Player target, String... contexts) {
        var tmd = RCTMod.getInstance().getTrainerManager().getData(source);
        reply(source, target, context -> tmd.getDialog().get(context), contexts);
    }

    public static void reply(LivingEntity source, Player target, String... contexts) {
        reply(source, target, context -> ChatUtils.defaultDialog.get(context), contexts);
    }

    private static void reply(LivingEntity source, Player target, Function<String, Text[]> messagesFunc, String... contexts) {
        for(var context : contexts) {
            var messages = messagesFunc.apply(context);

            if(messages != null) {
                if(messages.length > 0) {
                    var message = PlayerChatMessage.system("").withUnsignedContent(messages[(target.getRandom().nextInt() & Integer.MAX_VALUE) % messages.length].getComponent());
                    target.createCommandSourceStack().sendChatMessage(OutgoingChatMessage.create(message), false, ChatType.bind(ChatType.CHAT, source));
                    return;
                } else {
                    ModCommon.LOG.error(String.format("Empty dialog context '%s'", context));
                    return;
                }
            }
        }

        ModCommon.LOG.error(String.format("Invalid dialog contexts '%s'", String.valueOf(contexts)));
    }

    public static void replyRaw(LivingEntity source, Player target, Text text, Object... args) {
        var message = PlayerChatMessage.system("").withUnsignedContent(text.getComponent(args));
        target.createCommandSourceStack().sendChatMessage(OutgoingChatMessage.create(message), false, ChatType.bind(ChatType.CHAT, source));
    }

    public static void sendMessage(Player target, Text text, Object... args) {
        target.createCommandSourceStack().sendSystemMessage(text.getComponent(args));
    }

    public static void sendError(Player target, Text text, Object... args) {
        target.createCommandSourceStack().sendSystemMessage(text.getComponent(args).withStyle(ChatFormatting.RED));
    }

    public static void sendTitle(Player target, Text title, Text subtitle, Object... args) {
        try {
            var cs = target.createCommandSourceStack().withPermission(2).withSuppressedOutput();

            if(subtitle != null && !subtitle.isEmpty()) {
                cs.dispatcher().execute(String.format("title @s subtitle %s", Component.Serializer.toJson(subtitle.getComponent(args).withStyle(ChatFormatting.ITALIC), target.registryAccess())), cs);
            }

            if(title == null) {
                title = Text.empty();
            }
            
            cs.dispatcher().execute(String.format("title @s title %s", Component.Serializer.toJson(title.getComponent(args), target.registryAccess())), cs);
        } catch(CommandSyntaxException e) {
            ModCommon.LOG.error(e.getMessage(), e);
        }
    }

    public static void sendActionbar(Player target, Text message, Object... args) {
        try {
            var cs = target.createCommandSourceStack().withPermission(2).withSuppressedOutput();
            cs.dispatcher().execute(String.format("title @s actionbar %s", Component.Serializer.toJson(message.getComponent(args), target.registryAccess())), cs);
        } catch(CommandSyntaxException e) {
            ModCommon.LOG.error(e.getMessage(), e);
        }
    }

    private ChatUtils() {}
}
