package com.antonback.emirecipehud;

import net.neoforged.neoforge.common.ModConfigSpec;

public class EmiRecipeHudConfig {
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.IntValue COLUMNS;
    public static ModConfigSpec.IntValue ROWS;
    public static ModConfigSpec.IntValue MARGIN_LEFT;
    public static ModConfigSpec.IntValue MARGIN_TOP;
    public static ModConfigSpec.IntValue MARGIN_RIGHT;
    public static ModConfigSpec.IntValue MARGIN_BOTTOM;
    public static ModConfigSpec.EnumValue<Horizontal> H_ALIGN;
    public static ModConfigSpec.EnumValue<Vertical> V_ALIGN;

    public static ModConfigSpec.BooleanValue SHOW_HUD;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("hud");

        SHOW_HUD = builder.comment("Показывать ли HUD").define("show_hud", true);
        COLUMNS = builder.defineInRange("columns", 12, 1, 100);
        ROWS = builder.defineInRange("rows", 6, 1, 100);

        builder.push("margins");
        MARGIN_LEFT = builder.defineInRange("left", 2, 0, 2000);
        MARGIN_TOP = builder.defineInRange("top", 2, 0, 2000);
        MARGIN_RIGHT = builder.defineInRange("right", 2, 0, 2000);
        MARGIN_BOTTOM = builder.defineInRange("bottom", 2, 0, 2000);
        builder.pop();

        H_ALIGN = builder.defineEnum("horizontal_align", Horizontal.LEFT);
        V_ALIGN = builder.defineEnum("vertical_align", Vertical.TOP);

        builder.pop();
        SPEC = builder.build();
    }

    public enum Horizontal { LEFT, CENTER, RIGHT }
    public enum Vertical { TOP, CENTER, BOTTOM }
}