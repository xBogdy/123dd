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

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ChatUtils {
    private static Map<String, String[]> defaultDialog = new HashMap<>();

    public static void initDefault(Map<String, String[]> defaultDialog) {
        ChatUtils.defaultDialog = defaultDialog == null ? Map.of() : defaultDialog;
    }

    public static void reply(TrainerMob source, Player target, String context) {
        var messages = RCTMod.getInstance().getTrainerManager().getData(source).getDialog().get(context);

        if(messages == null) {
            ModCommon.LOG.error(String.format("Invalid dialog context '%s'", context));
        } else if(messages.length > 0) {
            var message = PlayerChatMessage.system(messages[(target.getRandom().nextInt() & Integer.MAX_VALUE) % messages.length]);
            target.createCommandSourceStack().sendChatMessage(OutgoingChatMessage.create(message), false, ChatType.bind(ChatType.CHAT, source));
        }
    }

    public static void reply(LivingEntity source, Player target, String context) {
        var messages = ChatUtils.defaultDialog.get(context);

        if(messages == null) {
            ModCommon.LOG.error(String.format("Invalid dialog context '%s'", context));
        } else if(messages.length > 0) {
            var message = PlayerChatMessage.system(messages[(target.getRandom().nextInt() & Integer.MAX_VALUE) % messages.length]);
            target.createCommandSourceStack().sendChatMessage(OutgoingChatMessage.create(message), false, ChatType.bind(ChatType.CHAT, source));
        }
    }

    public static void replyRaw(LivingEntity source, Player target, String rawMessage) {
        var message = PlayerChatMessage.system(rawMessage);
        target.createCommandSourceStack().sendChatMessage(OutgoingChatMessage.create(message), false, ChatType.bind(ChatType.CHAT, source));
    }

    public static void sendTitle(Player target, String title, String subtitle) {
        try {
            var cs = target.createCommandSourceStack().withPermission(2).withSuppressedOutput();

            if(subtitle != null && !subtitle.isBlank()) {
                cs.dispatcher().execute(String.format("title @s subtitle {\"text\": \"%s\", \"italic\": true}", subtitle), cs);
            }

            if(title == null) {
                title = "";
            }

            cs.dispatcher().execute(String.format("title @s title {\"text\": \"%s\"}", title), cs);
        } catch(CommandSyntaxException e) {
            ModCommon.LOG.error(e.getMessage(), e);
        }
    }

    private ChatUtils() {}
}
