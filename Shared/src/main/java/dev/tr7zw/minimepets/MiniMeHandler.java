package dev.tr7zw.minimepets;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.http.ParseException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

public interface MiniMeHandler<T extends TamableAnimal>  {

    static AtomicBoolean steve = new AtomicBoolean(true);

    static WeakHashMap<TamableAnimal, GameProfile> profiles = new WeakHashMap<>();
    static WeakHashMap<TamableAnimal, String> animalNames = new WeakHashMap<>();
    static Map<UUID, GameProfile> cachedProfiles = new HashMap<>();
    static Map<String, UUID> nameCache = new HashMap<>();
    static Set<String> invalidNames = new HashSet<>();

    public default void rendering(T livingEntity, float f, float g, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int i) {
        if (livingEntity.getOwnerUUID() != null) {
            if(livingEntity.getCustomName() != null) {
                if(!livingEntity.getCustomName().toString().equals(animalNames.get(livingEntity))) {
                    profiles.remove(livingEntity);
                }
            }
            if(!profiles.containsKey(livingEntity)) {
                if(livingEntity.getCustomName() != null) {
                    String name = livingEntity.getCustomName().getString();
                    if(invalidNames.contains(name)) {
                        superRender(livingEntity, f, g, poseStack, multiBufferSource, i);
                        return;
                    }
                    nameCache.computeIfAbsent(name, a -> getUuid(name));
                    UUID uuid = nameCache.get(name);
                    if(uuid == null) { // not a valid name
                        invalidNames.add(name);
                        superRender(livingEntity, f, g, poseStack, multiBufferSource, i);
                        return;
                    }
                    animalNames.put(null, name);
                    if(cachedProfiles.containsKey(uuid)) {
                        profiles.put(livingEntity, cachedProfiles.get(uuid));
                    } else {
                        profiles.put(livingEntity, new GameProfile(uuid, null));
                        cachedProfiles.put(profiles.get(livingEntity).getId(), profiles.get(livingEntity));
                        Minecraft.getInstance().getSkinManager().registerSkins(profiles.get(livingEntity), null, false);
                    }
                }else {
                    superRender(livingEntity, f, g, poseStack, multiBufferSource, i);
                    return;
                }
            }
            ResourceLocation id = getSkin(profiles.get(livingEntity));
            VertexConsumer vertices = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(id));
            renderPlayerAS(livingEntity, f, g, poseStack, multiBufferSource, i, vertices, steve.get() ? getDefaultModel() : getSlimModel());
            return;
        }
        superRender(livingEntity, f, g, poseStack, multiBufferSource, i);
    }
    
    public void superRender(T livingEntity, float f, float g, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int i);
    
    public default UUID getUuid(String name) {
        String url = "https://api.mojang.com/users/profiles/minecraft/"+name;
        try {
            String UUIDJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);           
            if(UUIDJson.isEmpty()) {
                return null;                       
            }
            JsonObject jsonObject = new JsonParser().parse(UUIDJson).getAsJsonObject();
            MiniMeShared.LOGGER.info("Got uuid for '" + name + "': " + jsonObject.get("id").getAsString());
            return UUID.fromString(
                    jsonObject.get("id").getAsString()
                    .replaceFirst( 
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5" 
                    )
                );
        } catch (IOException | ParseException e) {
            //ignore
        }
        return null;
    }
    
    public default ResourceLocation getSkin(GameProfile gameProfile) {
        Minecraft minecraft = Minecraft.getInstance();
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(gameProfile);
        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
            String model = ((MinecraftProfileTexture) map.get(MinecraftProfileTexture.Type.SKIN)).getMetadata("model");
            if (model == null || "default".equals(model)) {
                steve.set(true);
            }else {
                steve.set(false);
            }
            return (ResourceLocation) minecraft.getSkinManager().registerTexture(
                    (MinecraftProfileTexture) map.get((Object) MinecraftProfileTexture.Type.SKIN),
                    MinecraftProfileTexture.Type.SKIN);
        }
        return (ResourceLocation) DefaultPlayerSkin
                .getDefaultSkin((UUID) Player.createPlayerUUID((GameProfile) gameProfile));
    }
    
    public float getAnimationProgressRedirect(T entity, float tickDelta);
    public void setupTransformsRedirect(T entity, PoseStack matrices, float animationProgress, float yBodyRot,
            float tickDelta);
    public void scaleRedirect(T entity, PoseStack matrices, float amount);
    public boolean isVisibleRedirect(T entity);
    public float getAnimationCounterRedirect(T entity, float tickDelta);
    public boolean animateShaking(T entity);
    public PlayerModel<TamableAnimal> getDefaultModel();
    public PlayerModel<TamableAnimal> getSlimModel();

    public default void renderPlayerAS(T livingEntity, float f, float g, PoseStack poseStack,
            MultiBufferSource MultiBufferSource, int i, VertexConsumer vertices,
            PlayerModel<TamableAnimal> targetmodel) {
        poseStack.pushPose();
        float scale = livingEntity.isBaby() ? 0.3875f : 0.6375f;
        poseStack.scale(scale, scale, scale);
        targetmodel.attackTime = 0;
        targetmodel.riding = livingEntity.isPassenger() || livingEntity.isInSittingPose();
        targetmodel.young = false;//livingEntity.isBaby();
        targetmodel.crouching = animateShaking(livingEntity) && (System.currentTimeMillis()/100%2==0);
        float h = Mth.rotLerp((float) g, (float) livingEntity.yBodyRotO,
                (float) livingEntity.yBodyRot);
        float j = Mth.rotLerp((float) g, (float) livingEntity.yHeadRotO,
                (float) livingEntity.yHeadRot);
        float k = j - h;
        if (livingEntity.isPassenger() && livingEntity.getVehicle() instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity) livingEntity.getVehicle();
            h = Mth.rotLerp((float) g, (float) livingEntity2.yBodyRotO,
                    (float) livingEntity2.yBodyRot);
            k = j - h;
            float l = Mth.wrapDegrees((float) k);
            if (l < -85.0f) {
                l = -85.0f;
            }
            if (l >= 85.0f) {
                l = 85.0f;
            }
            h = j - l;
            if (l * l > 2500.0f) {
                h += l * 0.2f;
            }
            k = j - h;
        }
        float m = Mth.lerp((float) g, (float) livingEntity.xRotO, (float) livingEntity.getXRot());
        float o = getAnimationProgressRedirect(livingEntity, g);
        this.setupTransformsRedirect(livingEntity, poseStack, o, h, g);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.scaleRedirect(livingEntity, poseStack, g);
        poseStack.translate(0.0, -1.5010000467300415, 0.0);
        if(targetmodel.riding) {
            if(targetmodel.young) {
                poseStack.translate(0.0, 0.3, 0.0);  
            } else {
                poseStack.translate(0.0, 0.5, 0.0);  
            }
        }
        float p = 0.0f;
        float q = 0.0f;
        if (!livingEntity.isPassenger() && livingEntity.isAlive()) {
            p = Mth.lerp((float) g, (float) livingEntity.animationSpeedOld, (float) livingEntity.animationSpeed);
            q = livingEntity.animationPosition - livingEntity.animationSpeed * (1.0f - g);
            if (livingEntity.isBaby()) {
                q *= 3.0f;
            }
            if (p > 1.0f) {
                p = 1.0f;
            }
        }
        targetmodel.prepareMobModel(livingEntity, q, p, g);
        targetmodel.setupAnim(livingEntity, q, p, o, k, m);
        Minecraft minecraft = Minecraft.getInstance();
        boolean bl = this.isVisibleRedirect(livingEntity);
        boolean bl2 = !bl && !livingEntity.isInvisibleTo(minecraft.player);
        int r = LivingEntityRenderer.getOverlayCoords(livingEntity, this.getAnimationCounterRedirect(livingEntity, g));
        targetmodel.renderToBuffer(poseStack, vertices, i, r, 1.0f, 1.0f, 1.0f, bl2 ? 0.15f : 1.0f);
        poseStack.popPose();
        //super.render(livingEntity, f, g, PoseStack, MultiBufferSource, i);
    }
    
}

