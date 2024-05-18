package com.gitlab.srcmc.rctmod.client.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.client.screens.widgets.PlayerInfoWidget;
import com.gitlab.srcmc.rctmod.client.screens.widgets.TrainerListWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TrainerCardScreen extends Screen {
    private static final int CHECKBOX_X = 204;
    private static final int CHECKBOX_Y = 16;
    private static final int CHECKBOX_SIZE = 16;
    private static final int TYPE_BUTTON_X = 104;
    private static final int TYPE_BUTTON_Y = 16;
    private static final int TYPE_BUTTON_W = 100;
    private static final int TYPE_BUTTON_H = 16;
    private static final int NEXT_PAGE_BUTTON_X = 208;
    private static final int NEXT_PAGE_BUTTON_Y = 112;
    private static final int NEXT_PAGE_BUTTON_SIZE = 16;
    private static final String ALL_TRAINER_TYPES_STR = "ALL";

    private PlayerInfoWidget playerInfo;
    private TrainerListWidget trainerList;
    private CycleButton<String> trainerTypeButton;
    private Button nextPageButton;
    private Button prevPageButton;
    private Checkbox showUndefeated;

    public TrainerCardScreen() {
        super(Component.literal("Trainer Card"));
        var mc = Minecraft.getInstance();
        this.font = mc.font;        
    }

    @Override
    protected void init() {
        super.init();

        var window = Minecraft.getInstance().getWindow();
        this.width = window.getWidth();
        this.height = window.getHeight();

        var types = new ArrayList<String>();
        types.add(ALL_TRAINER_TYPES_STR);
        types.addAll(Stream.of(TrainerMobData.Type.values()).map(type -> type.name()).toList());

        this.playerInfo = new PlayerInfoWidget(this.font);
        this.trainerList = new TrainerListWidget(this.playerInfo, this.font, sortedTrainerIds());
        this.showUndefeated = new Checkbox(CHECKBOX_X, CHECKBOX_Y, CHECKBOX_SIZE, CHECKBOX_SIZE, Component.empty(), true);
        this.trainerTypeButton = CycleButton.<String>builder(t -> Component.literal(t))
            .withValues(types).withInitialValue(ALL_TRAINER_TYPES_STR)
            .create(TYPE_BUTTON_X, TYPE_BUTTON_Y, TYPE_BUTTON_W, TYPE_BUTTON_H, Component.empty());
        
        this.nextPageButton = Button
            .builder(Component.literal(">"), this::onNextPage)
            .pos(NEXT_PAGE_BUTTON_X, NEXT_PAGE_BUTTON_Y)
            .size(NEXT_PAGE_BUTTON_SIZE, NEXT_PAGE_BUTTON_SIZE).build();

        this.prevPageButton = Button
            .builder(Component.literal("<"), this::onPrevPage)
            .pos(NEXT_PAGE_BUTTON_X - NEXT_PAGE_BUTTON_SIZE, NEXT_PAGE_BUTTON_Y)
            .size(NEXT_PAGE_BUTTON_SIZE, NEXT_PAGE_BUTTON_SIZE).build();

        this.addRenderableOnly(this.playerInfo);
        this.addRenderableWidget(this.trainerList);
        this.addRenderableWidget(this.showUndefeated);
        this.addRenderableWidget(this.trainerTypeButton);
        this.addRenderableWidget(this.prevPageButton);
        this.addRenderableWidget(this.nextPageButton);
    }

    private void onNextPage(Button button) {
        this.trainerList.setPage(this.trainerList.getPage() + 1);
    }

    private void onPrevPage(Button button) {
        this.trainerList.setPage(this.trainerList.getPage() - 1);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        var mc = Minecraft.getInstance();

        if(mc.options.keyInventory.matches(key, scancode)) {
            mc.setScreen(null);
            return true;
        }

        return super.keyPressed(key, scancode, mods);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if(i == 1) {
            var mc = Minecraft.getInstance();
            mc.setScreen(null);
            return true;
        }

        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        this.nextPageButton.active = this.trainerList.getPage() < this.trainerList.getMaxPage();
        this.prevPageButton.active = this.trainerList.getPage() > 0;

        var trainerTypeStr = this.trainerTypeButton.getValue();
        var showUndefeated = this.showUndefeated.selected();
        var showAllTypes = trainerTypeStr.equals(ALL_TRAINER_TYPES_STR);

        if(!showAllTypes) {
            var trainerType = TrainerMobData.Type.valueOf(trainerTypeStr);
            this.trainerList.setTrainerType(trainerType);
        }

        this.trainerList.setShowAllTypes(showAllTypes);
        this.trainerList.setShowUndefeated(showUndefeated);

        this.playerInfo.tick();
        this.trainerList.tick();
    }

    private static List<String> sortedTrainerIds() {
        var tdm = RCTMod.get().getTrainerManager();

        return tdm.getAllData().map(entry -> entry.getKey()).sorted((k1, k2) -> {
            var t1 = tdm.getData(k1);
            var t2 = tdm.getData(k2);
            var c = t1.getTeam().getMembers().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0)
                  - t2.getTeam().getMembers().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0);
            
            if(c == 0) {
                c = t1.getTeam().getDisplayName().compareTo(t2.getTeam().getDisplayName());

                if(c == 0) {
                    c = k1.compareTo(k2);
                }
            }

            return c;
        }).toList();
    }
}
