package com.simplyag.customizablebrightness.smoke;

// Every Minecraft call the smoke harness makes, per game era. SmokeClient itself is version independent.

//? if >=26.1 {
/*import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.MinecraftServer;
//? if <26.3 {
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
//?}

import java.lang.reflect.Field;

final class Mc {
    static final int KEY_B = InputConstants.KEY_B;
    static final int KEY_F1 = InputConstants.KEY_F1;
    static final int KEY_F2 = InputConstants.KEY_F2;

    private Mc() {
    }

    private static Minecraft mc(Object client) {
        return (Minecraft) client;
    }

    static Screen screen(Object client) {
        //? if >=26.2 {
        return mc(client).gui.screen();
        //?} else {
        /^return mc(client).screen;^/
        //?}
    }

    static void setScreen(Object client, Object screen) {
        //? if >=26.2 {
        mc(client).gui.setScreen((Screen) screen);
        //?} else {
        /^mc(client).setScreen((Screen) screen);^/
        //?}
    }

    static void pressKey(Object client, int key) {
        long window = mc(client).getWindow().handle();
        //? if >=26.3 {
        // 26.3 reads input through SDL; this is the method its key events are dispatched to.
        mc(client).keyboardHandler.keyPress(window, InputConstants.PRESS, new KeyEvent(key, 0, 0));
        mc(client).keyboardHandler.keyPress(window, InputConstants.RELEASE, new KeyEvent(key, 0, 0));
        //?} else {
        /^// Invoke the GLFW key callback the game registered: the same path a physical key press takes.
        GLFWKeyCallback callback = GLFW.glfwSetKeyCallback(window, null);
        try {
            callback.invoke(window, key, 0, GLFW.GLFW_PRESS, 0);
            callback.invoke(window, key, 0, GLFW.GLFW_RELEASE, 0);
        } finally {
            GLFW.glfwSetKeyCallback(window, callback);
        }^/
        //?}
    }

    static Object toastManager(Object client) {
        //? if >=26.2 {
        return mc(client).gui.toastManager();
        //?} else {
        /^return mc(client).getToastManager();^/
        //?}
    }

    static boolean isTutorialToast(Object toast) {
        return toast instanceof net.minecraft.client.gui.components.toasts.TutorialToast;
    }

    static boolean isTitleScreen(Object client) {
        return screen(client) instanceof TitleScreen;
    }

    static boolean isOnboardingScreen(Object client) {
        return screen(client) instanceof AccessibilityOnboardingScreen;
    }

    static void showTitleScreen(Object client) {
        setScreen(client, new TitleScreen());
    }

    static boolean currentScreenIsPause(Object client) {
        return screen(client) instanceof PauseScreen;
    }

    static String currentScreenName(Object client) {
        Screen screen = screen(client);
        return screen == null ? "none" : screen.getClass().getName();
    }

    static void createWorld(Object client, String name) {
        CreateWorldScreen.openFresh(mc(client), () -> setScreen(client, null));
    }

    // Presses "Create New World" once the creation screen is up. Returns true when pressed.
    static boolean pressCreateButton(Object client) {
        Screen screen = screen(client);
        if (!(screen instanceof CreateWorldScreen)) return false;
        Button button = findButton(screen, I18n.get("selectWorld.create"));
        if (button == null) return false;
        button.onPress(new KeyEvent(InputConstants.KEY_RETURN, 0, 0));
        return true;
    }

    private static Button findButton(GuiEventListener element, String text) {
        if (element instanceof Button && ((Button) element).getMessage().getString().equals(text)) return (Button) element;
        if (element instanceof ContainerEventHandler) {
            for (GuiEventListener child : ((ContainerEventHandler) element).children()) {
                Button found = findButton(child, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    static void openWorld(Object client, String folder) {
        mc(client).createWorldOpenFlows().openWorld(folder, () -> setScreen(client, null));
    }

    static void dismissBlockingScreens(Object client) {
    }

    static boolean inWorld(Object client) {
        return mc(client).player != null && mc(client).level != null;
    }

    static void command(Object client, String command) {
        MinecraftServer server = mc(client).getSingleplayerServer();
        server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command));
    }

    // Opened from the pause menu as a player would: the screen reads its parent while it is constructed.
    static void openVideoSettings(Object client) {
        setScreen(client, new PauseScreen(true));
        setScreen(client, new VideoSettingsScreen(screen(client), mc(client), mc(client).options));
    }

    // What the Video Settings Brightness slider calls when it moves.
    static void setVanillaGamma(Object client, double value) {
        mc(client).options.gamma().set(value);
    }

    static Double getVanillaGamma(Object client) {
        return mc(client).options.gamma().get();
    }

    // 26.x builds the lightmap on the GPU; there is no CPU copy to read.
    static int[] sampleLightmap(Object client) {
        return null;
    }

    static String[] keyCategory(Object client, String keyName) {
        for (KeyMapping mapping : keyMappings(mc(client).options)) {
            if (mapping.getName().equals(keyName)) {
                KeyMapping.Category category = mapping.getCategory();
                return new String[]{"key.category." + category.id().getNamespace() + "." + category.id().getPath(),
                        category.label().getString()};
            }
        }
        return null;
    }

    private static KeyMapping[] keyMappings(Object options) {
        KeyMapping[] best = new KeyMapping[0];
        for (Field field : options.getClass().getDeclaredFields()) {
            if (field.getType() == KeyMapping[].class) {
                try {
                    field.setAccessible(true);
                    KeyMapping[] value = (KeyMapping[]) field.get(options);
                    if (value != null && value.length > best.length) best = value;
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        return best;
    }

    static void stop(Object client) {
        mc(client).stop();
    }
}
*/
//?} else {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
//? if >=1.19.4
import net.minecraft.client.gui.screen.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.server.MinecraftServer;
//? if >=1.21.9
import net.minecraft.client.input.KeyInput;
//? if >=1.16.5 {
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.option.KeyBinding;
//?} else {
/*import net.minecraft.client.gui.screen.VideoOptionsScreen;
import net.minecraft.client.options.KeyBinding;*/
//?}
//? if >=1.16.5 <1.19
/*import net.minecraft.client.option.Option;*/
//? if <1.16.5
/*import net.minecraft.client.options.Option;*/
//? if <1.21.2
/*import net.minecraft.client.texture.NativeImage;*/
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.io.File;
import java.lang.reflect.Field;

