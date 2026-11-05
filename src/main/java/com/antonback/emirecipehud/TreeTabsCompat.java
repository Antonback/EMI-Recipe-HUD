package com.antonback.emirecipehud;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.bom.MaterialTree;
import dev.emi.emi.bom.ProgressState;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeTabsCompat {
    private static boolean initialized = false;
    private static boolean available = false;

    private static Method tabsMethod;
    private static Method isParkedMethod;
    private static Field treeField;
    private static Field craftingModeField;

    private static void init() {
        if (initialized) return;
        initialized = true;
        try {
            Class<?> treeTabsClass = Class.forName("dev.roocky.emitreetabs.tab.TreeTabs");
            Class<?> treeTabClass = Class.forName("dev.roocky.emitreetabs.tab.TreeTab");

            tabsMethod = treeTabsClass.getMethod("tabs");
            isParkedMethod = treeTabsClass.getMethod("isParked", treeTabClass);
            treeField = treeTabClass.getField("tree");
            craftingModeField = treeTabClass.getField("craftingMode");

            available = true;
        } catch (Throwable t) {
            available = false;
        }
    }

    public static boolean isAvailable() {
        if (!initialized) init();
        return available;
    }

    public static List<MaterialTree> getCraftingTrees(EmiPlayerInventory playerInv) {
        if (!isAvailable()) return Collections.emptyList();

        try {
            List<?> tabs = (List<?>) tabsMethod.invoke(null);
            if (tabs == null || tabs.isEmpty()) {
                return Collections.emptyList();
            }

            List<MaterialTree> trees = new ArrayList<>();
            for (Object tab : tabs) {
                if (tab == null) continue;

                boolean craftingMode = craftingModeField.getBoolean(tab);
                boolean isParked = (boolean) isParkedMethod.invoke(null, tab);

                if (craftingMode && !isParked) {
                    MaterialTree tree = (MaterialTree) treeField.get(tab);
                    if (tree != null && tree.goal != null) {
                        try {
                            tree.calculateProgress(playerInv);
                        } catch (Exception ignored) {}

                        if (tree.goal.progress != ProgressState.COMPLETED) {
                            trees.add(tree);
                        }
                    }
                }
            }
            return trees;
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }
}