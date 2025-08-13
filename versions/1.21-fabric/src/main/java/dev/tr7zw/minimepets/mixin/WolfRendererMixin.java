package dev.tr7zw.minimepets.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.minimepets.MiniMeHandler;
import dev.tr7zw.minimepets.WolfAccess;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;

@Mixin(WolfRenderer.class)
public abstract class WolfRendererMixin extends MobRenderer<Wolf, WolfModel<Wolf>> implements MiniMeHandler<Wolf> {

    private PlayerModel<TamableAnimal> defaultModel;
    private PlayerModel<TamableAnimal> thinModel;

    public WolfRendererMixin(Context context, WolfModel<Wolf> arg2, float f) {
        super(context, arg2, f);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(Context context, CallbackInfo info) {
        defaultModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        thinModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(Wolf livingEntity, float f, float g, PoseStack PoseStack, MultiBufferSource MultiBufferSource,
            int i, CallbackInfo info) {
        rendering(livingEntity, f, g, PoseStack, MultiBufferSource, i);
        info.cancel();
    }

    @Override
    public void superRender(Wolf livingEntity, float f, float g, PoseStack PoseStack,
            MultiBufferSource MultiBufferSource, int i) {
        super.render(livingEntity, f, g, PoseStack, MultiBufferSource, i);
    }

    @Override
    public float getAnimationProgressRedirect(Wolf entity, float tickDelta) {
        return getBob(entity, tickDelta);
    }

    @Override
    public void setupTransformsRedirect(Wolf entity, PoseStack matrices, float animationProgress, float bodyYaw,
            float tickDelta) {
        //? if >= 1.20.5 {
        setupRotations(entity, matrices, animationProgress, bodyYaw, tickDelta, 0);
        //? } else {
        // setupRotations(entity, matrices, animationProgress, bodyYaw, tickDelta);
        //? }
    }

    @Override
    public void scaleRedirect(Wolf entity, PoseStack matrices, float amount) {
        scale(entity, matrices, amount);
    }

    @Override
    public boolean isVisibleRedirect(Wolf entity) {
        return isBodyVisible(entity);
    }

    @Override
    public float getAnimationCounterRedirect(Wolf entity, float tickDelta) {
        return getWhiteOverlayProgress(entity, tickDelta);
    }

    @Override
    public boolean animateShaking(Wolf entity) {
        return ((WolfAccess) entity).isShaking();
    }

    @Override
    public PlayerModel<TamableAnimal> getDefaultModel() {
        return defaultModel;
    }

    @Override
    public PlayerModel<TamableAnimal> getSlimModel() {
        return thinModel;
    }

}
