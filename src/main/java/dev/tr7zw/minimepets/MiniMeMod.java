//? if fabric {
package dev.tr7zw.minimepets;

import net.fabricmc.api.ClientModInitializer;

public class MiniMeMod extends MiniMeShared implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        init();
    }
}
//?}