final class Mc {
    static final int KEY_B = GLFW.GLFW_KEY_B;
    static final int KEY_F1 = GLFW.GLFW_KEY_F1;
    static final int KEY_F2 = GLFW.GLFW_KEY_F2;

    private Mc() {
    }

    private static MinecraftClient mc(Object client) {
        return (MinecraftClient) client;
    }

    static Screen screen(Object client) {
        return mc(client).currentScreen;
    }

    static void setScreen(Object client, Object screen) {
        //? if >=1.17.1 {
        mc(client).setScreen((Screen) screen);
        //?} else {
        /*mc(client).openScreen((Screen) screen);*/
        //?}
    }

    private static long window(Object client) {
        //? if >=1.15 {
        return mc(client).getWindow().getHandle();
        //?} else {
        /*return mc(client).window.getHandle();*/
        //?}
    }

    // Invoke the GLFW key callback the game registered: the same path a physical key press takes.
    static void pressKey(Object client, int key) {
        long window = window(client);
        GLFWKeyCallback callback = GLFW.glfwSetKeyCallback(window, null);
        try {
            callback.invoke(window, key, 0, GLFW.GLFW_PRESS, 0);
            callback.invoke(window, key, 0, GLFW.GLFW_RELEASE, 0);
        } finally {
            GLFW.glfwSetKeyCallback(window, callback);
        }
    }

    static Object toastManager(Object client) {
        return mc(client).getToastManager();
    }

    static boolean isTutorialToast(Object toast) {
        return toast instanceof net.minecraft.client.toast.TutorialToast;
    }

