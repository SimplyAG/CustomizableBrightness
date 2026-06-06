package com.simplyag.customizablebrightness;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomizableBrightnessClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("customizable-brightness");
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("customizable-brightness", "brightness"));
    private static final String KEY_CYCLE_BRIGHTNESS = "key.customizable-brightness.cycle";

    private static BrightnessConfig config;
    private static double currentGamma = 1.0;
    private static int currentBrightnessIndex = 1;

    private KeyMapping cycleBrightnessKey;
    private boolean initialized = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Customizable Brightness mod");

        // Load configuration
        config = new BrightnessConfig();
        currentBrightnessIndex = config.getLastBrightnessIndex();
        currentGamma = config.getBrightnessAtIndex(currentBrightnessIndex);

        // Register keybinding
        cycleBrightnessKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                KEY_CYCLE_BRIGHTNESS,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                KEY_CATEGORY
        ));

        // Register tick event for key detection and initialization
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.options == null) {
                return;
            }

            // One-time initialization to set the gamma from config
            if (!initialized) {
                client.options.gamma().set(currentGamma);
                initialized = true;
                LOGGER.info("Set initial brightness to: {}x ({}%)", currentGamma, (int)(currentGamma * 100));
            }

            // Check if the keybind was pressed
            while (cycleBrightnessKey.consumeClick()) {
                cycleBrightness(client);
            }
        });

        LOGGER.info("Customizable Brightness mod initialized successfully");
    }

    private void cycleBrightness(net.minecraft.client.Minecraft client) {
        // Cycle to next brightness level
        currentBrightnessIndex = (currentBrightnessIndex + 1) % config.getBrightnessStopsCount();

        // Get the new brightness value
        double brightness = config.getBrightnessAtIndex(currentBrightnessIndex);

        // Apply the brightness (this will trigger our mixin)
        client.options.gamma().set(brightness);

        // Save the current index
        config.saveLastBrightnessIndex(currentBrightnessIndex);

        // Display feedback to player
        displayBrightnessFeedback(client, brightness);
    }

    private void displayBrightnessFeedback(net.minecraft.client.Minecraft client, double brightness) {
        // Always display as percentage (0% to 1000%+)
        int percentage = (int)(brightness * 100);
        String message = "\u00a7eBrightness: \u00a7f" + percentage + "%";

        // Send message to action bar
        if (client.gui != null) {
            client.gui.setOverlayMessage(Component.literal(message), true);
        }
    }

    /**
     * Called by the mixin to get the current gamma value
     */
    public static double getCurrentGamma() {
        return currentGamma;
    }

    /**
     * Called by the mixin to set the current gamma value
     */
    public static void setCurrentGamma(double gamma) {
        currentGamma = gamma;
    }

    /**
     * Get the config instance (for GUI)
     */
    public static BrightnessConfig getConfig() {
        return config;
    }
}
