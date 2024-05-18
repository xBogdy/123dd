package com.gitlab.srcmc.rctmod.client.screens.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.algorithm.IAlgorithm;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.service.TrainerManager;
import com.gitlab.srcmc.rctmod.client.ModClient;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TrainerListWidget extends AbstractScrollWidget {
    private static final int X = 96;
    private static final int Y = 32;
    private static final int W = 112;
    private static final int H = 72;
    private static final float INNER_SCALE = 0.65f;
    private static final int UPDATES_PER_TICK = 100;
    private static final int ENTRIES_PER_PAGE = 100;

    private class Entry {
        public final StringWidget name;
        public final StringWidget count;

        public Entry(StringWidget name, StringWidget count) {
            this.name = name;
            this.count = count;
        }

        public void render(GuiGraphics guiGraphics, int x, int y, float f) {
            this.name.render(guiGraphics, x, y, f);
            this.count.render(guiGraphics, x, y, f);
        }
    }

    private class UpdateState implements IAlgorithm {
        public final Map<Integer, List<Entry>> pages;
        public final PlayerState playerState;
        public int i, c, x, y, w, h;
        public TrainerManager tdm;
        private boolean realtime;

        public UpdateState() {
            this(new HashMap<>());
        }

        private UpdateState(Map<Integer, List<Entry>> pages) {
            this.realtime = pages == TrainerListWidget.this.pages;
            this.pages = pages;
            this.playerState = PlayerState.get(ModClient.get().getLocalPlayer().get());
            this.tdm = RCTMod.get().getTrainerManager();
            this.w = (int)((getWidth() - totalInnerPadding())/INNER_SCALE);
            this.h = TrainerListWidget.this.entryHeight;
        }

        @Override
        public void tick() {
            int u = 0, p = 0, r = 0;

            for(; this.i < trainerIds.size() && u < UPDATES_PER_TICK; this.i++, u++) {
                var trainerId = trainerIds.get(this.i);

                if(showAllTypes || this.tdm.getData(trainerId).getType() == trainerType) {
                    var count = this.playerState.getTrainerDefeatCount(trainerId);

                    if(showUndefeated || count > 0) {
                        p = this.c / ENTRIES_PER_PAGE;
                        r = this.c % ENTRIES_PER_PAGE;
                        this.y = r * this.h;

                        var name = count > 0 ? this.tdm.getData(trainerId).getTeam().getDisplayName() : "???";
                        name = String.format("%04d: %s", this.i + 1, name);

                        var nameWidget = new StringWidget(this.x, this.y, this.w, this.h, toComponent(name), font).alignLeft();
                        var countWidget = new StringWidget(this.x, this.y, this.w, this.h,
                            count > 9000 ? toComponent(">9k") :
                            count > 999 ? toComponent((count/1000) + "k") :
                            toComponent(count), font).alignRight();

                        this.pages.computeIfAbsent(p, ArrayList::new).add(new Entry(nameWidget, countWidget));
                        this.c++;

                        if(realtime) {
                            TrainerListWidget.this.maxPage = p;
                        }
                    }
                }
            }

            if(!realtime && this.finished()) {
                TrainerListWidget.this.pages = pages;
                TrainerListWidget.this.maxPage = p;
                TrainerListWidget.this.page = Math.max(0, Math.min(p, TrainerListWidget.this.page));
            }
        }

        @Override
        public boolean finished() {
            return this.i >= TrainerListWidget.this.trainerIds.size();
        }
    }

    private Font font;
    private int innerHeight, entryHeight;
    private List<String> trainerIds;
    private boolean showUndefeated, showAllTypes = true;
    private TrainerMobData.Type trainerType;
    private Map<Integer, List<Entry>> pages = new HashMap<>();
    private UpdateState updateState;
    private int page, maxPage;

    public TrainerListWidget(AbstractWidget parent, Font font, List<String> trainerIds) {
        super(parent.getX() + X, parent.getY() + Y, W, H, Component.empty());
        this.font = font;
        this.trainerIds = new ArrayList<>(trainerIds);
        this.innerHeight = parent.getHeight() - this.totalInnerPadding();
        this.entryHeight = this.getHeight()/8;
        this.updateState = new UpdateState(this.pages);
    }

    public void tick() {
        if(this.updateState.finished()) {
            this.updateState = new UpdateState();
        } else {
            this.updateState.tick();
        }

        this.innerHeight = Math.max(
            getHeight() - totalInnerPadding(),
            this.pages.getOrDefault(this.page, List.of()).size()*this.entryHeight);
    }

    private void renderEntries(GuiGraphics guiGraphics, int x, int y, float f) {
        for(var entry : this.pages.getOrDefault(this.page, List.of())) {
            entry.render(guiGraphics, x, y, f);
        }
    }

    public int getPage() {
        return this.page;
    }

    public int getMaxPage() {
        return this.maxPage;
    }

    public boolean getShowUndefeated() {
        return this.showUndefeated;
    }

    public TrainerMobData.Type getTrainerType() {
        return this.trainerType;
    }

    public boolean getShowAllTypes() {
        return this.showAllTypes;
    }

    public void setPage(int page) {
        var nextPage = Math.max(0, Math.min(this.maxPage, page));

        if(nextPage != this.page) {
            this.page = nextPage;
            this.setScrollAmount(0);
        }
    }

    public void setShowUndefeated(boolean showUndefeated) {
        if(this.showUndefeated != showUndefeated) {
            this.showUndefeated = showUndefeated;
            this.updateEntries();
        }
    }

    public void setTrainerType(TrainerMobData.Type trainerType) {
        if(this.trainerType != trainerType) {
            this.trainerType = trainerType;
            this.updateEntries();
        }
    }

    public void setShowAllTypes(boolean showAllTypes) {
        if(this.showAllTypes != showAllTypes) {
            this.showAllTypes = showAllTypes;
            this.updateEntries();
        }
    }

    private void updateEntries() {
        this.setScrollAmount(0);
        this.pages.clear();
        this.page = this.maxPage = 0;
        this.updateState = new UpdateState(this.pages);
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int x, int y, float f) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float) (this.getX() + this.innerPadding()), (float) (this.getY() + this.innerPadding()), 0.0F);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(INNER_SCALE, INNER_SCALE, 1f);
        this.renderEntries(guiGraphics, x, y, f);
        guiGraphics.pose().popPose();
        guiGraphics.pose().popPose();
    }

    @Override
    protected int getInnerHeight() {
        return (int)(this.innerHeight*INNER_SCALE);
    }

    @Override
    protected double scrollRate() {
        return 9.0;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO
    }

    private static<T> MutableComponent toComponent(T value) {
        return Component.literal(String.valueOf(value)).withStyle(ChatFormatting.GREEN);
    }
}
