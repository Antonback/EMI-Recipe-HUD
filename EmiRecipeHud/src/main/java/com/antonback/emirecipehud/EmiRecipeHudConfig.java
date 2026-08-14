package com.antonback.emirecipehud;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class EmiRecipeHudConfig {
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.IntValue COLUMNS;
    public static ForgeConfigSpec.IntValue ROWS;
    public static ForgeConfigSpec.IntValue MARGIN_LEFT;
    public static ForgeConfigSpec.IntValue MARGIN_TOP;
    public static ForgeConfigSpec.IntValue MARGIN_RIGHT;
    public static ForgeConfigSpec.IntValue MARGIN_BOTTOM;

    public static ForgeConfigSpec.EnumValue<Horizontal> H_ALIGN;
    public static ForgeConfigSpec.EnumValue<Vertical> V_ALIGN;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("hud");

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

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC);
    }

    public enum Horizontal { LEFT, CENTER, RIGHT }
    public enum Vertical { TOP, CENTER, BOTTOM }
}