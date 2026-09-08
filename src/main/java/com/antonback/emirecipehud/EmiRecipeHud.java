package com.antonback.emirecipehud;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

@Mod("emirecipehud")
public class EmiRecipeHud {
    public EmiRecipeHud(ModContainer container) {
        // Регистрируем клиентский конфиг напрямую в контейнер мода
        container.registerConfig(ModConfig.Type.CLIENT, EmiRecipeHudConfig.SPEC);
    }
}