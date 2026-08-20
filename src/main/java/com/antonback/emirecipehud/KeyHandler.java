package com.antonback.emirecipehud;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "emirecipehud", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyHandler {
    public static final KeyMapping TOGGLE_HUD = new KeyMapping(
            "key.emirecipehud.toggle",
            GLFW.GLFW_KEY_F6,
            "key.categories.emirecipehud"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_HUD);
    }

    @Mod.EventBusSubscriber(modid = "emirecipehud", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class InputHandler {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (TOGGLE_HUD.consumeClick()) {
                boolean newState = !EmiRecipeHudConfig.SHOW_HUD.get();
                EmiRecipeHudConfig.SHOW_HUD.set(newState);
                EmiRecipeHudConfig.SPEC.save();
            }
        }
    }
}