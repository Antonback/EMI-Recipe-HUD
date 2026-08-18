package com.antonback.emirecipehud;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.*;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.MicroTextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = "emirecipehud", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientHudEvents {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelowAll("emi_recipe_hud", (gui, guiGraphics, partialTick, width, height) -> {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null || mc.screen != null || BoM.tree == null || BoM.tree.goal == null || !BoM.craftingMode) {
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

                // 1. Добавляем обычное сырье
                for (FlatMaterialCost cost : BoM.tree.cost.costs.values()) {
                    EmiStack stack = cost.ingredient.getEmiStacks().get(0);
                    if (cost.amount > 0 && !displayMap.containsKey(stack)) {
                        displayMap.put(stack, new DisplayItem(stack, cost.amount, ProgressState.UNSTARTED, false, false, 0));
                    }
                }

                // 2. ДОБАВЛЯЕМ ШАНСОВЫЕ ПРЕДМЕТЫ (GregTech и др.)
                for (ChanceMaterialCost cost : BoM.tree.cost.chanceCosts.values()) {
                    EmiStack stack = cost.ingredient.getEmiStacks().get(0);
                    long amount = cost.getEffectiveAmount();
                    if (amount > 0 && !displayMap.containsKey(stack)) {
                        DisplayItem item = new DisplayItem(stack, amount, ProgressState.UNSTARTED, false, false, 0);
                        // Помечаем предмет как шансовый (для цвета)
                        item.possibleBatches = -1;
                        displayMap.put(stack, item);
                    }
                }

                List<DisplayItem> toRender = new ArrayList<>(displayMap.values());
                if (toRender.isEmpty()) return;

                int maxCols = EmiRecipeHudConfig.COLUMNS.get();
                int maxRows = EmiRecipeHudConfig.ROWS.get();
                int totalItems = Math.min(toRender.size(), maxCols * maxRows);
                int cols = Math.min(totalItems, maxCols);
                int rows = (int) Math.ceil((double) totalItems / cols);

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
                for (int i = 0; i < totalItems; i++) {
                    DisplayItem item = toRender.get(i);
                    if (count >= cols) { curX = startX + 6; curY += 18; count = 0; }
                    emiContext.drawStack(item.stack, curX, curY);

                    int color;
                    if (item.possibleBatches == -1) {
                        color = 0xEBA400; // Оранжево-желтый для шансовых предметов (как в EMI)
                    } else if (item.isGoal || item.progress == ProgressState.COMPLETED) {
                        color = 0x915900; // Оранжевый
                    } else if (item.isIntermediate) {
                        if (item.possibleBatches >= item.neededBatches && item.neededBatches > 0) color = 0x00918E; // Бирюзовый
                        else if (item.possibleBatches > 0) color = 0x790091; // Фиолетовый
                        else color = 0x915900;
                    } else {
                        color = 0x911300; // Красный
                    }

                    MicroTextRenderer.render(emiContext, item.amount, item.stack.getKey() instanceof Fluid, 17, curX + 17, curY + 18, color | 0xFF000000);
                    curX += 18; count++;
                }
            } catch (Exception e) {}
        });
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
                if (!m.containsKey(s)) {
                    DisplayItem it = new DisplayItem(s, a, n.progress, g, n.recipe != null, n.neededBatches);
                    if (n.recipe != null) it.possibleBatches = calculatePossibleBatches(n, i);
                    m.put(s, it);
                } else m.get(s).amount += a;
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