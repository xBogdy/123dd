package com.gitlab.srcmc.mymodid.api;

import java.util.Map;

import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import static java.util.Map.entry;

public class ChatUtils {
    // TODO: this map should be a property of TrainerMobData so each entity (or group of entities) can have its own set of replies.
    private static final Map<String, String[]> REPLY_MAP = Map.ofEntries(
        entry("missing_badges", new String[]{
            "You might want to beat some leaders with lower levels first."
        }),
        entry("missing_beaten_e4", new String[]{
            "You have not proven yourself worthy to face me."
        }),
        entry("missing_beaten_champs", new String[]{
            "..."
        }),
        entry("low_level_cap", new String[]{
            "You still need to train more."
        }),
        entry("battle_start", new String[]{
            "I hope you have prepared yourself."
        }),
        entry("battle_lost", new String[]{
            "Oh no...",
            "This was unexpected."
        }),
        entry("battle_won", new String[]{
            "That was easy."
        }),
        entry("on_cooldown", new String[]{
            "Give me a break."
        })
    );

    public static void reply(Entity source, Player target, String key) {
        var messages = REPLY_MAP.get(key);

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
        return String.format("trainers makebattle @s 'trainers/teams/%s' %s", trainer, source.getUUID().toString());
    }
}
