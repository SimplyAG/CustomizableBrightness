package com.simplyag.customizablebrightness;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BrightnessConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("customizable-brightness");
    private static final String CONFIG_FILE_NAME = "customizable_brightness.properties";
    private static final String DEFAULT_BRIGHTNESS_STOPS = "0.0,1.0,2.0,5.0,10.0";
    private static final String BRIGHTNESS_STOPS_KEY = "brightness_stops";
    private static final String LAST_INDEX_KEY = "last_brightness_index";

    private final Path configPath;
    private final Properties properties;
    private List<Double> brightnessStops;
    private int lastBrightnessIndex;

    public BrightnessConfig() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        this.configPath = configDir.resolve(CONFIG_FILE_NAME);
        this.properties = new Properties();
        this.brightnessStops = new ArrayList<>();
        this.lastBrightnessIndex = 1; // Default to 100% (index 1)

        loadConfig();
    }

    private void loadConfig() {
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
                LOGGER.info("Loaded configuration from {}", configPath);

                // Parse brightness stops
                String stopsStr = properties.getProperty(BRIGHTNESS_STOPS_KEY, DEFAULT_BRIGHTNESS_STOPS);
                parseBrightnessStops(stopsStr);

                // Parse last brightness index
                String lastIndexStr = properties.getProperty(LAST_INDEX_KEY, "1");
                try {
                    lastBrightnessIndex = Integer.parseInt(lastIndexStr);
                    // Ensure index is valid
                    if (lastBrightnessIndex < 0 || lastBrightnessIndex >= brightnessStops.size()) {
                        lastBrightnessIndex = 1;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid last brightness index, using default");
                    lastBrightnessIndex = 1;
                }

            } catch (IOException e) {
                LOGGER.error("Failed to load configuration", e);
                createDefaultConfig();
            }
        } else {
            LOGGER.info("Configuration file not found, creating default");
            createDefaultConfig();
        }
    }

    private void parseBrightnessStops(String stopsStr) {
        brightnessStops.clear();
        String[] stops = stopsStr.split(",");

        for (String stop : stops) {
            try {
                double value = Double.parseDouble(stop.trim());
                brightnessStops.add(value);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid brightness value: {}", stop);
            }
        }

        // If no valid stops were parsed, use defaults
        if (brightnessStops.isEmpty()) {
            LOGGER.warn("No valid brightness stops found, using defaults");
            parseBrightnessStops(DEFAULT_BRIGHTNESS_STOPS);
        }
    }

    private void createDefaultConfig() {
        parseBrightnessStops(DEFAULT_BRIGHTNESS_STOPS);
        properties.setProperty(BRIGHTNESS_STOPS_KEY, DEFAULT_BRIGHTNESS_STOPS);
        properties.setProperty(LAST_INDEX_KEY, "1");
        saveConfig();
    }

    public void saveConfig() {
        try {
            // Ensure config directory exists
            Files.createDirectories(configPath.getParent());

            try (OutputStream output = Files.newOutputStream(configPath)) {
                properties.store(output, "Customizable Brightness Configuration\n" +
                        "brightness_stops: Comma-separated list of brightness multipliers (1.0 = 100%)\n" +
                        "last_brightness_index: Index of the last used brightness level");
                LOGGER.info("Saved configuration to {}", configPath);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration", e);
        }
    }

    public void saveLastBrightnessIndex(int index) {
        this.lastBrightnessIndex = index;
        properties.setProperty(LAST_INDEX_KEY, String.valueOf(index));
        saveConfig();
    }

    public List<Double> getBrightnessStops() {
        return new ArrayList<>(brightnessStops);
    }

    public int getLastBrightnessIndex() {
        return lastBrightnessIndex;
    }

    public double getBrightnessAtIndex(int index) {
        if (index >= 0 && index < brightnessStops.size()) {
            return brightnessStops.get(index);
        }
        return 1.0; // Default to 100%
    }

    public int getBrightnessStopsCount() {
        return brightnessStops.size();
    }

    public void updateBrightnessStops(List<Double> newStops) {
        this.brightnessStops = new ArrayList<>(newStops);

        // Update properties
        String stopsStr = brightnessStops.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
        properties.setProperty(BRIGHTNESS_STOPS_KEY, stopsStr);

        // Ensure current index is still valid
        if (lastBrightnessIndex >= brightnessStops.size()) {
            lastBrightnessIndex = Math.max(0, brightnessStops.size() - 1);
            properties.setProperty(LAST_INDEX_KEY, String.valueOf(lastBrightnessIndex));
        }

        saveConfig();
        LOGGER.info("Updated brightness stops to: {}", stopsStr);
    }
}
