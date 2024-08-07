package dev.tr7zw.minimepets;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.http.ParseException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.tr7zw.minimepets.MiniMeShared.RenderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.PlayerSkin.Model;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

// spotless:off 
//#if MC >= 12005
//#else
//$$ import net.minecraft.nbt.CompoundTag;
//$$ import com.google.common.cache.LoadingCache;
//$$ import java.util.concurrent.CompletableFuture;
//#endif
//spotless:on

public interface MiniMeHandler<T extends TamableAnimal> {

    static AtomicBoolean steve = new AtomicBoolean(true);

    static ExecutorService exector = Executors.newFixedThreadPool(1);

    static WeakHashMap<TamableAnimal, String> animalNames = new WeakHashMap<>();
    // spotless:off 
    //#if MC >= 12005
    static Map<UUID, GameProfile> cachedProfiles = new HashMap<>();
    static WeakHashMap<TamableAnimal, GameProfile> profiles = new WeakHashMap<>();
    //#else
    //$$ static Map<UUID, CompoundTag> cachedProfiles = new HashMap<>();
    //$$ static WeakHashMap<TamableAnimal, CompoundTag> profiles = new WeakHashMap<>();
    //#endif
    //spotless:on
    static Map<String, UUID> nameCache = new HashMap<>();
    static Set<String> invalidNames = new HashSet<>();

