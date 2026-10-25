package dev.tr7zw.minimepets.mixin;

import dev.tr7zw.minimepets.LivingExtension;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements LivingExtension {

    @Unique
    private Component minime$customName;

    @Override
    public void minime$setCustomName(Component component) {
        this.minime$customName = component;
    }

    @Override
    public Component minime$getCustomName() {
        return minime$customName;
    }
}
