package com.simplyag.customizablebrightness;

/**
 * Owns the brightness in use. The mod config is the single source of truth: vanilla's gamma option only
 * mirrors this value. Whatever vanilla reads back from options.txt while the game starts is ignored, so a
 * stale options.txt can never override the brightness the player last chose (with B or with the slider).
 *
 * <p>Plain Java on purpose (no Minecraft types), so it compiles identically for every target.
 */
public final class BrightnessController {
    private static final double EPSILON = 1.0E-6;

    private static BrightnessConfig config;
    private static double brightness = 1.0;
    private static int index = 1;
    private static boolean ready = false;

    private BrightnessController() {
    }

    static void init(BrightnessConfig loadedConfig) {
        config = loadedConfig;
        index = loadedConfig.getLastBrightnessIndex();
        brightness = loadedConfig.getCurrentBrightness();
    }

    /**
     * Called on the first client tick, after vanilla has finished loading options.txt. From then on, a vanilla
     * change to the gamma option is the player moving the Brightness slider.
     */
    static void markReady() {
        ready = true;
    }

    static boolean isReady() {
        return ready;
    }

    public static double get() {
        return brightness;
    }

    static BrightnessConfig getConfig() {
        return config;
    }

    /** B key: step to the next configured stop and persist it. */
    static double cycle() {
        int count = config.getBrightnessStopsCount();
        int next;
        if (index >= 0 && index < count && Math.abs(config.getBrightnessAtIndex(index) - brightness) < EPSILON) {
            next = (index + 1) % count;
        } else {
            // The slider moved us off the stops: continue from the nearest stop above the current value.
            next = 0;
            double best = Double.MAX_VALUE;
            for (int i = 0; i < count; i++) {
                double stop = config.getBrightnessAtIndex(i);
                if (stop > brightness + EPSILON && stop < best) {
                    best = stop;
                    next = i;
                }
            }
        }
        index = next;
        brightness = config.getBrightnessAtIndex(next);
        config.saveBrightness(index, brightness);
        return brightness;
    }

    /** Vanilla set the gamma option (the Brightness slider). Ignored while options.txt is being loaded. */
    public static void onVanillaChange(double value) {
        if (!ready || config == null || Math.abs(value - brightness) < EPSILON) {
            return;
        }
        brightness = value;
        for (int i = 0; i < config.getBrightnessStopsCount(); i++) {
            if (Math.abs(config.getBrightnessAtIndex(i) - value) < EPSILON) {
                index = i;
                break;
            }
        }
        config.saveBrightness(index, brightness);
    }
}
