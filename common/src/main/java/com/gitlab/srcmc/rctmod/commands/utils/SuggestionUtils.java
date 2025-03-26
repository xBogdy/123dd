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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerType;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

public final class SuggestionUtils {
    public static final String BULK_ALL = "/all";
    public static final String BULK_TYPE = "/type";
    public static final String BULK_SERIES = "/series";

    private static final Map<String, Supplier<Stream<Map.Entry<String, TrainerMobData>>>> typeSuggestions = new HashMap<>();
    private static final Map<String, Supplier<Stream<Map.Entry<String, TrainerMobData>>>> seriesSuggestions = new HashMap<>();

    public static void initSuggestions() {
        var tm = RCTMod.getInstance().getTrainerManager();
        var sm = RCTMod.getInstance().getSeriesManager();

        seriesSuggestions.clear();
        typeSuggestions.clear();

        sm.getSeriesIds().stream().forEach(s -> seriesSuggestions.put(
            String.format("%s/%s", BULK_SERIES, s),
            () -> tm.getAllData().filter(e -> e.getValue().isOfSeries(s))));
        
        tm.getAllData()
            .map(e -> e.getValue().getType()).distinct()
            .forEach(t -> typeSuggestions.put(String.format("%s/%s", BULK_TYPE, t.id()), () -> tm.getAllData().filter(e -> e.getValue().getType().equals(t))));
    }

    public static boolean executeBulk(String arg, Consumer<Stream<Map.Entry<String, TrainerMobData>>> consumer) {
        if(BULK_ALL.equals(arg)) {
            consumer.accept(RCTMod.getInstance().getTrainerManager().getAllData());
            return true;
        }

        if(seriesSuggestions.containsKey(arg)) {
            consumer.accept(seriesSuggestions.get(arg).get());
            return true;
        }

        if(typeSuggestions.containsKey(arg)) {
            consumer.accept(typeSuggestions.get(arg).get());
            return true;
        }

        return false;
    }

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

    public static CompletableFuture<Suggestions> get_trainer_suggestions_bulk(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        var remaining = builder.getRemaining();
        var tm = RCTMod.getInstance().getTrainerManager();

        if(BULK_ALL.startsWith(remaining)) {
            builder.suggest(BULK_ALL);
        }

        typeSuggestions.keySet().stream()
            .filter(key -> key.startsWith(remaining))
            .forEach(builder::suggest);

        seriesSuggestions.keySet().stream()
            .filter(key -> key.startsWith(remaining))
            .forEach(builder::suggest);
        
        tm.getAllData()
            .map(e -> e.getKey())
            .filter(tid -> tid.startsWith(remaining))
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> get_series_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        Stream.concat(Stream.of(String.format("/%s", SeriesManager.EMPTY_SERIES_ID)), RCTMod.getInstance().getSeriesManager().getSeriesIds().stream()).forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> get_progress_trainer_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        var sm = RCTMod.getInstance().getSeriesManager();
        var tm = RCTMod.getInstance().getTrainerManager();
        var remaining = builder.getRemainingLowerCase();
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

        tidStream.distinct()
            .filter(tid -> tid.startsWith(remaining))
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> get_type_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        var remaining = builder.getRemainingLowerCase();
        
        TrainerType.ids().stream()
            .filter(id -> id.startsWith(remaining))
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private static String[] GRAPH_FLAGS = {"include_defeated", "include_optionals", "include_singles"};
    public static final int GF_INCLUDE_DEFEATED = 1<<(GRAPH_FLAGS.length - 1);
    public static final int GF_INCLUDE_OPTIONALS = 1<<(GRAPH_FLAGS.length - 2);
    public static final int GF_INCLUDE_SINGLES = 1<<(GRAPH_FLAGS.length - 3);

    public static int getGraphFlags(String in) throws IllegalArgumentException {
        var inFlags = in.toLowerCase().split("\s+");
        int flags = 0;

        for(var f : inFlags) {
            var match = false;

            for(int i = 0; i < GRAPH_FLAGS.length; i++) {
                if(f.equals(GRAPH_FLAGS[i])) {
                    flags |= (1<<(GRAPH_FLAGS.length - (i + 1)));
                    match = true;
                    break;
                }
            }

            if(!match) {
                throw new IllegalArgumentException(String.format("'%s' is not a valid graph flag", f));
            }
        }

        return flags;
    }

    public static CompletableFuture<Suggestions> get_graph_flag_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        var newFlag = builder.getRemaining().endsWith(" ");
        var remaining = builder.getRemainingLowerCase().split("\s+");
        var sb = new StringBuilder();

        for(int i = 0; i < remaining.length + (newFlag ? 0 : -1); i++) {
            sb.append(remaining[i]).append(' ');
        }

        var pre = sb.toString();

        Stream.of(GRAPH_FLAGS)
            .filter(flag -> Stream.of(remaining).noneMatch(f -> flag.equals(f)) && (newFlag || Stream.of(remaining).anyMatch(f -> flag.startsWith(f))))
            .forEach(f -> builder.suggest(pre + f));

        return builder.buildFuture();
    }

    private SuggestionUtils() {
    }
}
