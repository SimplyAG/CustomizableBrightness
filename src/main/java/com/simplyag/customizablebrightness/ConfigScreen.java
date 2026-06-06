package com.simplyag.customizablebrightness;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigScreen {
    // Default brightness stops as percentages
    private static final List<Integer> DEFAULT_STOPS_PERCENT = List.of(0, 100, 200, 500, 1000);

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Customizable Brightness Config"));

        // Get current brightness stops from config
        BrightnessConfig config = CustomizableBrightnessClient.getConfig();
        List<Double> brightnessStops = new ArrayList<>(config.getBrightnessStops());

        builder.setSavingRunnable(() -> {
            // Save the modified brightness stops back to config
            config.updateBrightnessStops(brightnessStops);
        });

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("Brightness Stops"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Add editable fields for each brightness stop (converted to percentages for user input)
        for (int i = 0; i < brightnessStops.size(); i++) {
            final int index = i;
            double multiplier = brightnessStops.get(index);
            int percentage = (int)(multiplier * 100);

            // Get the default value for this index, or use the current value if beyond defaults
            int defaultPercentage = index < DEFAULT_STOPS_PERCENT.size()
                    ? DEFAULT_STOPS_PERCENT.get(index)
                    : percentage;

            String label = String.format("Stop %d", index + 1);

            general.addEntry(entryBuilder.startIntField(Component.literal(label), percentage)
                    .setDefaultValue(defaultPercentage)
                    .setTooltip(Component.literal("Brightness level in percentage (0% = dark, 100% = normal, 1000% = very bright)"))
                    .setSaveConsumer(newPercentage -> {
                        if (index < brightnessStops.size()) {
                            // Convert percentage back to multiplier (0-10000% → 0.0-100.0)
                            brightnessStops.set(index, newPercentage / 100.0);
                        }
                    })
                    .setMin(0)
                    .setMax(10000)
                    .build());
        }

        // Add info about current stops
        general.addEntry(entryBuilder.startTextDescription(
                Component.literal("§7Add new stops by editing the config file§r\n" +
                        "§7at config/customizable_brightness.properties§r\n\n" +
                        "§eFormat:§r brightness_stops=0.0,1.0,2.0,5.0,10.0\n" +
                        "§7(Values are stored as multipliers: 1.0 = 100%)§r\n\n" +
                        "§aCurrent Stops:§r " + formatStopsList(brightnessStops)))
                .build());

        // Add info section
        general.addEntry(entryBuilder.startTextDescription(
                Component.literal("§6How to use:§r\n" +
                        "• Press §eB§r (or your configured key) to cycle brightness\n" +
                        "• All values are displayed as percentages\n" +
                        "• 0% = complete darkness, 100% = normal, 1000% = max brightness\n" +
                        "• Edit brightness_stops in config file to add/remove levels\n\n" +
                        "§6Tips:§r\n" +
                        "• Use 0% for complete darkness\n" +
                        "• Use 1000%+ for extreme brightness in caves\n" +
                        "• Keep stops sorted for best experience\n\n" +
                        "§eClick 'Save & Quit' to save your changes!§r"))
                .build());

        return builder.build();
    }

    private static String formatStopsList(List<Double> stops) {
        return stops.stream()
                .map(v -> String.format("%.0f%%", v * 100))
                .collect(Collectors.joining(", "));
    }
}
