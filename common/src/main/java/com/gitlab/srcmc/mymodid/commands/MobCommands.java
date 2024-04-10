package com.gitlab.srcmc.mymodid.commands;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.api.RCTMod;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class MobCommands {
    private MobCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(ModCommon.MOD_ID)
            .requires(css -> css.hasPermission(1))
            .then(Commands.literal("mob")
                .then(Commands.literal("spawn")
                    .executes(MobCommands::mob_spawn))
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

    private static int mob_spawn(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            var tsp = RCTMod.get().getTrainerSpawner();
            try {
                tsp.attemptSpawnFor(player);
            } catch(Exception e) {
                ModCommon.LOG.error(e.getMessage(), e);
            }
            return 0;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
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
