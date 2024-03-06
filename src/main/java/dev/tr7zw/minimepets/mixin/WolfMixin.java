package dev.tr7zw.minimepets.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.tr7zw.minimepets.WolfAccess;
import net.minecraft.world.entity.animal.Wolf;

@Mixin(Wolf.class)
public class WolfMixin implements WolfAccess {

    @Shadow
    private boolean isShaking;

    @Override
    public boolean isShaking() {
        return isShaking;
    }

}
