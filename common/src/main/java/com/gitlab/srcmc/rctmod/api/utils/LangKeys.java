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
package com.gitlab.srcmc.rctmod.api.utils;

public final class LangKeys {
    // public static final String ITEMGROUP = "itemGroup.rctmod";
    // public static final String ITEM_TRAINER_CARD = "item.rctmod.trainer_card";
    // public static final String BLOCK_TRAINER_SPAWNER = "block.rctmod.trainer_spawner";
    // public static final String ENTITY_TRAINER_ASSOCIATION = "entity.rctmod.trainer_association";
    // public static final String ENTITY_TRAINER = "entity.rctmod.trainer";

    public static final String GUI_TRAINER_CARD_TITLE = "gui.rctmod.trainer_card.title";
    public static final String GUI_TRAINER_CARD_LEVEL_CAP = "gui.rctmod.trainer_card.level_cap";
    public static final String GUI_TRAINER_CARD_TOTAL = "gui.rctmod.trainer_card.total";
    public static final String GUI_TRAINER_CARD_LOADING = "gui.rctmod.trainer_card.loading";
    public static final String GUI_TRAINER_CARD_TYPE = "gui.rctmod.trainer_card.type";
    public static final String GUI_TRAINER_CARD_IDENTITY = "gui.rctmod.trainer_card.identity";
    public static final String GUI_TRAINER_CARD_SIGNATURE_ITEM = "gui.rctmod.trainer_card.signature_item";
    public static final String GUI_TRAINER_CARD_REQUIRED_TRAINERS = "gui.rctmod.trainer_card.required_trainers";
    public static final String GUI_TRAINER_CARD_BIOMES = "gui.rctmod.trainer_card.biomes";
    public static final String GUI_TRAINER_CARD_OVERVIEW = "gui.rctmod.trainer_card.overview";
    public static final String GUI_TRAINER_CARD_SPAWNING = "gui.rctmod.trainer_card.spawning";
    public static final String GUI_TRAINER_CARD_TEAM = "gui.rctmod.trainer_card.team";

    public static final String GUI_TITLE_SERIES_STARTED = "gui.rctmod.title.series_started";
    public static final String GUI_TITLE_SERIES_PAUSED = "gui.rctmod.title.series_paused";
    public static final String GUI_TITLE_SERIES_CONTINUED = "gui.rctmod.title.series_continued";
    public static final String GUI_TITLE_SERIES_COMPLETED = "gui.rctmod.title.series_completed";
    public static final String GUI_TRAINER_ASSOCIATION_DIFFICULTY = "gui.rctmod.trainer_association.difficulty";
    public static final String GUI_TRAINER_ASSOCIATION_IMPORTANT = "gui.rctmod.trainer_association.important";
    public static final String GUI_TRAINER_ASSOCIATION_COMPLETED = "gui.rctmod.trainer_association.completed";
    public static final String GUI_TRAINER_ASSOCIATION_SERIES_RESET = "gui.rctmod.trainer_association.series_reset";
    public static final String GUI_TRAINER_ASSOCIATION_SERIES_CONTINUE = "gui.rctmod.trainer_association.series_continue";

    public static final String COMMANDS_ERRORS_CALLER_NOT_A_PLAYER = "commands.rctmod.errors.caller_not_a_player";
    public static final String COMMANDS_ERRORS_TARGET_NOT_A_PLAYER = "commands.rctmod.errors.target_not_a_player";
    public static final String COMMANDS_ERRORS_EMPTY_SERIES = "commands.rctmod.errors.empty_series";
    public static final String COMMANDS_FEEDBACK_SERIES_GRAPH = "commands.rctmod.feedback.series_graph";
    public static final String COMMANDS_FEEDBACK_CLICK_LINK = "commands.rctmod.feedback.click_link";

    public static String SERIES_TITLE(String seriesId) { return String.format("series.rctmod.%s.title", seriesId); }
    public static String SERIES_DESCRIPTION(String seriesId) { return String.format("series.rctmod.%s.description", seriesId); }
    public static String TRAINER_TYPE_TITLE(String typeId) { return String.format("trainer_type.rctmod.%s.title", typeId); }
    public static String TRAINER_NAME(String trainerId) { return String.format("trainer.rctmod.%s.name", trainerId); }
    public static String POKEMON_NICKNAME(String trainerId, int position) { return String.format("pokemon.rctmod.%s.%d.nickname", trainerId, position); }

    private LangKeys() {}
}
