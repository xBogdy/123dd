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

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class MobCommands {
    private MobCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(ModCommon.MOD_ID)
            .requires(css -> css.hasPermission(1))
            .then(Commands.literal("mob")
                .then(Commands.literal("spawn_for")
                .requires(css -> css.hasPermission(2))
                    .executes(MobCommands::mob_spawn_for)
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(MobCommands::mob_spawn_for_target)))
                .then(Commands.literal("summon")
                .requires(css -> css.hasPermission(2))
                    .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("trainer", StringArgumentType.string())
                    .suggests(MobCommands::get_trainer_suggestions)
                        .executes(MobCommands::mob_summon_trainer)
                        .then(Commands.argument("at", BlockPosArgument.blockPos())
                            .executes(MobCommands::mob_summon_trainer_at))))
                .then(Commands.literal("get")
                    .then(Commands.literal("max_trainer_wins")
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes(MobCommands::mob_get_max_trainer_wins_target)))
                    .then(Commands.literal("max_trainer_defeats")
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes(MobCommands::mob_get_max_trainer_defeats_target)))
                    .then(Commands.literal("required_level_cap")
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes(MobCommands::mob_get_required_level_cap_target)))
                    .then(Commands.literal("required_badges")
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes(MobCommands::mob_get_required_badges_target)))
                    .then(Commands.literal("required_beaten_e4")
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes(MobCommands::mob_get_required_beaten_e4_target)))
                    .then(Commands.literal("required_beaten_champs")
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes(MobCommands::mob_get_required_beaten_champs_target))))));
    }

    private static CompletableFuture<Suggestions> get_trainer_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        RCTMod.get().getTrainerManager().getAllData().map(e -> e.getKey()).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int mob_summon_trainer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            try {
                var level = player.level();
                var mob = TrainerMob.getEntityType().create(level);
                mob.setPos(player.blockPosition().above().getCenter());
                mob.setTrainerId(context.getArgument("trainer", String.class));
                level.addFreshEntity(mob);
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
        return 0;
    }

    private static int mob_spawn_for(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            try {
                RCTMod.get().getTrainerSpawner().attemptSpawnFor(player);
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
                RCTMod.get().getTrainerSpawner().attemptSpawnFor(player);
            } catch(Exception e) {
                ModCommon.LOG.error(e.getMessage(), e);
            }

            return 0;
        }

        context.getSource().sendFailure(Component.literal("target is not a player"));
        return -1;
    }

    private static int mob_get_max_trainer_wins_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(EntityArgument.getEntity(context, "target") instanceof TrainerMob mob) {
            var max_trainer_wins = RCTMod.get().getTrainerManager().getData(mob).getMaxTrainerWins();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(max_trainer_wins)), false);
            return max_trainer_wins;
        }

        context.getSource().sendFailure(Component.literal("target is not a trainer mob"));
        return -1;
    }

    private static int mob_get_max_trainer_defeats_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(EntityArgument.getEntity(context, "target") instanceof TrainerMob mob) {
            var max_trainer_defeats = RCTMod.get().getTrainerManager().getData(mob).getMaxTrainerDefeats();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(max_trainer_defeats)), false);
            return max_trainer_defeats;
        }

        context.getSource().sendFailure(Component.literal("target is not a trainer mob"));
        return -1;
    }

    private static int mob_get_required_level_cap_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(EntityArgument.getEntity(context, "target") instanceof TrainerMob mob) {
            var required_level_cap = RCTMod.get().getTrainerManager().getData(mob).getRequiredLevelCap();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(required_level_cap)), false);
            return required_level_cap;
        }

        context.getSource().sendFailure(Component.literal("target is not a trainer mob"));
        return -1;
    }

    private static int mob_get_required_badges_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(EntityArgument.getEntity(context, "target") instanceof TrainerMob mob) {
            var required_badges = RCTMod.get().getTrainerManager().getData(mob).getRequiredBadges();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(required_badges)), false);
            return required_badges;
        }

        context.getSource().sendFailure(Component.literal("target is not a trainer mob"));
        return -1;
    }

    private static int mob_get_required_beaten_e4_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(EntityArgument.getEntity(context, "target") instanceof TrainerMob mob) {
            var required_beaten_e4 = RCTMod.get().getTrainerManager().getData(mob).getRequiredBeatenE4();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(required_beaten_e4)), false);
            return required_beaten_e4;
        }

        context.getSource().sendFailure(Component.literal("target is not a trainer mob"));
        return -1;
    }

    private static int mob_get_required_beaten_champs_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(EntityArgument.getEntity(context, "target") instanceof TrainerMob mob) {
            var required_beaten_champs = RCTMod.get().getTrainerManager().getData(mob).getRequiredBeatenChamps();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(required_beaten_champs)), false);
            return required_beaten_champs;
        }

        context.getSource().sendFailure(Component.literal("target is not a trainer mob"));
        return -1;
    }
}
