package com.simplyag.customizablebrightness;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//? if >=1.16.5 {
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
//?} else {
/*import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;*/
//?}
//? if >=1.19 {
import net.minecraft.text.Text;
//?} else {
/*import net.minecraft.text.LiteralText;*/
//?}
//? if >=1.21.9
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
//? if >=1.17 {
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//?} else {
/*import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;*/
//?}

public class CustomizableBrightnessClient implements ClientModInitializer {
    //? if >=1.17 {
    private static final Logger LOGGER = LoggerFactory.getLogger("customizable-brightness");
    //?} else {
    /*private static final Logger LOGGER = LogManager.getLogger("customizable-brightness");*/
    //?}
    //? if >=1.21.9 {
    private static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(Identifier.of("customizable-brightness", "brightness"));
    //?} else {
    /*private static final String KEY_CATEGORY = "category.customizable-brightness";*/
    //?}
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
                //? if >=1.19 {
                client.options.getGamma().setValue(currentGamma);
                //?} else {
                /*client.options.gamma = currentGamma;*/
                //?}
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

        // Apply the brightness
        //? if >=1.19 {
        client.options.getGamma().setValue(brightness);
        //?} else {
        /*client.options.gamma = brightness;*/
        //?}

        // Save the current index
        config.saveLastBrightnessIndex(currentBrightnessIndex);

        // Display feedback to player
        displayBrightnessFeedback(client, brightness);

        LOGGER.info("Brightness changed to: {}x ({}%)", brightness, (int)(brightness * 100));
    }

    private void displayBrightnessFeedback(net.minecraft.client.MinecraftClient client, double brightness) {
        int percentage = (int)(brightness * 100);
        String message = "\u00a7eBrightness: \u00a7f" + percentage + "%";

        if (client.player != null) {
            //? if >=1.19 {
            client.player.sendMessage(Text.literal(message), true);
            //?} else if >=1.16 {
            /*client.player.sendMessage(new LiteralText(message), true);*/
            //?} else {
            /*client.player.sendMessage(new LiteralText(message));*/
            //?}
        }
    }

    public static double getCurrentGamma() {
        return currentGamma;
    }

    public static void setCurrentGamma(double gamma) {
        currentGamma = gamma;
    }

    public static BrightnessConfig getConfig() {
        return config;
    }

    public static void saveConfiguration() {
        if (config != null) {
            config.saveConfig();
            LOGGER.info("Configuration saved from GUI");
        }
    }
}
