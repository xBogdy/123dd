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
package com.gitlab.srcmc.rctmod.commands;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData.Type;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class PlayerCommands {
    private PlayerCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(ModCommon.MOD_ID)
            .requires(css -> css.hasPermission(1))
            .then(Commands.literal("player")
                .then(Commands.literal("get")
                    .then(Commands.literal("level_cap")
                        .executes(PlayerCommands::player_get_level_cap)
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(PlayerCommands::player_get_level_cap_target)))
                    .then(Commands.literal("defeats")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("type", StringArgumentType.string())
                            .suggests(PlayerCommands::get_type_suggestions)
                            .executes(PlayerCommands::player_get_defeats_type)
                            .then(Commands.argument("target", EntityArgument.player())
                                .executes(PlayerCommands::player_get_defeats_type_target)))))
                .then(Commands.literal("set")
                    .requires(css -> css.hasPermission(2))
                    .then(Commands.literal("level_cap")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                            .executes(PlayerCommands::player_set_level_cap_value))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(PlayerCommands::player_set_level_cap_targets_value))))
                    .then(Commands.literal("defeats")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("type", StringArgumentType.string())
                            .suggests(PlayerCommands::get_type_suggestions)
                            .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(PlayerCommands::player_set_defeats_type_value))
                            .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                    .executes(PlayerCommands::player_set_defeats_type_targets_value))))))));
    }

    private static CompletableFuture<Suggestions> get_type_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        Stream.of(Type.values()).map(Type::name).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int player_get_level_cap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var level_cap = RCTMod.get().getTrainerManager().getData(player).getLevelCap();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(level_cap)), false);
            return level_cap;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_get_level_cap_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "target");
        var level_cap = RCTMod.get().getTrainerManager().getData(player).getLevelCap();
        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(level_cap)), false);
        return level_cap;
    }

    private static int player_get_defeats_type(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            try {
                var type = Type.valueOf(context.getArgument("type", String.class));
                var count = RCTMod.get().getTrainerManager().getData(player).getDefeats(type);
                context.getSource().sendSuccess(() -> Component.literal(String.valueOf(count)), false);
                return count;
            } catch(IllegalArgumentException e) {
                context.getSource().sendFailure(Component.literal(e.getMessage()));
            }
        } else {
            context.getSource().sendFailure(Component.literal("caller is not a player"));
        }

        return -1;
    }

    private static int player_get_defeats_type_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            var type = Type.valueOf(context.getArgument("type", String.class));
            var player = EntityArgument.getPlayer(context, "target");
            var count = RCTMod.get().getTrainerManager().getData(player).getDefeats(type);
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(count)), false);
            return count;
        } catch(IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return - 1;
        }
    }

    private static int player_set_level_cap_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var level_cap = IntegerArgumentType.getInteger(context, "value");
            RCTMod.get().getTrainerManager().getData(player).setLevelCap(player, level_cap);
            return level_cap;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_set_level_cap_targets_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var targets = EntityArgument.getPlayers(context, "targets");
        var level_cap = IntegerArgumentType.getInteger(context, "value");
        var tm = RCTMod.get().getTrainerManager();

        for(var player : targets) {
            tm.getData(player).setLevelCap(player, level_cap);
        }
        
        return level_cap;
    }

    private static int player_set_defeats_type_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            try {
                var type = Type.valueOf(context.getArgument("type", String.class));
                var count = IntegerArgumentType.getInteger(context, "value");
                RCTMod.get().getTrainerManager().getData(player).setDefeats(type, count);
                return count;
            } catch(IllegalArgumentException e) {
                context.getSource().sendFailure(Component.literal(e.getMessage()));
            }
        } else {
            context.getSource().sendFailure(Component.literal("caller is not a player"));
        }

        return -1;
    }

    private static int player_set_defeats_type_targets_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            var type = Type.valueOf(context.getArgument("type", String.class));
            var targets = EntityArgument.getPlayers(context, "targets");
            var count = IntegerArgumentType.getInteger(context, "value");
            var tm = RCTMod.get().getTrainerManager();

            for(var player : targets) {
                tm.getData(player).setDefeats(type, count);
            }
            
            return count;
        } catch(IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return -1;
        }
    }
}
