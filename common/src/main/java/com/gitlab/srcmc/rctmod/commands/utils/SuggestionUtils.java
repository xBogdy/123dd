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
package com.gitlab.srcmc.rctmod.commands.utils;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerType;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class SuggestionUtils {
    public static CompletableFuture<Suggestions> get_trainer_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        var remaining = builder.getRemaining();

        // Alternative: only suggest first id components (since there seems to be a max amount of shown suggestions, probably ~1000)
        // if(remaining.isBlank()) {
        //     RCTMod.getInstance().getTrainerManager().getAllData()
        //         .map(e -> { int i = e.getKey().indexOf('_'); return i < 0 ? e.getKey() : e.getKey().substring(0, i+1); })
        //         .distinct().forEach(builder::suggest);
        // } else {
        //     RCTMod.getInstance().getTrainerManager().getAllData()
        //         .map(e -> e.getKey())
        //         .filter(tid -> tid.startsWith(remaining))
        //         .forEach(builder::suggest);
        // }

        RCTMod.getInstance().getTrainerManager().getAllData()
            .map(e -> e.getKey())
            .filter(tid -> tid.startsWith(remaining))
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> get_series_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        Stream.concat(RCTMod.getInstance().getSeriesManager().getSeriesIds().stream(), Stream.of("")).forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> get_progress_trainer_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        var sm = RCTMod.getInstance().getSeriesManager();
        var tm = RCTMod.getInstance().getTrainerManager();
        Stream<String> tidStream;

        if(context.getSource().getEntity() instanceof Player p) {
            var tpd = tm.getData(p);
            tidStream = sm.getGraph(tpd.getCurrentSeries()).stream().filter(tn -> !tn.isAlone()).map(tn -> tn.id());
        } else {
            var sb = Stream.<String>builder();

            EntityArgument.getPlayers(context, "targets").forEach(p -> {
                var tpd = tm.getData(p);
                sm.getGraph(tpd.getCurrentSeries()).stream().filter(tn -> !tn.isAlone()).map(tn -> tn.id()).forEach(sb::accept);
            });

            tidStream = sb.build();
        }

        tidStream.distinct().forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> get_type_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        TrainerType.ids().forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static int player_get_current_series(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(context.getSource().getEntity() instanceof Player player) {
            context.getSource().sendSuccess(() -> Component.literal(PlayerState.get(player).getCurrentSeries()), false);
            return 0;
        }
        
        context.getSource().sendFailure(Component.literal("caller is not a player"));
        return -1;
    }

    private SuggestionUtils() {
    }
}
