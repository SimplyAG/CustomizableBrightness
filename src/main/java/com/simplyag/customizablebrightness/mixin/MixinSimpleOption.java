package com.simplyag.customizablebrightness.mixin;

import com.simplyag.customizablebrightness.CustomizableBrightnessClient;
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

    /**
     * Mixin to return the custom gamma value instead of the vanilla one
     */
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void getModValue(CallbackInfoReturnable<T> info) {
        if (isGammaOption()) {
            info.setReturnValue((T)(Double)CustomizableBrightnessClient.getCurrentGamma());
        }
    }

    /**
     * Mixin to set the custom gamma value instead of the vanilla one
     */
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
