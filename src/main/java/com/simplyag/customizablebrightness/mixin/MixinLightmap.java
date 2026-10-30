package com.simplyag.customizablebrightness.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simplyag.customizablebrightness.BrightnessController;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

/^*
 * The one place the game itself reads the gamma option. It gets the real brightness; everyone else (the option's own
 * codec, Sodium's settings, shader packs through Iris) keeps seeing the 0..1 they expect, see {@link MixinSimpleOption}.
 ^/
@Mixin(LightmapRenderStateExtractor.class)
public class MixinLightmap {
    @ModifyExpressionValue(method = "extract",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;gamma()Lnet/minecraft/client/OptionInstance;")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
    private Object realBrightness(Object gamma) {
        return BrightnessController.get();
    }
}
*///?}
