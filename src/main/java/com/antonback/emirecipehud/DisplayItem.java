package com.antonback.emirecipehud;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.ProgressState;

public class DisplayItem {
    public final EmiStack stack;
    public long amount;
    public final ProgressState progress;
    public boolean isGoal;
    public boolean isIntermediate;
    public long possibleBatches;
    public long neededBatches;

    public DisplayItem(EmiStack stack, long amount, ProgressState progress, boolean isGoal, boolean isIntermediate, long neededBatches) {
        this.stack = stack;
        this.amount = amount;
        this.progress = progress;
        this.isGoal = isGoal;
        this.isIntermediate = isIntermediate;
        this.neededBatches = neededBatches;
        this.possibleBatches = 0;
    }
}