    static boolean isTitleScreen(Object client) {
        return screen(client) instanceof TitleScreen;
    }

    static boolean isOnboardingScreen(Object client) {
        //? if >=1.19.4 {
        return screen(client) instanceof AccessibilityOnboardingScreen;
        //?} else {
        /*return false;*/
        //?}
    }

    static void showTitleScreen(Object client) {
        setScreen(client, new TitleScreen());
    }

    static boolean currentScreenIsPause(Object client) {
        return screen(client) instanceof GameMenuScreen;
    }

    static String currentScreenName(Object client) {
        Screen screen = screen(client);
        return screen == null ? "none" : screen.getClass().getName();
    }

    static void createWorld(Object client, String name) {
        //? if >=1.21.9 {
        CreateWorldScreen.show(mc(client), () -> setScreen(client, null));
        //?} else if >=1.21.2 {
        /*CreateWorldScreen.show(mc(client), screen(client));*/
        //?} else if >=1.19 {
        /*CreateWorldScreen.create(mc(client), screen(client));*/
        //?} else if >=1.16.5 {
        /*setScreen(client, CreateWorldScreen.create(screen(client)));*/
        //?} else if >=1.16.2 {
        /*setScreen(client, CreateWorldScreen.method_31130(screen(client)));*/
        //?} else {
        /*setScreen(client, new CreateWorldScreen(screen(client)));*/
        //?}
    }

    // Presses "Create New World" once the creation screen is up. Returns true when pressed.
    static boolean pressCreateButton(Object client) {
        Screen screen = screen(client);
        if (!(screen instanceof CreateWorldScreen)) return false;
        ButtonWidget button = findButton(screen, I18n.translate("selectWorld.create"));
        if (button == null) return false;
        //? if >=1.21.9 {
        button.onPress(new KeyInput(GLFW.GLFW_KEY_ENTER, 0, 0));
        //?} else {
        /*button.onPress();*/
        //?}
        return true;
    }

