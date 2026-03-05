package com.simplyag.customizablebrightness;

//? if >=1.16.2 {
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screen.Screen;
//? if >=1.19 {
import net.minecraft.text.Text;
//?} else {
/*import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;*/
//?}

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                //? if >=1.19 {
                .setTitle(Text.literal("Customizable Brightness Config"));
                //?} else {
                /*.setTitle(new LiteralText("Customizable Brightness Config"));*/
                //?}

        BrightnessConfig config = CustomizableBrightnessClient.getConfig();
        List<Double> brightnessStops = new ArrayList<>(config.getBrightnessStops());

        builder.setSavingRunnable(() -> {
            config.updateBrightnessStops(brightnessStops);
        });

        //? if >=1.19 {
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        //?} else {
        /*ConfigCategory general = builder.getOrCreateCategory(new LiteralText("General"));*/
        //?}
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        //? if >=1.19 {
        SubCategoryBuilder brightnessCategory = entryBuilder.startSubCategory(Text.literal("Brightness Stops"));
        brightnessCategory.setTooltip(Text.literal("Configure the brightness levels you can cycle through (as percentages)"));
        //?} else {
        /*SubCategoryBuilder brightnessCategory = entryBuilder.startSubCategory(new LiteralText("Brightness Stops"));
        brightnessCategory.setTooltip(new LiteralText("Configure the brightness levels you can cycle through (as percentages)"));*/
        //?}

        for (int i = 0; i < brightnessStops.size(); i++) {
            final int index = i;
            double multiplier = brightnessStops.get(index);
            int percentage = (int)(multiplier * 100);

            String label = String.format("Stop %d", index + 1);

            //? if >=1.19 {
            brightnessCategory.add(entryBuilder.startIntField(Text.literal(label), percentage)
                    .setDefaultValue(percentage)
                    .setTooltip(Text.literal("Brightness level in percentage (0% = dark, 100% = normal, 1000% = very bright)"))
            //?} else {
            /*brightnessCategory.add(entryBuilder.startIntField(new LiteralText(label), percentage)
                    .setDefaultValue(percentage)
                    .setTooltip(new LiteralText("Brightness level in percentage (0% = dark, 100% = normal, 1000% = very bright)"))*/
            //?}
                    .setSaveConsumer(newPercentage -> {
                        if (index < brightnessStops.size()) {
                            brightnessStops.set(index, newPercentage / 100.0);
                        }
                    })
                    .setMin(0)
                    .setMax(10000)
                    .build());
        }

        //? if >=1.19 {
        brightnessCategory.add(entryBuilder.startTextDescription(
                Text.literal("\u00a77Add new stops by editing the config file\u00a7r\n" +
                        "\u00a77at config/customizable_brightness.properties\u00a7r\n\n" +
                        "\u00a7eFormat:\u00a7r brightness_stops=0.0,1.0,2.0,5.0,10.0\n" +
                        "\u00a77(Values are stored as multipliers: 1.0 = 100%)\u00a7r\n\n" +
                        "\u00a7aCurrent Stops:\u00a7r " + formatStopsList(brightnessStops)))
                .build());
        //?} else {
        /*brightnessCategory.add(entryBuilder.startTextDescription(
                new LiteralText("\u00a77Add new stops by editing the config file\u00a7r\n" +
                        "\u00a77at config/customizable_brightness.properties\u00a7r\n\n" +
                        "\u00a7eFormat:\u00a7r brightness_stops=0.0,1.0,2.0,5.0,10.0\n" +
                        "\u00a77(Values are stored as multipliers: 1.0 = 100%)\u00a7r\n\n" +
                        "\u00a7aCurrent Stops:\u00a7r " + formatStopsList(brightnessStops)))
                .build());*/
        //?}

        general.addEntry(brightnessCategory.build());

        //? if >=1.19 {
        general.addEntry(entryBuilder.startTextDescription(
                Text.literal("\u00a76How to use:\u00a7r\n" +
                        "\u2022 Press \u00a7eB\u00a7r (or your configured key) to cycle brightness\n" +
                        "\u2022 All values are displayed as percentages\n" +
                        "\u2022 0% = complete darkness, 100% = normal, 1000% = max brightness\n" +
                        "\u2022 Edit brightness_stops in config file to add/remove levels\n\n" +
                        "\u00a76Tips:\u00a7r\n" +
                        "\u2022 Use 0% for complete darkness\n" +
                        "\u2022 Use 1000%+ for extreme brightness in caves\n" +
                        "\u2022 Keep stops sorted for best experience\n\n" +
                        "\u00a7eClick 'Done' to save your changes!\u00a7r"))
                .build());
        //?} else {
        /*general.addEntry(entryBuilder.startTextDescription(
                new LiteralText("\u00a76How to use:\u00a7r\n" +
                        "\u2022 Press \u00a7eB\u00a7r (or your configured key) to cycle brightness\n" +
                        "\u2022 All values are displayed as percentages\n" +
                        "\u2022 0% = complete darkness, 100% = normal, 1000% = max brightness\n" +
                        "\u2022 Edit brightness_stops in config file to add/remove levels\n\n" +
                        "\u00a76Tips:\u00a7r\n" +
                        "\u2022 Use 0% for complete darkness\n" +
                        "\u2022 Use 1000%+ for extreme brightness in caves\n" +
                        "\u2022 Keep stops sorted for best experience\n\n" +
                        "\u00a7eClick 'Done' to save your changes!\u00a7r"))
                .build());*/
        //?}

        return builder.build();
    }

    private static String formatStopsList(List<Double> stops) {
        return stops.stream()
                .map(v -> String.format("%.0f%%", v * 100))
                .collect(Collectors.joining(", "));
    }
}
//?} else {
/*public class ConfigScreen {}*/
//?}
