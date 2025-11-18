package com.simplyag.customizablebrightness;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Customizable Brightness Config"));

        // Get current brightness stops from config
        BrightnessConfig config = CustomizableBrightnessClient.getConfig();
        List<Double> brightnessStops = new ArrayList<>(config.getBrightnessStops());

        builder.setSavingRunnable(() -> {
            // Save the modified brightness stops back to config
            config.updateBrightnessStops(brightnessStops);
        });

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Create a subcategory for brightness stops
        SubCategoryBuilder brightnessCategory = entryBuilder.startSubCategory(Text.literal("Brightness Stops"));
        brightnessCategory.setTooltip(Text.literal("Configure the brightness levels you can cycle through (as percentages)"));

        // Add editable fields for each brightness stop (converted to percentages for user input)
        for (int i = 0; i < brightnessStops.size(); i++) {
            final int index = i;
            double multiplier = brightnessStops.get(index);
            int percentage = (int)(multiplier * 100);

            String label = String.format("Stop %d", index + 1);

            brightnessCategory.add(entryBuilder.startIntField(Text.literal(label), percentage)
                    .setDefaultValue(percentage)
                    .setTooltip(Text.literal("Brightness level in percentage (0% = dark, 100% = normal, 1000% = very bright)"))
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
        brightnessCategory.add(entryBuilder.startTextDescription(
                Text.literal("§7Add new stops by editing the config file§r\n" +
                        "§7at config/customizable_brightness.properties§r\n\n" +
                        "§eFormat:§r brightness_stops=0.0,1.0,2.0,5.0,10.0\n" +
                        "§7(Values are stored as multipliers: 1.0 = 100%)§r\n\n" +
                        "§aCurrent Stops:§r " + formatStopsList(brightnessStops)))
                .build());

        general.addEntry(brightnessCategory.build());

        // Add info section
        general.addEntry(entryBuilder.startTextDescription(
                Text.literal("§6How to use:§r\n" +
                        "• Press §eB§r (or your configured key) to cycle brightness\n" +
                        "• All values are displayed as percentages\n" +
                        "• 0% = complete darkness, 100% = normal, 1000% = max brightness\n" +
                        "• Edit brightness_stops in config file to add/remove levels\n\n" +
                        "§6Tips:§r\n" +
                        "• Use 0% for complete darkness\n" +
                        "• Use 1000%+ for extreme brightness in caves\n" +
                        "• Keep stops sorted for best experience\n\n" +
                        "§eClick 'Done' to save your changes!§r"))
                .build());

        return builder.build();
    }

    private static String formatStopsList(List<Double> stops) {
        return stops.stream()
                .map(v -> String.format("%.0f%%", v * 100))
                .collect(Collectors.joining(", "));
    }
}