    private static ButtonWidget findButton(Element element, String text) {
        if (element instanceof ButtonWidget) {
            //? if >=1.16 {
            String message = ((ButtonWidget) element).getMessage().getString();
            //?} else {
            /*String message = ((ButtonWidget) element).getMessage();*/
            //?}
            if (message.equals(text)) return (ButtonWidget) element;
        }
        if (element instanceof ParentElement) {
            for (Element child : ((ParentElement) element).children()) {
                ButtonWidget found = findButton(child, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    static void openWorld(Object client, String folder) {
        //? if >=1.20.3 {
        mc(client).createIntegratedServerLoader().start(folder, () -> setScreen(client, null));
        //?} else if >=1.19 {
        /*mc(client).createIntegratedServerLoader().start(screen(client), folder);*/
        //?} else if >=1.16 {
        /*mc(client).startIntegratedServer(folder);*/
        //?} else {
        /*mc(client).startIntegratedServer(folder, folder, null);*/
        //?}
    }

    static void dismissBlockingScreens(Object client) {
    }

    static boolean inWorld(Object client) {
        return mc(client).player != null && mc(client).world != null;
    }

    static void command(Object client, String command) {
        MinecraftServer server = mc(client).getServer();
        server.execute(() -> {
            //? if >=1.21.10 {
            server.getCommandManager().parseAndExecute(server.getCommandSource(), command);
            //?} else if >=1.19 {
            /*server.getCommandManager().executeWithPrefix(server.getCommandSource(), command);*/
            //?} else {
            /*server.getCommandManager().execute(server.getCommandSource(), command);*/
            //?}
        });
    }

    // Opened from the pause menu as a player would: the screen reads its parent while it is constructed.
    static void openVideoSettings(Object client) {
        setScreen(client, new GameMenuScreen(true));
        //? if >=1.21 {
        setScreen(client, new VideoOptionsScreen(screen(client), mc(client), mc(client).options));
        //?} else {
        /*setScreen(client, new VideoOptionsScreen(screen(client), mc(client).options));*/
        //?}
    }

    // What the Video Settings Brightness slider calls when it moves.
    static void setVanillaGamma(Object client, double value) {
        //? if >=1.19 {
        mc(client).options.getGamma().setValue(value);
        //?} else {
        /*Option.GAMMA.set(mc(client).options, value);*/
        //?}
    }

    static Double getVanillaGamma(Object client) {
        //? if >=1.19 {
        return mc(client).options.getGamma().getValue();
        //?} else {
        /*return mc(client).options.gamma;*/
        //?}
    }

    // The 16x16 lightmap texture where the game still builds it on the CPU (up to 1.21.1). Luminance of the
    // cells (block 0, sky 0), (block 7, sky 0), (block 0, sky 15), (block 15, sky 15). Null on GPU-only versions.
    static int[] sampleLightmap(Object client) {
        //? if >=1.21.2 {
        return null;
        //?} else {
        /*try {
            LightmapTextureManager lightmap = (LightmapTextureManager) fieldOfType(mc(client).gameRenderer, GameRenderer.class, LightmapTextureManager.class);
            NativeImage image = (NativeImage) fieldOfType(lightmap, LightmapTextureManager.class, NativeImage.class);
            return new int[]{lum(image, 0, 0), lum(image, 7, 0), lum(image, 0, 15), lum(image, 15, 15)};
        } catch (Exception e) {
            SmokeClient.LOG.warn("lightmap read failed", e);
            return null;
        }*/
        //?}
    }

    //? if <1.21.2 {
    /*private static int lum(NativeImage image, int x, int y) {
        //? if >=1.17.1 {
        int abgr = image.getColor(x, y);
        //?} else if >=1.16 {
        /^int abgr = image.getPixelColor(x, y);^/
        //?} else if >=1.14.4 {
        /^int abgr = image.getPixelRgba(x, y);^/
        //?} else {
        /^int abgr = image.getPixelRGBA(x, y);^/
        //?}
        int r = abgr & 0xFF, g = (abgr >> 8) & 0xFF, b = (abgr >> 16) & 0xFF;
        return (int) Math.round(0.2126 * r + 0.7152 * g + 0.0722 * b);
    }*/
    //?}

    // Found by type, so the same code works whatever the field is called in a given version.
    private static Object fieldOfType(Object owner, Class<?> ownerClass, Class<?> type) throws IllegalAccessException {
        for (Field field : ownerClass.getDeclaredFields()) {
            if (field.getType() == type) {
                field.setAccessible(true);
                return field.get(owner);
            }
        }
        throw new IllegalStateException("no " + type.getSimpleName() + " field in " + ownerClass.getName());
    }

    static String[] keyCategory(Object client, String keyName) {
        for (KeyBinding binding : keyBindings(mc(client).options)) {
            //? if >=1.21.9 {
            String name = binding.getId();
            //?} else if >=1.16 {
            /*String name = binding.getTranslationKey();*/
            //?} else {
            /*String name = binding.getId();*/
            //?}
            if (name.equals(keyName)) {
                //? if >=1.21.9 {
                KeyBinding.Category category = binding.getCategory();
                return new String[]{"key.category." + category.id().getNamespace() + "." + category.id().getPath(),
                        category.getLabel().getString()};
                //?} else {
                /*String category = binding.getCategory();
                return new String[]{category, I18n.translate(category)};*/
                //?}
            }
        }
        return null;
    }

    private static KeyBinding[] keyBindings(Object options) {
        KeyBinding[] best = new KeyBinding[0];
        for (Field field : options.getClass().getDeclaredFields()) {
            if (field.getType() == KeyBinding[].class) {
                try {
                    field.setAccessible(true);
                    KeyBinding[] value = (KeyBinding[]) field.get(options);
                    if (value != null && value.length > best.length) best = value;
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        return best;
    }

    static void stop(Object client) {
        mc(client).scheduleStop();
    }
}
//?}
