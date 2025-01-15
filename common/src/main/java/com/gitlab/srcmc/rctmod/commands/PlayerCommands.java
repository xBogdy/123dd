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
package com.gitlab.srcmc.rctmod.commands;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData.Type;
import com.gitlab.srcmc.rctmod.api.data.save.TrainerPlayerData;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class PlayerCommands {
    private PlayerCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(ModCommon.MOD_ID)
            .requires(css -> css.hasPermission(1))
            .then(Commands.literal("player")
                .then(Commands.literal("get")
                    .then(Commands.literal("progress")
                        .executes(PlayerCommands::player_get_progress)
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(PlayerCommands::player_get_progress_target)))
                    .then(Commands.literal("level_cap")
                        .executes(PlayerCommands::player_get_level_cap)
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(PlayerCommands::player_get_level_cap_target)))
                    .then(Commands.literal("defeats")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainerId", StringArgumentType.string())
                            .suggests(PlayerCommands::get_trainer_suggestions)
                            .executes(PlayerCommands::player_get_defeats)
                            .then(Commands.argument("target", EntityArgument.player())
                                .executes(PlayerCommands::player_get_defeats_target))))
                    .then(Commands.literal("type_defeats")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("type", StringArgumentType.string())
                            .suggests(PlayerCommands::get_type_suggestions)
                            .executes(PlayerCommands::player_get_type_defeats)
                            .then(Commands.argument("target", EntityArgument.player())
                                .executes(PlayerCommands::player_get_type_defeats_target)))))
                .then(Commands.literal("set")
                    .requires(css -> css.hasPermission(2))
                    .then(Commands.literal("progress")
                        .then(Commands.literal("before")
                            .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainerId", StringArgumentType.string())
                            .suggests(PlayerCommands::get_progress_trainer_suggestions)
                            .executes(PlayerCommands::player_set_progress_before)))
                        .then(Commands.literal("after")
                            .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainerId", StringArgumentType.string())
                            .suggests(PlayerCommands::get_progress_trainer_suggestions)
                            .executes(PlayerCommands::player_set_progress_after)))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .then(Commands.literal("before")
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainerId", StringArgumentType.string())
                                .suggests(PlayerCommands::get_progress_trainer_suggestions)
                                .executes(PlayerCommands::player_set_progress_targets_before)))
                            .then(Commands.literal("after")
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainerId", StringArgumentType.string())
                                .suggests(PlayerCommands::get_progress_trainer_suggestions)
                                .executes(PlayerCommands::player_set_progress_targets_after)))))
                    .then(Commands.literal("defeats")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainerId", StringArgumentType.string())
                            .suggests(PlayerCommands::get_trainer_suggestions)
                            .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(PlayerCommands::player_set_defeats_value))
                            .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                    .executes(PlayerCommands::player_set_defeats_targets_value)))))
                )));
    }

    private static CompletableFuture<Suggestions> get_progress_trainer_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        RCTMod.getInstance().getTrainerManager().getAllData()
            .filter(e -> !e.getValue().getFollowdBy().isEmpty() || e.getValue().getMissingRequirements(Set.of()).findFirst().isPresent())
            .map(e -> e.getKey()).forEach(builder::suggest);

        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> get_trainer_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        RCTMod.getInstance().getTrainerManager().getAllData().map(e -> e.getKey()).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> get_type_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        Stream.of(Type.values()).map(Type::name).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int player_get_progress(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var it = RCTMod.getInstance().getTrainerManager().getData(player).getDefeatedTrainerIds().stream().iterator();
            var sb = new StringBuilder();
            sb.append('[');
            
            if(it.hasNext()) {
                sb.append(it.next());
            }
            
            while(it.hasNext()) {
                sb.append(", ").append(it.next());
            }

            sb.append(']');
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(sb.toString())), false);
            return 0;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_get_progress_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "target");
        var it = RCTMod.getInstance().getTrainerManager().getData(player).getDefeatedTrainerIds().stream().iterator();
        var sb = new StringBuilder();
        sb.append('[');
        
        if(it.hasNext()) {
            sb.append(it.next());
        }
        
        while(it.hasNext()) {
            sb.append(", ").append(it.next());
        }

        sb.append(']');
        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(sb.toString())), false);
        return 0;
    }

    private static int player_get_level_cap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var level_cap = RCTMod.getInstance().getTrainerManager().getData(player).getLevelCap();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(level_cap)), false);
            return level_cap;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_get_level_cap_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "target");
        var level_cap = RCTMod.getInstance().getTrainerManager().getData(player).getLevelCap();
        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(level_cap)), false);
        return level_cap;
    }

    private static int player_get_defeats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            try {
                var trainerId = context.getArgument("trainerId", String.class);
                var count = PlayerState.get(player).getTrainerDefeatCount(trainerId);
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

    private static int player_get_defeats_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            var trainerId = context.getArgument("trainerId", String.class);
            var player = EntityArgument.getPlayer(context, "target");
            var count = PlayerState.get(player).getTrainerDefeatCount(trainerId);
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(count)), false);
            return count;
        } catch(IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return - 1;
        }
    }

    private static int player_get_type_defeats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            try {
                var type = Type.valueOf(context.getArgument("type", String.class));
                var count = PlayerState.get(player).getTypeDefeatCount(type);
                context.getSource().sendSuccess(() -> Component.literal(String.valueOf(count)), false);
                return (int)count;
            } catch(IllegalArgumentException e) {
                context.getSource().sendFailure(Component.literal(e.getMessage()));
            }
        } else {
            context.getSource().sendFailure(Component.literal("caller is not a player"));
        }

        return -1;
    }

    private static int player_get_type_defeats_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            var type = Type.valueOf(context.getArgument("type", String.class));
            var player = EntityArgument.getPlayer(context, "target");
            var count = PlayerState.get(player).getTypeDefeatCount(type);
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(count)), false);
            return (int)count;
        } catch(IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return - 1;
        }
    }

    private static void add_progress(TrainerPlayerData tpd, String trainerId, Set<String> visited) {
        if(!visited.contains(trainerId)) {
            visited.add(trainerId);

            var tmd = RCTMod.getInstance().getTrainerManager().getData(trainerId);
            tmd.getMissingRequirements(Set.of()).forEach(tid -> PlayerCommands.add_progress(tpd, tid, visited));
            tpd.addProgressDefeat(trainerId);
        }
    }

    private static int player_set_progress_before(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var trainerId = StringArgumentType.getString(context, "trainerId");
            var tm = RCTMod.getInstance().getTrainerManager();
            var tmd = tm.getData(trainerId);

            if(tmd != null) {
                var tpd = RCTMod.getInstance().getTrainerManager().getData(player);
                tpd.removeProgressDefeats();
                var visited = new HashSet<String>();
                tmd.getMissingRequirements(Set.of()).forEach(tid -> PlayerCommands.add_progress(tpd, tid, visited));
            }

            return 0;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_set_progress_after(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var trainerId = StringArgumentType.getString(context, "trainerId");
            var tm = RCTMod.getInstance().getTrainerManager();
            var tmd = tm.getData(trainerId);

            if(tmd != null) {
                var tpd = RCTMod.getInstance().getTrainerManager().getData(player);
                tpd.removeProgressDefeats();
                PlayerCommands.add_progress(tpd, trainerId, new HashSet<>());
            }

            return 0;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_set_progress_targets_before(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var targets = EntityArgument.getPlayers(context, "targets");
        var trainerId = StringArgumentType.getString(context, "trainerId");
        var tm = RCTMod.getInstance().getTrainerManager();

        for(var player : targets) {
            var tmd = tm.getData(trainerId);

            if(tmd != null) {
                var tpd = RCTMod.getInstance().getTrainerManager().getData(player);
                tpd.removeProgressDefeats();
                var visited = new HashSet<String>();
                tmd.getMissingRequirements(Set.of()).forEach(tid -> PlayerCommands.add_progress(tpd, tid, visited));
            }
        }
        
        return 0;
    }

    private static int player_set_progress_targets_after(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var targets = EntityArgument.getPlayers(context, "targets");
        var trainerId = StringArgumentType.getString(context, "trainerId");
        var tm = RCTMod.getInstance().getTrainerManager();

        for(var player : targets) {
            var tmd = tm.getData(trainerId);

            if(tmd != null) {
                var tpd = RCTMod.getInstance().getTrainerManager().getData(player);
                tpd.removeProgressDefeats();
                PlayerCommands.add_progress(tpd, trainerId, new HashSet<>());
            }
        }
        
        return 0;
    }

    private static int player_set_defeats_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            try {
                var trainerId = context.getArgument("trainerId", String.class);
                var count = IntegerArgumentType.getInteger(context, "value");
                RCTMod.getInstance().getTrainerManager().getBattleMemory((ServerLevel)player.level(), trainerId).setDefeatedBy(trainerId, player, count);
                return count;
            } catch(IllegalArgumentException e) {
                context.getSource().sendFailure(Component.literal(e.getMessage()));
            }
        } else {
            context.getSource().sendFailure(Component.literal("caller is not a player"));
        }

        return -1;
    }

    private static int player_set_defeats_targets_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            var trainerId = context.getArgument("trainerId", String.class);
            var targets = EntityArgument.getPlayers(context, "targets");
            var count = IntegerArgumentType.getInteger(context, "value");
            var tm = RCTMod.getInstance().getTrainerManager();

            for(var player : targets) {
                tm.getBattleMemory((ServerLevel)player.level(), trainerId).setDefeatedBy(trainerId, player, count);
            }
            
            return count;
        } catch(IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return -1;
        }
    }
}
