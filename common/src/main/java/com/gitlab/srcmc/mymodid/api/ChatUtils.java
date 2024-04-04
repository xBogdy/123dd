package com.gitlab.srcmc.mymodid.api;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class ChatUtils {
    private ChatUtils() {}

    public static void reply(TrainerMob source, Player target, String context) {
        var messages = ModCommon.TRAINER_MANAGER.getData(source).getDialog().get(context);

        if(messages != null && messages.length > 0) {
            var message = PlayerChatMessage.unsigned(target.getUUID(), messages[(target.getRandom().nextInt() & Integer.MAX_VALUE) % messages.length]);
            target.createCommandSourceStack().sendChatMessage(OutgoingChatMessage.create(message), false, ChatType.bind(ChatType.CHAT, source));
        }
    }

    public static void battle(TrainerMob source, Player target) {
        try {
            target.getServer().getCommands().getDispatcher().execute(battleCommand(source, target, source.getTrainerId()), target.createCommandSourceStack());
        } catch(CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String battleCommand(Entity source, Player target, String trainer) {
        return String.format("trainers makebattle @s '%s' %s", trainer, source.getUUID().toString());
    }
}
