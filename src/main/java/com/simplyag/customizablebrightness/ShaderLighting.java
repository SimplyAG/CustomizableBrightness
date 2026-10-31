package com.simplyag.customizablebrightness;

//? if >=26.1 {
/*import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;

/^*
 * Brightness above 100% under a shader pack. Packs light the world themselves from two Iris uniforms: screenBrightness
 * (the gamma option, which they expect to be 0..1) and nightVision (0..1, which every pack surveyed turns into its own
 * extra lighting). So the part above 100% arrives as night vision.
 *
 * <p>Iris is optional, so its public API is reached by reflection.
 ^/
public final class ShaderLighting {
    private static final Object IRIS_API;
    private static final Method SHADER_PACK_IN_USE;

    static {
        try {
            boolean iris = FabricLoader.getInstance().isModLoaded("iris");
            Class<?> api = iris ? Class.forName("net.irisshaders.iris.api.v0.IrisApi") : null;
            IRIS_API = iris ? api.getMethod("getInstance").invoke(null) : null;
            SHADER_PACK_IN_USE = iris ? api.getMethod("isShaderPackInUse") : null;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Iris API not usable", e);
        }
    }

    private ShaderLighting() {
    }

    /^* Nothing at 100%, full at 1000%, even steps in between. ^/
    public static float nightVision() {
        double brightness = BrightnessController.get();
        if (brightness <= 1.0 || !shaderPackInUse()) {
            return 0.0F;
        }
        return (float) Math.min(Math.log10(brightness), 1.0);
    }

    private static boolean shaderPackInUse() {
        if (IRIS_API == null) {
            return false;
        }
        try {
            return (boolean) SHADER_PACK_IN_USE.invoke(IRIS_API);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Iris API not usable", e);
        }
    }
}
*///?}
