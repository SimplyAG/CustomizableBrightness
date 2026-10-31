package com.simplyag.customizablebrightness.mixin;

//? if >=26.1 {
/*import com.simplyag.customizablebrightness.BrightnessController;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionInstance.class)
public class MixinSimpleOption<T> {
    @Shadow
    @Final
    private Component caption;

    // The option stays within the 0..1 its readers expect: options.txt, Sodium (which resets an out-of-range value to
    // its default at startup) and shader packs. The lightmap takes the real brightness, see MixinLightmap.
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void getModValue(CallbackInfoReturnable<T> info) {
        if (isGammaOption()) {
            info.setReturnValue((T)(Double)Math.min(BrightnessController.get(), 1.0));
        }
    }

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    public void setModValue(T value, CallbackInfo info) {
        if (isGammaOption()) {
            BrightnessController.onVanillaChange((Double) value);
            info.cancel();
        }
    }

    @Unique
    private boolean isGammaOption() {
        if (caption.getContents() instanceof TranslatableContents translatableContents) {
            return translatableContents.getKey().equals("options.gamma");
        }

        return false;
    }
}
*/
//?} else {
//? if >=1.19 {
import com.mojang.serialization.Codec;
import com.simplyag.customizablebrightness.BrightnessController;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleOption.class)
public class MixinSimpleOption<T> {
    @Shadow
    @Final
    Text text;

    @Shadow
    @Final
    private Codec<T> codec;

    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    public void getModValue(CallbackInfoReturnable<Double> info) {
        if (isGammaOption()) {
            info.setReturnValue(BrightnessController.get());
        }
    }

    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    public void setModValue(T value, CallbackInfo info) {
        if (isGammaOption()) {
            BrightnessController.onVanillaChange((Double) value);
            info.cancel();
        }
    }

    // The gamma codec only accepts 0..1, so every options save above 100% logged "Error saving option" and
    // dropped the line. Clamp what goes into options.txt; the mod config keeps the real value.
    @Inject(method = "getCodec", at = @At("HEAD"), cancellable = true)
    public void clampSavedValue(CallbackInfoReturnable<Codec<T>> info) {
        if (isGammaOption()) {
            Codec<Double> original = (Codec<Double>)(Object)codec;
            info.setReturnValue((Codec<T>)(Object)original.xmap(value -> value, value -> Math.max(0.0, Math.min(1.0, value))));
        }
    }

    @Unique
    private boolean isGammaOption() {
        if (text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            return translatableTextContent.getKey().equals("options.gamma");
        }
        return false;
    }
}
//?} else {
/*import com.simplyag.customizablebrightness.BrightnessController;
//? if >=1.16.5 {
import net.minecraft.client.option.GameOptions;
//?} else {
/^import net.minecraft.client.options.GameOptions;^/
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class MixinSimpleOption {
    @Shadow
    public double gamma;

    // These versions keep gamma in a plain field that the Brightness slider writes directly. The tick handler
    // picks slider changes up, but a change and the save that follows can land in the same tick.
    @Inject(method = "write", at = @At("HEAD"))
    private void onWrite(CallbackInfo ci) {
        BrightnessController.onVanillaChange(gamma);
    }
}
*/
//?}
//?}
