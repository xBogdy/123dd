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
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class TrainerCommands {
    private TrainerCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(ModCommon.MOD_ID)
            .requires(css -> css.hasPermission(1))
            .then(Commands.literal("trainer")
                .then(Commands.literal("unregister_persistent")
                    .requires(css -> css.hasPermission(2))
                    .then(Commands.argument("mobUUID", StringArgumentType.string())
                        .executes(TrainerCommands::mob_unregister_persistent)))
                .then(Commands.literal("spawn_for")
                    .requires(css -> css.hasPermission(2))
                    .executes(TrainerCommands::mob_spawn_for)
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(TrainerCommands::mob_spawn_for_target)))
                .then(Commands.literal("summon")
                    .requires(css -> css.hasPermission(2))
                    .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainer", StringArgumentType.string())
                        .suggests(TrainerCommands::get_trainer_suggestions)
                        .executes(TrainerCommands::mob_summon_trainer)
                        .then(Commands.argument("at", BlockPosArgument.blockPos())
                            .executes(TrainerCommands::mob_summon_trainer_at))))
                .then(Commands.literal("summon_persistent")
                    .requires(css -> css.hasPermission(2))
                    .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainer", StringArgumentType.string())
                        .suggests(TrainerCommands::get_trainer_suggestions)
                        .executes(TrainerCommands::mob_summon_persistent_trainer)
                        .then(Commands.argument("at", BlockPosArgument.blockPos())
                            .executes(TrainerCommands::mob_summon_persistent_trainer_at))))
                .then(Commands.literal("get")
                    .then(Commands.literal("type")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainer", StringArgumentType.string())
                            .suggests(TrainerCommands::get_trainer_suggestions)
                            .executes(TrainerCommands::mob_get_type)))
                    .then(Commands.literal("max_trainer_wins")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainer", StringArgumentType.string())
                            .suggests(TrainerCommands::get_trainer_suggestions)
                            .executes(TrainerCommands::mob_get_max_trainer_wins)))
                    .then(Commands.literal("max_trainer_defeats")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainer", StringArgumentType.string())
                            .suggests(TrainerCommands::get_trainer_suggestions)
                            .executes(TrainerCommands::mob_get_max_trainer_defeats)))
                    .then(Commands.literal("reward_level_cap")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainer", StringArgumentType.string())
                            .suggests(TrainerCommands::get_trainer_suggestions)
                            .executes(TrainerCommands::mob_get_reward_level_cap)))
                    .then(Commands.literal("required_level_cap")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainer", StringArgumentType.string())
                            .suggests(TrainerCommands::get_trainer_suggestions)
                            .executes(TrainerCommands::mob_get_required_level_cap)))
                    // .then(Commands.literal("required_defeats")
                    //     .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("type", StringArgumentType.string())
                    //         .suggests(TrainerCommands::get_type_suggestions)
                    //         .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainer", StringArgumentType.string())
                    //             .suggests(TrainerCommands::get_trainer_suggestions)
                    //             .executes(TrainerCommands::mob_get_required_defeats))))
        )));
    }

    private static CompletableFuture<Suggestions> get_trainer_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        RCTMod.getInstance().getTrainerManager().getAllData().map(e -> e.getKey()).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> get_type_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        Stream.of(Type.values()).map(Type::name).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int mob_summon_trainer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            try {
                var level = player.level();
                var mob = TrainerMob.getEntityType().create(level);
                mob.setPos(player.blockPosition().above().getCenter().add(0, -0.5, 0));
                mob.setTrainerId(context.getArgument("trainer", String.class));
                level.addFreshEntity(mob);
                RCTMod.getInstance().getTrainerSpawner().register(mob);
            } catch(Exception e) {
                ModCommon.LOG.error(e.getMessage(), e);
            }

            return 0;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int mob_summon_trainer_at(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var level = context.getSource().getLevel();
        var mob = TrainerMob.getEntityType().create(level);
        mob.setPos(BlockPosArgument.getSpawnablePos(context, "at").getCenter().add(0, -0.5, 0));
        mob.setTrainerId(context.getArgument("trainer", String.class));
        level.addFreshEntity(mob);
        RCTMod.getInstance().getTrainerSpawner().register(mob);
        return 0;
    }

    private static int mob_summon_persistent_trainer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            try {
                var level = player.level();
                var mob = TrainerMob.getEntityType().create(level);
                mob.setPos(player.blockPosition().above().getCenter().add(0, -0.5, 0));
                mob.setTrainerId(context.getArgument("trainer", String.class));
                mob.setPersistent(true);
                level.addFreshEntity(mob);
                RCTMod.getInstance().getTrainerSpawner().register(mob);
            } catch(Exception e) {
                ModCommon.LOG.error(e.getMessage(), e);
            }

            return 0;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int mob_summon_persistent_trainer_at(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var level = context.getSource().getLevel();
        var mob = TrainerMob.getEntityType().create(level);
        mob.setPos(BlockPosArgument.getSpawnablePos(context, "at").getCenter().add(0, -0.5, 0));
        mob.setTrainerId(context.getArgument("trainer", String.class));
        mob.setPersistent(true);
        level.addFreshEntity(mob);
        RCTMod.getInstance().getTrainerSpawner().register(mob);
        return 0;
    }

    private static int mob_spawn_for(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            try {
                RCTMod.getInstance().getTrainerSpawner().attemptSpawnFor(player);
            } catch(Exception e) {
                ModCommon.LOG.error(e.getMessage(), e);
            }

            return 0;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int mob_spawn_for_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(EntityArgument.getEntity(context, "target") instanceof Player player) {
            try {
                RCTMod.getInstance().getTrainerSpawner().attemptSpawnFor(player);
            } catch(Exception e) {
                ModCommon.LOG.error(e.getMessage(), e);
            }

            return 0;
        }

        context.getSource().sendFailure(Component.literal("target is not a player"));
        return -1;
    }

    private static int mob_get_type(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var type = RCTMod.getInstance().getTrainerManager()
            .getData(context.getArgument("trainer", String.class))
            .getType().name();

        context.getSource().sendSuccess(() -> Component.literal(type), false);
        return 0;
    }

    private static int mob_get_max_trainer_wins(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var max_trainer_wins = RCTMod.getInstance().getTrainerManager()
            .getData(context.getArgument("trainer", String.class))
            .getMaxTrainerWins();

        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(max_trainer_wins)), false);
        return max_trainer_wins;
    }

    private static int mob_get_max_trainer_defeats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var max_trainer_defeats = RCTMod.getInstance().getTrainerManager()
            .getData(context.getArgument("trainer", String.class))
            .getMaxTrainerDefeats();

        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(max_trainer_defeats)), false);
        return max_trainer_defeats;
    }

    private static int mob_get_reward_level_cap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var reward_level_cap = RCTMod.getInstance().getTrainerManager()
            .getData(context.getArgument("trainer", String.class))
            .getRewardLevelCap();

        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(reward_level_cap)), false);
        return reward_level_cap;
    }

    private static int mob_get_required_level_cap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var required_level_cap = RCTMod.getInstance().getTrainerManager()
            .getData(context.getArgument("trainer", String.class))
            .getRequiredLevelCap();

        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(required_level_cap)), false);
        return required_level_cap;
    }

    // private static int mob_get_required_defeats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    //     var required_defeats = RCTMod.getInstance().getTrainerManager()
    //         .getData(context.getArgument("trainer", String.class))
    //         .getRequiredDefeats(Type.valueOf(context.getArgument("type", String.class)));

    //     context.getSource().sendSuccess(() -> Component.literal(String.valueOf(required_defeats)), false);
    //     return required_defeats;
    // }

    private static int mob_unregister_persistent(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        RCTMod.getInstance().getTrainerSpawner().unregisterPersistent(context.getArgument("mobUUID", String.class));
        return 0;
    }
}
