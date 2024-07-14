package dev.tr7zw.minimepets.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.minimepets.MiniMeHandler;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;

@Mixin(CatRenderer.class)
public abstract class CatRendererMixin extends MobRenderer<Cat, CatModel<Cat>> implements MiniMeHandler<Cat> {

    private PlayerModel<TamableAnimal> defaultModel;
    private PlayerModel<TamableAnimal> thinModel;

    public CatRendererMixin(Context context, CatModel<Cat> arg2, float f) {
        super(context, arg2, f);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(Context context, CallbackInfo info) {
        defaultModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        thinModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    public void render(Cat livingEntity, float f, float g, PoseStack matrixStack,
            MultiBufferSource vertexConsumerProvider, int i) {
        rendering(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public void superRender(Cat livingEntity, float f, float g, PoseStack PoseStack,
            MultiBufferSource MultiBufferSource, int i) {
        super.render(livingEntity, f, g, PoseStack, MultiBufferSource, i);
    }

    @Override
    public float getAnimationProgressRedirect(Cat entity, float tickDelta) {
        return getBob(entity, tickDelta);
    }

    @Override
    public void setupTransformsRedirect(Cat entity, PoseStack matrices, float animationProgress, float bodyYaw,
            float tickDelta) {
        // spotless:off 
        //#if MC >= 12005
        setupRotations(entity, matrices, animationProgress, bodyYaw, tickDelta, 0);
        //#else
        //$$ setupRotations(entity, matrices, animationProgress, bodyYaw, tickDelta);
        //#endif
        //spotless:on
    }

    @Override
    public void scaleRedirect(Cat entity, PoseStack matrices, float amount) {
        scale(entity, matrices, amount);
    }

    @Override
    public boolean isVisibleRedirect(Cat entity) {
        return isBodyVisible(entity);
    }

    @Override
    public float getAnimationCounterRedirect(Cat entity, float tickDelta) {
        return getWhiteOverlayProgress(entity, tickDelta);
    }

    @Override
    public boolean animateShaking(Cat entity) {
        return false;
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