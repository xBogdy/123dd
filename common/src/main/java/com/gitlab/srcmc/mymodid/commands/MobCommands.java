package com.gitlab.srcmc.mymodid.commands;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public class MobCommands {
    private MobCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(ModCommon.MOD_ID)
            .requires(css -> css.hasPermission(1))
            .then(Commands.literal("mob")
                .then(Commands.literal("get")
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

    private static int mob_get_required_level_cap_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(EntityArgument.getEntity(context, "target") instanceof TrainerMob mob) {
            var required_level_cap = ModCommon.TRAINER_MANAGER.getData(mob).getRequiredLevelCap();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(required_level_cap)), false);
            return required_level_cap;
        }

        context.getSource().sendFailure(Component.literal("target is not a trainer mob"));
        return -1;
    }

    private static int mob_get_required_badges_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(EntityArgument.getEntity(context, "target") instanceof TrainerMob mob) {
            var required_badges = ModCommon.TRAINER_MANAGER.getData(mob).getRequiredBadges();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(required_badges)), false);
            return required_badges;
        }

        context.getSource().sendFailure(Component.literal("target is not a trainer mob"));
        return -1;
    }

    private static int mob_get_required_beaten_e4_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(EntityArgument.getEntity(context, "target") instanceof TrainerMob mob) {
            var required_beaten_e4 = ModCommon.TRAINER_MANAGER.getData(mob).getRequiredBeatenE4();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(required_beaten_e4)), false);
            return required_beaten_e4;
        }

        context.getSource().sendFailure(Component.literal("target is not a trainer mob"));
        return -1;
    }

    private static int mob_get_required_beaten_champs_target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(EntityArgument.getEntity(context, "target") instanceof TrainerMob mob) {
            var required_beaten_champs = ModCommon.TRAINER_MANAGER.getData(mob).getRequiredBeatenChamps();
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(required_beaten_champs)), false);
            return required_beaten_champs;
        }

        context.getSource().sendFailure(Component.literal("target is not a trainer mob"));
        return -1;
    }
}
