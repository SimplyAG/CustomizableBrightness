package com.simplyag.customizablebrightness.mixin;

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

    /**
     * Mixin to return the custom gamma value instead of the vanilla one
     */
    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    public void getModValue(CallbackInfoReturnable<Double> info) {
        if (isGammaOption()) {
            info.setReturnValue(CustomizableBrightnessClient.getCurrentGamma());
        }
    }

    /**
     * Mixin to set the custom gamma value instead of the vanilla one
     */
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
