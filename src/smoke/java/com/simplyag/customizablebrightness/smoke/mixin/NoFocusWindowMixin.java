package com.simplyag.customizablebrightness.smoke.mixin;

//? if >=26.1 {
/*import com.mojang.blaze3d.platform.Window;
*///?} else {
import net.minecraft.client.util.Window;
//?}
//? if <26.3 {
import org.lwjgl.glfw.GLFW;
//?} else {
/*import org.lwjgl.sdl.SDLHints;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Test-only: the smoke client's window opens without taking keyboard focus, so a sweep does not pull the desktop
 * away from whoever is using the machine. The harness never needs focus: it calls the game's key handler directly
 * and runs with pauseOnLostFocus off. GLFW versions get window hints just before glfwCreateWindow; 26.3 creates its
 * window through SDL, which takes the same request as a hint set before the window is created.
 */
@Mixin(value = Window.class, remap = false)
public class NoFocusWindowMixin {
    //? if <26.1 {
    @Inject(method = "<init>", remap = false,
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J", remap = false))
    private void cbsmoke$openWithoutFocus(CallbackInfo ci) {
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(0x0002000C, GLFW.GLFW_FALSE); // GLFW_FOCUS_ON_SHOW (GLFW 3.3; older versions ignore it)
    }
    //?} else if <26.3 {
    /*// 26.1 and 26.2 moved the glfwCreateWindow call out of the constructor into a static helper.
    @Inject(method = "createGlfwWindow", remap = false,
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J", remap = false))
    private static void cbsmoke$openWithoutFocus(CallbackInfoReturnable<Long> cir) {
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(0x0002000C, GLFW.GLFW_FALSE); // GLFW_FOCUS_ON_SHOW
    }
    *///?} else {
    /*@Inject(method = "createWindow", remap = false, at = @At("HEAD"))
    private void cbsmoke$openWithoutFocus(CallbackInfoReturnable<Long> cir) {
        SDLHints.SDL_SetHint("SDL_WINDOW_ACTIVATE_WHEN_SHOWN", "0");
    }
    *///?}
}
