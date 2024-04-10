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

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

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
                    .then(Commands.literal("badges")
                        .executes(PlayerCommands::player_get_badges)
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(PlayerCommands::player_get_badges_target)))
                    .then(Commands.literal("beaten_e4")
                        .executes(PlayerCommands::player_get_beaten_e4)
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(PlayerCommands::player_get_beaten_e4_target)))
                    .then(Commands.literal("beaten_champs")
                        .executes(PlayerCommands::player_get_beaten_champs)
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(PlayerCommands::player_get_beaten_champs_target))))
                .then(Commands.literal("set")
                .requires(css -> css.hasPermission(2))
                    .then(Commands.literal("level_cap")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                            .executes(PlayerCommands::player_set_level_cap_value))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(PlayerCommands::player_set_level_cap_targets_value))))
                    .then(Commands.literal("badges")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                            .executes(PlayerCommands::player_set_badges_value))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(PlayerCommands::player_set_badges_targets_value))))
                    .then(Commands.literal("beaten_e4")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                            .executes(PlayerCommands::player_set_beaten_e4_value))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(PlayerCommands::player_set_beaten_e4_targets_value))))
                    .then(Commands.literal("beaten_champs")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                            .executes(PlayerCommands::player_set_beaten_champs_value))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(PlayerCommands::player_set_beaten_champs_targets_value)))))));
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

    private static int player_get_badges(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var badges = RCTMod.get().getTrainerManager().getData(player).getBadges();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(badges)), false);
            return badges;
        }

        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_get_badges_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "target");
        var badges = RCTMod.get().getTrainerManager().getData(player).getBadges();
        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(badges)), false);
        return badges;
    }

    private static int player_get_beaten_e4(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var beaten_e4 = RCTMod.get().getTrainerManager().getData(player).getBeatenE4();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(beaten_e4)), false);
            return beaten_e4;
        }

        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_get_beaten_e4_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "target");
        var beaten_e4 = RCTMod.get().getTrainerManager().getData(player).getBeatenE4();
        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(beaten_e4)), false);
        return beaten_e4;
    }

    private static int player_get_beaten_champs(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var beaten_champs = RCTMod.get().getTrainerManager().getData(player).getBeatenChamps();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(beaten_champs)), false);
            return beaten_champs;
        }

        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_get_beaten_champs_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "target");
        var beaten_champs = RCTMod.get().getTrainerManager().getData(player).getBeatenChamps();
        context.getSource().sendSuccess(() -> Component.literal(String.valueOf(beaten_champs)), false);
        return beaten_champs;
    }

    private static int player_set_level_cap_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var level_cap = IntegerArgumentType.getInteger(context, "value");
            RCTMod.get().getTrainerManager().getData(player).setLevelCap(level_cap);
            return level_cap;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_set_level_cap_targets_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var targets = EntityArgument.getPlayers(context, "targets");
        var level_cap = IntegerArgumentType.getInteger(context, "value");

        for(var player : targets) {
            RCTMod.get().getTrainerManager().getData(player).setLevelCap(level_cap);
        }
        
        return level_cap;
    }

    private static int player_set_badges_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var badges = IntegerArgumentType.getInteger(context, "value");
            RCTMod.get().getTrainerManager().getData(player).setBadges(badges);
            return badges;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_set_badges_targets_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var targets = EntityArgument.getPlayers(context, "targets");
        var badges = IntegerArgumentType.getInteger(context, "value");

        for(var player : targets) {
            RCTMod.get().getTrainerManager().getData(player).setBadges(badges);
        }
        
        return badges;
    }

    private static int player_set_beaten_e4_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var beaten_e4 = IntegerArgumentType.getInteger(context, "value");
            RCTMod.get().getTrainerManager().getData(player).setBeatenE4(beaten_e4);
            return beaten_e4;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_set_beaten_e4_targets_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var targets = EntityArgument.getPlayers(context, "targets");
        var beaten_e4 = IntegerArgumentType.getInteger(context, "value");

        for(var player : targets) {
            RCTMod.get().getTrainerManager().getData(player).setBeatenE4(beaten_e4);
        }
        
        return beaten_e4;
    }

    private static int player_set_beaten_champs_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var beaten_champs = IntegerArgumentType.getInteger(context, "value");
            RCTMod.get().getTrainerManager().getData(player).setBeatenChamps(beaten_champs);
            return beaten_champs;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private static int player_set_beaten_champs_targets_value(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var targets = EntityArgument.getPlayers(context, "targets");
        var beaten_champs = IntegerArgumentType.getInteger(context, "value");

        for(var player : targets) {
            RCTMod.get().getTrainerManager().getData(player).setBeatenChamps(beaten_champs);
        }
        
        return beaten_champs;
    }
}
