package com.antonback.emirecipehud;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "emirecipehud", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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

    @EventBusSubscriber(modid = "emirecipehud", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
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