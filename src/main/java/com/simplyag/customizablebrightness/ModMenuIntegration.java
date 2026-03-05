package com.simplyag.customizablebrightness;

//? if >=1.16.2 {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::create;
    }
}
//?} else {
/*public class ModMenuIntegration {}*/
//?}
