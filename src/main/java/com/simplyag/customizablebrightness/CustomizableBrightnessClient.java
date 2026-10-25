package com.simplyag.customizablebrightness;

//? if >=26.1 {
/*import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class CustomizableBrightnessClient implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("customizable-brightness");
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("customizable-brightness", "brightness"));
    private static final String KEY_CYCLE_BRIGHTNESS = "key.customizable-brightness.cycle";

    private KeyMapping cycleBrightnessKey;
    // The key hint on screen in this world, if any.
    private TutorialToast hintToast;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Customizable Brightness mod");

        BrightnessController.init(new BrightnessConfig());

        // Looked up by name at runtime: 26.3 replaced GLFW with SDL and renumbered every key code, so a
        // compile-time constant (GLFW_KEY_B / InputConstants.KEY_B) would bind the wrong key on other 26.x versions.
        int keyB = InputConstants.getKey("key.keyboard.b").getValue();
        cycleBrightnessKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(KEY_CYCLE_BRIGHTNESS, keyB, KEY_CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        resolveInWorldCalls();

        LOGGER.info("Customizable Brightness mod initialized successfully");
    }

    // The TutorialToast constructor as a method reference.
    private interface NewTutorialToast {
        TutorialToast create(Font font, TutorialToast.Icons icon, Component title, Component description, boolean progressable);
    }

    // Links now what otherwise first runs in a world (chat hint, B feedback): if a Minecraft version removed one,
    // the game fails at startup with Fabric's error naming this mod instead of crashing mid-game.
    private static void resolveInWorldCalls() {
        Object[] calls = {
                (Function<String, Component>) Component::literal,
                (BiFunction<String, Object[], Component>) Component::translatable,
                (Function<KeyMapping, Component>) KeyMapping::getTranslatedKeyMessage,
                (BiConsumer<LocalPlayer, Component>) LocalPlayer::sendOverlayMessage,
                (NewTutorialToast) TutorialToast::new,
                (Consumer<TutorialToast>) TutorialToast::hide,
                // 26.2 moved the toast manager from Minecraft to Gui.
                /^? if >=26.2 {^//^(Function<net.minecraft.client.gui.Gui, ToastManager>) net.minecraft.client.gui.Gui::toastManager^//^?} else {^/(Function<Minecraft, ToastManager>) Minecraft::getToastManager/^?}^/,
                (BiConsumer<ToastManager, Toast>) ToastManager::addToast,
                Minecraft.getInstance().font,
        };
    }

    private void onEndTick(Minecraft client) {
        if (client.options == null) {
            return;
        }

        if (!BrightnessController.isReady()) {
            // options.txt is loaded by now. The gamma it held is ignored: the mod config is the source of truth.
            BrightnessController.markReady();
            LOGGER.info("Restored brightness: {}%", Math.round(BrightnessController.get() * 100));
        }

        if (client.player == null) {
            // Left the world (or not in one yet): a hint the player never acted on shows again on the next join.
            hideKeyHint();
            return;
        }

        if (hintToast == null && !BrightnessController.getConfig().isHintDismissed()) {
            showKeyHint(client);
        }

        while (cycleBrightnessKey.consumeClick()) {
            cycleBrightness(client);
        }
    }

    private void cycleBrightness(Minecraft client) {
        double brightness = BrightnessController.cycle();
        long percentage = Math.round(brightness * 100);
        client.player.sendOverlayMessage(Component.literal("\u00a7eBrightness: \u00a7f" + percentage + "%"));
        LOGGER.info("Brightness changed to: {}%", percentage);

        if (!BrightnessController.getConfig().isHintDismissed()) {
            BrightnessController.getConfig().markHintDismissed();
            hideKeyHint();
            LOGGER.info("Key hint dismissed");
        }
    }

    private void showKeyHint(Minecraft client) {
        // Vanilla's tutorial toast (the "move with W A S D" box) stays up until hide() is called.
        hintToast = new TutorialToast(client.font, TutorialToast.Icons.MOVEMENT_KEYS,
                Component.translatable("customizable-brightness.hint.title", cycleBrightnessKey.getTranslatedKeyMessage()),
                Component.translatable("customizable-brightness.hint.description"), false);
        /^? if >=26.2 {^//^client.gui.toastManager()^//^?} else {^/client.getToastManager()/^?}^/.addToast(hintToast);
        LOGGER.info("Showed the key hint toast");
    }

    private void hideKeyHint() {
        if (hintToast != null) {
            hintToast.hide();
            hintToast = null;
        }
    }

    public static double getCurrentGamma() {
        return BrightnessController.get();
    }

    public static BrightnessConfig getConfig() {
        return BrightnessController.getConfig();
    }
}
*/
//?} else {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
//? if >=1.21.4
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.toast.TutorialToast;
//? if >=1.16.5 {
import net.minecraft.client.option.KeyBinding;
//?} else {
/*import net.minecraft.client.options.KeyBinding;*/
//?}
//? if >=1.19 {
import net.minecraft.text.Text;
//?} else {
/*import net.minecraft.text.LiteralText;*/
//?}
//? if >=1.21.9
import net.minecraft.util.Identifier;
// log4j-api ships with every supported version, so the logger needs no version branches.
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class CustomizableBrightnessClient implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("customizable-brightness");
    //? if >=1.21.9 {
    private static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(Identifier.of("customizable-brightness", "brightness"));
    //?} else {
    /*private static final String KEY_CATEGORY = "category.customizable-brightness";*/
    //?}
    private static final String KEY_CYCLE_BRIGHTNESS = "key.customizable-brightness.cycle";

    private KeyBinding cycleBrightnessKey;
    // The key hint on screen in this world, if any.
    private TutorialToast hintToast;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Customizable Brightness mod");

        BrightnessController.init(new BrightnessConfig());

        cycleBrightnessKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(KEY_CYCLE_BRIGHTNESS, GLFW.GLFW_KEY_B, KEY_CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        resolveInWorldCalls();

        LOGGER.info("Customizable Brightness mod initialized successfully");
    }

    // ClientPlayerEntity.sendMessage(text, actionBar) as an unbound method reference.
    private interface SendToPlayer {
        void send(ClientPlayerEntity player, /*? if >=1.19 {*/Text/*?} else {*//*LiteralText*//*?}*/ message, boolean actionBar);
    }

    // The TutorialToast constructor as a method reference.
    private interface NewTutorialToast {
        TutorialToast create(/*? if >=1.21.4 {*/TextRenderer textRenderer, /*?}*/TutorialToast.Type type, /*? if >=1.19 {*/Text/*?} else {*//*LiteralText*//*?}*/ title, /*? if >=1.19 {*/Text/*?} else {*//*LiteralText*//*?}*/ description, boolean hasProgressBar);
    }

    // Links now what otherwise first runs in a world (chat hint, B feedback): if a Minecraft version removed one,
    // the game fails at startup with Fabric's error naming this mod instead of crashing mid-game.
    private static void resolveInWorldCalls() {
        Object[] calls = {
                (BiFunction<String, Object[], String>) I18n::translate,
                (Function<KeyBinding, String>) KeyBinding::/*? if >=1.16 {*/getBoundKeyTranslationKey/*?} else {*//*getName*//*?}*/,
                (Function<String, /*? if >=1.19 {*/Text/*?} else {*//*LiteralText*//*?}*/>) /*? if >=1.19 {*/Text::literal/*?} else {*//*LiteralText::new*//*?}*/,
                (SendToPlayer) ClientPlayerEntity::/*? if >=1.16 {*/sendMessage/*?} else {*//*addChatMessage*//*?}*/,
                (NewTutorialToast) TutorialToast::new,
                (Consumer<TutorialToast>) TutorialToast::hide,
                (Function<MinecraftClient, ToastManager>) MinecraftClient::getToastManager,
                (BiConsumer<ToastManager, Toast>) ToastManager::add,
                /*? if >=1.21.4 {*/MinecraftClient.getInstance().textRenderer,/*?}*/
        };
    }

    private void onEndTick(MinecraftClient client) {
        if (client.options == null) {
            return;
        }

        if (!BrightnessController.isReady()) {
            // options.txt is loaded by now. The gamma it held is ignored: the mod config is the source of truth.
            //? if <1.19 {
            /*client.options.gamma = BrightnessController.get();*/
            //?}
            BrightnessController.markReady();
            LOGGER.info("Restored brightness: {}%", Math.round(BrightnessController.get() * 100));
        }
        //? if <1.19 {
        /*else if (client.options.gamma != BrightnessController.get()) {
            // These versions keep gamma in a plain field that the Brightness slider writes directly.
            BrightnessController.onVanillaChange(client.options.gamma);
        }*/
        //?}

        if (client.player == null) {
            // Left the world (or not in one yet): a hint the player never acted on shows again on the next join.
            hideKeyHint();
            return;
        }

        if (hintToast == null && !BrightnessController.getConfig().isHintDismissed()) {
            showKeyHint(client);
        }

        while (cycleBrightnessKey.wasPressed()) {
            cycleBrightness(client);
        }
    }

    private void cycleBrightness(MinecraftClient client) {
        double brightness = BrightnessController.cycle();
        //? if <1.19 {
        /*client.options.gamma = brightness;*/
        //?}
        long percentage = Math.round(brightness * 100);
        sendToPlayer(client, "\u00a7eBrightness: \u00a7f" + percentage + "%", true);
        LOGGER.info("Brightness changed to: {}%", percentage);

        if (!BrightnessController.getConfig().isHintDismissed()) {
            BrightnessController.getConfig().markHintDismissed();
            hideKeyHint();
            LOGGER.info("Key hint dismissed");
        }
    }

    private void showKeyHint(MinecraftClient client) {
        // Inline conditions keep both spellings of the same method on one line, so 1.14-1.16.1 compile to
        // identical classes (line numbers included) and can share one jar.
        String key = I18n.translate(cycleBrightnessKey./*? if >=1.16 {*/getBoundKeyTranslationKey/*?} else {*//*getName*//*?}*/());
        // Vanilla's tutorial toast (the "move with W A S D" box) stays up until hide() is called.
        hintToast = new TutorialToast(/*? if >=1.21.4 {*/client.textRenderer, /*?}*/TutorialToast.Type.MOVEMENT_KEYS,
                text(I18n.translate("customizable-brightness.hint.title", key)), text(I18n.translate("customizable-brightness.hint.description")), false);
        client.getToastManager().add(hintToast);
        LOGGER.info("Showed the key hint toast");
    }

    private void hideKeyHint() {
        if (hintToast != null) {
            hintToast.hide();
            hintToast = null;
        }
    }

    private static void sendToPlayer(MinecraftClient client, String message, boolean actionBar) {
        client.player./*? if >=1.16 {*/sendMessage/*?} else {*//*addChatMessage*//*?}*/(text(message), actionBar);
    }

    private static /*? if >=1.19 {*/Text/*?} else {*//*LiteralText*//*?}*/ text(String text) {
        return /*? if >=1.19 {*/Text.literal(text)/*?} else {*//*new LiteralText(text)*//*?}*/;
    }

    public static double getCurrentGamma() {
        return BrightnessController.get();
    }

    public static BrightnessConfig getConfig() {
        return BrightnessController.getConfig();
    }
}
//?}
