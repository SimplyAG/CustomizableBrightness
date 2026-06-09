package com.simplyag.customizablebrightness.mixin;

//? if >=26.1 {
/*import com.simplyag.customizablebrightness.CustomizableBrightnessClient;
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

    
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void getModValue(CallbackInfoReturnable<T> info) {
        if (isGammaOption()) {
            info.setReturnValue((T)(Double)CustomizableBrightnessClient.getCurrentGamma());
        }
    }

    
    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    public void setModValue(T value, CallbackInfo info) {
        if (isGammaOption()) {
            CustomizableBrightnessClient.setCurrentGamma((Double) value);
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
import com.simplyag.customizablebrightness.CustomizableBrightnessClient;
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

    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    public void getModValue(CallbackInfoReturnable<Double> info) {
        if (isGammaOption()) {
            info.setReturnValue(CustomizableBrightnessClient.getCurrentGamma());
        }
    }

    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    public void setModValue(T value, CallbackInfo info) {
        if (isGammaOption()) {
            CustomizableBrightnessClient.setCurrentGamma((Double) value);
            info.cancel();
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
//?} else if >=1.16.5 {
/*
import com.simplyag.customizablebrightness.CustomizableBrightnessClient;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class MixinSimpleOption {
    @Shadow
    public double gamma;

    @Inject(method = "write", at = @At("HEAD"))
    private void onWrite(CallbackInfo ci) {
        gamma = CustomizableBrightnessClient.getCurrentGamma();
    }
}
*/
//?} else {
/*
import com.simplyag.customizablebrightness.CustomizableBrightnessClient;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class MixinSimpleOption {
    @Shadow
    public double gamma;

    @Inject(method = "write", at = @At("HEAD"))
    private void onWrite(CallbackInfo ci) {
        gamma = CustomizableBrightnessClient.getCurrentGamma();
    }
}
*/
//?}
//?}
