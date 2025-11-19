package com.simplyag.customizablebrightness;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomizableBrightnessClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("customizable-brightness");
    private static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(Identifier.of("customizable-brightness", "brightness"));
    private static final String KEY_CYCLE_BRIGHTNESS = "key.customizable-brightness.cycle";

    private static BrightnessConfig config;
    private static double currentGamma = 1.0;
    private static int currentBrightnessIndex = 1;

    private KeyBinding cycleBrightnessKey;
    private boolean initialized = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Customizable Brightness mod");

        // Load configuration
        config = new BrightnessConfig();
        currentBrightnessIndex = config.getLastBrightnessIndex();
        currentGamma = config.getBrightnessAtIndex(currentBrightnessIndex);

        // Register keybinding
        cycleBrightnessKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_CYCLE_BRIGHTNESS,
                InputUtil.Type.KEYSYM,
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
                client.options.getGamma().setValue(currentGamma);
                initialized = true;
                LOGGER.info("Set initial brightness to: {}x ({}%)", currentGamma, (int)(currentGamma * 100));
            }

            // Check if the keybind was pressed
            while (cycleBrightnessKey.wasPressed()) {
                cycleBrightness(client);
            }
        });

        LOGGER.info("Customizable Brightness mod initialized successfully");
    }

    private void cycleBrightness(net.minecraft.client.MinecraftClient client) {
        // Cycle to next brightness level
        currentBrightnessIndex = (currentBrightnessIndex + 1) % config.getBrightnessStopsCount();

        // Get the new brightness value
        double brightness = config.getBrightnessAtIndex(currentBrightnessIndex);

        // Apply the brightness (this will trigger our mixin)
        client.options.getGamma().setValue(brightness);

        // Save the current index
        config.saveLastBrightnessIndex(currentBrightnessIndex);

        // Display feedback to player
        displayBrightnessFeedback(client, brightness);

        LOGGER.info("Brightness changed to: {}x ({}%)", brightness, (int)(brightness * 100));
    }

    private void displayBrightnessFeedback(net.minecraft.client.MinecraftClient client, double brightness) {
        // Always display as percentage (0% to 1000%+)
        int percentage = (int)(brightness * 100);
        String message = "§eBrightness: §f" + percentage + "%";

        // Send message to action bar
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message), true);
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

    /**
     * Save configuration (called from GUI)
     */
    public static void saveConfiguration() {
        if (config != null) {
            config.saveConfig();
            LOGGER.info("Configuration saved from GUI");
        }
    }
}