    public default void rendering(T livingEntity, float f, float g, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int i) {
        if (livingEntity.getOwnerUUID() != null) {
            if (livingEntity.getCustomName() != null
                    && !livingEntity.getCustomName().getString().equals(animalNames.get(livingEntity))) {
                profiles.remove(livingEntity);
            }
            if (!profiles.containsKey(livingEntity)) {
                if (livingEntity.getCustomName() != null) {
                    profiles.put(livingEntity, null); // set to null, so only one attempt to load is made
                    String name = livingEntity.getCustomName().getString();
                    if (invalidNames.contains(name)) {
                        superRender(livingEntity, f, g, poseStack, multiBufferSource, i);
                        return;
                    }
                    animalNames.put(livingEntity, name);
                    exector.submit(() -> {
                        nameCache.computeIfAbsent(name, a -> getUuid(name));
                        UUID uuid = nameCache.get(name);
                        if (uuid == null) { // not a valid name
                            invalidNames.add(name);
                            return;
                        }
                        if (cachedProfiles.containsKey(uuid)) {
                            profiles.put(livingEntity, cachedProfiles.get(uuid));
                        } else {
                            resolveProfile(livingEntity, name, uuid);
                        }
                    });
                } else {
                    superRender(livingEntity, f, g, poseStack, multiBufferSource, i);
                    return;
                }
            }
            // spotless:off 
            //#if MC >= 12005
            GameProfile profile = profiles.get(livingEntity);
            //#else
            //$$ CompoundTag tag = profiles.get(livingEntity);
            //$$ GameProfile profile = tag != null ? SkullBlockEntity.getOrResolveGameProfile(tag) : null;
            //#endif
            //spotless:on
            if (profile != null) {
                RenderEvent event = new RenderEvent(livingEntity, (LivingEntityRenderer) (Object) this, f, poseStack,
                        multiBufferSource, i, new AtomicBoolean());
                MiniMeShared.instance.renderPreEvent.callEvent(event);
                if (event.cancled().get()) {
                    return;// cancel all rendering
                }
                PlayerSkin skin = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);
                ResourceLocation id = skin.texture();
                steve.set(skin.model() == Model.WIDE);
                VertexConsumer vertices = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(id));
                renderPlayerAS(livingEntity, f, g, poseStack, multiBufferSource, i, vertices,
                        steve.get() ? getDefaultModel() : getSlimModel());
                MiniMeShared.instance.renderPostEvent.callEvent(event);
                return;
            }
        }
        superRender(livingEntity, f, g, poseStack, multiBufferSource, i);
    }

    public default void resolveProfile(T livingEntity, String name, UUID uuid) {
        // spotless:off 
        //#if MC >= 12005
        SkullBlockEntity.fetchGameProfile(uuid).thenAccept(prof -> {
            profiles.put(livingEntity, prof.get());
            cachedProfiles.put(uuid, prof.get());
        });
        //#else
        //$$ CompoundTag tag = new CompoundTag();
        //$$ tag.putString("SkullOwner", name);
        //$$ SkullBlockEntity.resolveGameProfile(tag);
        //$$ profiles.put(livingEntity, tag);
        //$$ cachedProfiles.put(uuid, tag);
        //#endif
        //spotless:on
    }

    public static PlayerSkin getSkin(GameProfile gameProfile) {
        Minecraft minecraftClient = Minecraft.getInstance();
        if (gameProfile.getProperties() == null) {
            return DefaultPlayerSkin.get(getOrCreatePlayerUUID(gameProfile));
        }
        PlayerSkin skin = minecraftClient.getSkinManager().getInsecureSkin(gameProfile);
        if (skin != null) {
            return skin;
        }
        return DefaultPlayerSkin.get(getOrCreatePlayerUUID(gameProfile));
    }

    public static UUID getOrCreatePlayerUUID(GameProfile gameProfile) {
        UUID uUID = gameProfile.getId();
        if (uUID == null) {
            uUID = UUIDUtil.createOfflinePlayerUUID(gameProfile.getName());
        }

        return uUID;
    }

    public void superRender(T livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource,
            int i);

    public default UUID getUuid(String name) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
        try {
            String UUIDJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            if (UUIDJson.isEmpty()) {
                return null;
            }
            JsonObject jsonObject = new JsonParser().parse(UUIDJson).getAsJsonObject();
//            MiniMeShared.LOGGER.info("Got uuid for '" + name + "': " + jsonObject.get("id").getAsString());
            return UUID.fromString(jsonObject.get("id").getAsString().replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5"));
        } catch (IOException | ParseException e) {
            // ignore
        }
        return null;
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

    public default void renderPlayerAS(T livingEntity, float f, float tick, PoseStack poseStack,
            MultiBufferSource MultiBufferSource, int i, VertexConsumer vertices,
            PlayerModel<TamableAnimal> targetmodel) {
        poseStack.pushPose();
        float scale = livingEntity.isBaby() ? 0.3875f : 0.6375f;
        poseStack.scale(scale, scale, scale);
        targetmodel.attackTime = 0;
        targetmodel.riding = livingEntity.isPassenger() || livingEntity.isInSittingPose();
        targetmodel.young = false;// livingEntity.isBaby();
        targetmodel.crouching = animateShaking(livingEntity) && (System.currentTimeMillis() / 100 % 2 == 0);
        float h = Mth.rotLerp((float) tick, (float) livingEntity.yBodyRotO, (float) livingEntity.yBodyRot);
        float j = Mth.rotLerp((float) tick, (float) livingEntity.yHeadRotO, (float) livingEntity.yHeadRot);
        float k = j - h;
        if (livingEntity.isPassenger() && livingEntity.getVehicle() instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity) livingEntity.getVehicle();
            h = Mth.rotLerp((float) tick, (float) livingEntity2.yBodyRotO, (float) livingEntity2.yBodyRot);
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
        float m = Mth.lerp((float) tick, (float) livingEntity.xRotO, (float) livingEntity.getXRot());
        float o = getAnimationProgressRedirect(livingEntity, tick);
        this.setupTransformsRedirect(livingEntity, poseStack, o, h, tick);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.scaleRedirect(livingEntity, poseStack, tick);
        poseStack.translate(0.0, -1.5010000467300415, 0.0);
        if (targetmodel.riding) {
            if (targetmodel.young) {
                poseStack.translate(0.0, 0.3, 0.0);
            } else {
                poseStack.translate(0.0, 0.5, 0.0);
            }
        }
        float p = 0.0f;
        float q = 0.0f;
        if (!livingEntity.isPassenger() && livingEntity.isAlive()) {
            p = livingEntity.walkAnimation.speed(tick);
            q = livingEntity.walkAnimation.position(tick);
            if (livingEntity.isBaby()) {
                q *= 3.0f;
            }
            if (p > 1.0f) {
                p = 1.0f;
            }
        }
        targetmodel.prepareMobModel(livingEntity, q, p, tick);
        targetmodel.setupAnim(livingEntity, q, p, o, k, m);
        Minecraft minecraft = Minecraft.getInstance();
        boolean bl = this.isVisibleRedirect(livingEntity);
        boolean bl2 = !bl && !livingEntity.isInvisibleTo(minecraft.player);
        int r = LivingEntityRenderer.getOverlayCoords(livingEntity,
                this.getAnimationCounterRedirect(livingEntity, tick));
        // spotless:off 
        //#if MC >= 12100
        targetmodel.renderToBuffer(poseStack, vertices, i, r, Integer.MAX_VALUE);
        //#else
        //$$ targetmodel.renderToBuffer(poseStack, vertices, i, r, 1.0f, 1.0f, 1.0f, bl2 ? 0.15f : 1.0f);
        //#endif
        //spotless:on
        poseStack.popPose();
        // super.render(livingEntity, f, g, PoseStack, MultiBufferSource, i);
    }

}
