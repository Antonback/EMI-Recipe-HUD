package com.antonback.emirecipehud;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.*;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.MicroTextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.*;

@EventBusSubscriber(modid = "emirecipehud", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientHudEvents {

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        // Регистрируем слой HUD под панелью быстрого доступа (Hotbar)
        event.registerBelow(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath("emirecipehud", "emi_recipe_hud"),
                (guiGraphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();

                    if (!EmiRecipeHudConfig.SHOW_HUD.get() || mc.player == null || mc.screen != null ||
                            BoM.tree == null || BoM.tree.goal == null || !BoM.craftingMode) {
                        return;
                    }

                    try {
                        EmiPlayerInventory playerInv = EmiPlayerInventory.of(mc.player);
                        BoM.tree.calculateProgress(playerInv);

                        if (BoM.tree.goal.progress == ProgressState.COMPLETED) {
                            BoM.tree = null;
                            BoM.craftingMode = false;
                            return;
                        }

                        Map<EmiStack, DisplayItem> displayMap = new LinkedHashMap<>();

                        collectFromTree(BoM.tree.goal, displayMap, playerInv, true);

                        for (FlatMaterialCost cost : BoM.tree.cost.costs.values()) {
                            EmiStack stack = cost.ingredient.getEmiStacks().get(0);
                            if (cost.amount > 0) {
                                addToMap(displayMap, stack, cost.amount, ProgressState.UNSTARTED, false, false, 0, 0);
                            }
                        }

                        for (ChanceMaterialCost cost : BoM.tree.cost.chanceCosts.values()) {
                            EmiStack stack = cost.ingredient.getEmiStacks().get(0);
                            long amount = cost.getEffectiveAmount();
                            if (amount > 0) {
                                addToMap(displayMap, stack, amount, ProgressState.UNSTARTED, false, false, 0, -1);
                            }
                        }

                        List<DisplayItem> fullList = new ArrayList<>(displayMap.values());
                        if (fullList.isEmpty()) return;

                        int maxCols = EmiRecipeHudConfig.COLUMNS.get();
                        int maxRows = EmiRecipeHudConfig.ROWS.get();
                        int capacity = maxCols * maxRows;

                        List<DisplayItem> toRender;
                        if (fullList.size() > capacity) {
                            toRender = fullList.subList(fullList.size() - capacity, fullList.size());
                        } else {
                            toRender = fullList;
                        }

                        int totalToShow = toRender.size();
                        int cols = Math.min(totalToShow, maxCols);
                        int rows = (int) Math.ceil((double) totalToShow / cols);

                        int boxW = (cols * 18) + 10;
                        int boxH = (rows * 18) + 10;
                        int screenW = mc.getWindow().getGuiScaledWidth();
                        int screenH = mc.getWindow().getGuiScaledHeight();

                        int startX = calculateX(EmiRecipeHudConfig.H_ALIGN.get(), screenW, boxW, EmiRecipeHudConfig.MARGIN_LEFT.get(), EmiRecipeHudConfig.MARGIN_RIGHT.get());
                        int startY = calculateY(EmiRecipeHudConfig.V_ALIGN.get(), screenH, boxH, EmiRecipeHudConfig.MARGIN_TOP.get(), EmiRecipeHudConfig.MARGIN_BOTTOM.get());

                        EmiDrawContext emiContext = EmiDrawContext.wrap(guiGraphics);
                        emiContext.fill(startX, startY, boxW, boxH, 0x66000000);
                        emiContext.fill(startX, startY, boxW, 1, 0x33ffffff);
                        emiContext.fill(startX, startY + boxH - 1, boxW, 1, 0x33ffffff);
                        emiContext.fill(startX, startY, 1, boxH, 0x33ffffff);
                        emiContext.fill(startX + boxW - 1, startY, 1, boxH, 0x33ffffff);

                        int curX = startX + 6, curY = startY + 6, count = 0;
                        for (DisplayItem item : toRender) {
                            if (count >= cols) { curX = startX + 6; curY += 18; count = 0; }
                            emiContext.drawStack(item.stack, curX, curY);

                            int color;
                            if (item.possibleBatches == -1) color = 0xEBA400;
                            else if (item.isGoal || item.progress == ProgressState.COMPLETED) color = 0x915900;
                            else if (item.isIntermediate) {
                                if (item.possibleBatches >= item.neededBatches && item.neededBatches > 0) color = 0x00918E;
                                else if (item.possibleBatches > 0) color = 0x790091;
                                else color = 0x915900;
                            } else color = 0x911300;

                            MicroTextRenderer.render(emiContext, item.amount, item.stack.getKey() instanceof Fluid, 17, curX + 17, curY + 18, color | 0xFF000000);
                            curX += 18; count++;
                        }
                    } catch (Exception e) {}
                }
        );
    }

    private static void addToMap(Map<EmiStack, DisplayItem> map, EmiStack stack, long amount, ProgressState progress, boolean isGoal, boolean isIntermediate, long neededBatches, long possibleBatches) {
        EmiStack key = null;
        for (EmiStack s : map.keySet()) {
            if (s.isEqual(stack)) {
                key = s;
                break;
            }
        }

        if (key != null) {
            DisplayItem existing = map.get(key);
            existing.amount += amount;
            if (isGoal) existing.isGoal = true;
            if (isIntermediate) existing.isIntermediate = true;
            existing.neededBatches += neededBatches;
            if (possibleBatches > 0) {
                existing.possibleBatches = (existing.possibleBatches <= 0) ? possibleBatches : Math.min(existing.possibleBatches, possibleBatches);
            }
        } else {
            DisplayItem item = new DisplayItem(stack, amount, progress, isGoal, isIntermediate, neededBatches);
            item.possibleBatches = possibleBatches;
            map.put(stack, item);
        }
    }

    private static int calculateX(EmiRecipeHudConfig.Horizontal align, int s, int b, int l, int r) {
        if (align == EmiRecipeHudConfig.Horizontal.CENTER) return (s - b) / 2;
        if (align == EmiRecipeHudConfig.Horizontal.RIGHT) return s - b - r;
        return l;
    }

    private static int calculateY(EmiRecipeHudConfig.Vertical align, int s, int b, int t, int bot) {
        if (align == EmiRecipeHudConfig.Vertical.CENTER) return (s - b) / 2;
        if (align == EmiRecipeHudConfig.Vertical.BOTTOM) return s - b - bot;
        return t;
    }

    private static void collectFromTree(MaterialNode n, Map<EmiStack, DisplayItem> m, EmiPlayerInventory i, boolean g) {
        if (n == null || n.ingredient.isEmpty()) return;
        EmiStack s = n.ingredient.getEmiStacks().get(0);
        if (g || (n.recipe != null && n.progress != ProgressState.COMPLETED)) {
            long a = n.totalNeeded;
            if (a > 0) {
                long possible = (n.recipe != null) ? calculatePossibleBatches(n, i) : 0;
                addToMap(m, s, a, n.progress, g, n.recipe != null, n.neededBatches, possible);
            }
        }
        if (n.children != null) for (MaterialNode c : n.children) collectFromTree(c, m, i, false);
    }

    private static long calculatePossibleBatches(MaterialNode n, EmiPlayerInventory inv) {
        if (n.children == null || n.children.isEmpty()) return 0;
        long p = Long.MAX_VALUE;
        for (MaterialNode c : n.children) {
            long h = 0;
            for (EmiStack s : c.ingredient.getEmiStacks()) {
                EmiStack found = inv.inventory.get(s);
                if (found != null) h += found.getAmount();
            }
            if (c.amount > 0) p = Math.min(p, h / c.amount);
        }
        return p == Long.MAX_VALUE ? 0 : p;
    }
}