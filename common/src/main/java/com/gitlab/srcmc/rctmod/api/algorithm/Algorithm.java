package com.gitlab.srcmc.rctmod.api.algorithm;

import java.util.function.Supplier;

public class Algorithm implements IAlgorithm {
    public interface Action {
        void perform();
    }

    private Action ticker;
    private Supplier<Boolean> finishChecker;

    public Algorithm() {
        this(() -> {}, () -> true);
    }

    public Algorithm(Action ticker, Supplier<Boolean> finishChecker) {
        this.ticker = ticker;
        this.finishChecker = finishChecker;
    }

    @Override
    public void tick() {
        this.ticker.perform();
    }

    @Override
    public boolean finished() {
        return this.finishChecker.get();
    }
}
