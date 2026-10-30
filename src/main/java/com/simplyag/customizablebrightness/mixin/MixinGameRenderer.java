package com.simplyag.customizablebrightness.mixin;

//? if >=26.1 {
/*import com.simplyag.customizablebrightness.ShaderLighting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/^*
 * Iris asks this method for its nightVision uniform on every frame, effect or not; vanilla only calls it for entities
 * that have the effect. A real Night Vision effect keeps its vanilla value.
 ^/
@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    //? if >=26.2 {
    @Inject(method = "nightVisionScale", at = @At("HEAD"), cancellable = true)
    //?} else {
    /^@Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)^/
    //?}
    private static void shaderBrightness(LivingEntity entity, float partialTick, CallbackInfoReturnable<Float> info) {
        float nightVision = ShaderLighting.nightVision();
        if (nightVision > 0.0F && !entity.hasEffect(MobEffects.NIGHT_VISION)) {
            info.setReturnValue(nightVision);
        }
    }
}
*///?}
