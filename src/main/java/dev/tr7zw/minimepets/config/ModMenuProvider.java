//? if fabric {

package dev.tr7zw.minimepets.config;

import com.terraformersmc.modmenu.api.*;

public class ModMenuProvider implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreenProvider::createConfigScreen;
    }

}
//? }
