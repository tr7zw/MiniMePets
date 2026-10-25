package dev.tr7zw.minimepets.mixin;

import com.google.gson.*;
import com.mojang.authlib.*;
import com.mojang.blaze3d.vertex.*;
import dev.tr7zw.minimepets.*;
import net.minecraft.client.*;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider.*;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.resources.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import org.apache.commons.io.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, V extends EntityModel> extends EntityRenderer {

    protected LivingEntityRendererMixin(Context context) {
        super(context);
    }

    //? if <1.21.9 {
    /*private static ExecutorService exector = Executors.newFixedThreadPool(1);
    private static Map<UUID, GameProfile> cachedProfiles = new HashMap<>();
    private static WeakHashMap<String, GameProfile> profiles = new WeakHashMap<>();
    private static Map<String, UUID> nameCache = new HashMap<>();
    *///? } else {
    private static WeakHashMap<String, ResolvableProfile> profiles = new WeakHashMap<>();
    //? }
    private static Set<String> invalidNames = new HashSet<>();
    boolean rendering = false;

    //? if >= 1.21.9 {

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    public void submit(LivingEntityRenderState livingEntity, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState, CallbackInfo info) {
        //? } else {

        /*@Inject(method = "render", at = @At("HEAD"), cancellable = true)
        public void render(LivingEntityRenderState livingEntity, PoseStack poseStack, MultiBufferSource multiBufferSource,
            int packedLight, CallbackInfo info) {
        *///? }
        if (rendering) {
            return;
        }
        if ((livingEntity instanceof FelineRenderState || livingEntity instanceof WolfRenderState)
                && !livingEntity.isInvisible) {
            //? if >= 1.21.9 {

            PlayerSkin skin = getSkin(((LivingExtension) livingEntity).minime$getCustomName());
            //? } else {

            /*PlayerSkin skin = getSkin(livingEntity.customName);
            
            *///? }
            if (skin == null) {
                return;
            }
            EntityRenderer playerRenderer = Minecraft.getInstance().getEntityRenderDispatcher()
                    .getRenderer(Minecraft.getInstance().player);
            rendering = true;
            AvatarRenderState fakePlayer = (AvatarRenderState) playerRenderer.createRenderState();
            remapRenderState(livingEntity, fakePlayer);
            fakePlayer.skin = skin;
            fakePlayer.scale = fakePlayer.isBaby ? 0.3875f : 0.6375f;
            if (fakePlayer.isPassenger) {
                if (fakePlayer.isBaby) {
                    poseStack.translate(0, -0.2, 0);
                } else {
                    poseStack.translate(0, -0.3, 0);
                }
            }
            fakePlayer.isBaby = false;
            //? if >= 1.21.9 {

            playerRenderer.submit(fakePlayer, poseStack, submitNodeCollector, cameraRenderState);
            //? } else {

            /*playerRenderer.render(fakePlayer, poseStack, multiBufferSource, packedLight);
            *///? }
            rendering = false;
            info.cancel();
        }
    }

    //?if < 1.21.9 {
    /*private GameProfile getProfile(Component customName) {
        if (customName == null) {
            return null;
        }
        String name = customName.getString();
        if (!profiles.containsKey(name)) {
            profiles.put(name, null); // set to null, so only one attempt to load is made
            if (invalidNames.contains(name)) {
                return null;
            }
            exector.submit(() -> {
                nameCache.computeIfAbsent(name, a -> getUuid(name));
                UUID uuid = nameCache.get(name);
                if (uuid == null) { // not a valid name
                    invalidNames.add(name);
                    return;
                }
                if (cachedProfiles.containsKey(uuid)) {
                    profiles.put(name, cachedProfiles.get(uuid));
                } else {
                    resolveProfile(name, uuid);
                }
            });
        }
        return profiles.get(name);
    }
    
    private UUID getUuid(String name) {
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
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
    
    private void resolveProfile(String name, UUID uuid) {
        SkullBlockEntity.fetchGameProfile(uuid).thenAccept(prof -> {
            profiles.put(name, prof.get());
            cachedProfiles.put(uuid, prof.get());
        });
    }
    *///? } else {
    private ResolvableProfile getProfile(Component customName) {
        if (customName == null)
            return null;
        String nameString = customName.getString();
        if (!profiles.containsKey(nameString)) {
            profiles.put(nameString, null);
            if (invalidNames.contains(nameString))
                return null;
            CompletableFuture
                    .supplyAsync(() -> Minecraft.getInstance().services().profileResolver().fetchByName(nameString),
                            Util.nonCriticalIoPool())
                    .thenAcceptAsync(profileOpt -> profileOpt.ifPresentOrElse(
                            profile -> profiles.put(nameString, ResolvableProfile.createResolved(profile)),
                            () -> invalidNames.add(nameString)), Minecraft.getInstance());
        }
        return profiles.get(nameString);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"))
    private void onExtractRenderState(LivingEntity entity, LivingEntityRenderState state, float partialTicks,
                                      CallbackInfo ci) {
        ((LivingExtension) state).minime$setCustomName(entity.getCustomName());
    }
    //? }

    private PlayerSkin getSkin(Component name) {
        if (name == null)
            return null;
        //? if >= 1.21.9 {
        ResolvableProfile profile = getProfile(name);
        if (profile == null)
            return null;
        return Minecraft.getInstance().playerSkinRenderCache().lookup(profile).getNow(Optional.empty())
                .map(PlayerSkinRenderCache.RenderInfo::playerSkin).orElse(null);
        //? } else {
        /*GameProfile profile = getProfile(name);
        if (profile == null)
            return null;
        return Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);
        *///? }
    }

    private static void remapRenderState(EntityRenderState from, AvatarRenderState to) {
        for (Field src : from.getClass().getFields()) {
            try {
                Field target = to.getClass().getField(src.getName());
                if (target != null) {
                    target.set(to, src.get(from));
                }
            } catch (Exception ex) {
            }
        }
        if (from instanceof FelineRenderState pet) {
            to.isPassenger = pet.isSitting;
        }
        if (from instanceof WolfRenderState pet) {
            to.isPassenger = pet.isSitting;
            to.isCrouching = pet.shakeAnim > 0 && (System.currentTimeMillis() / 100 % 2 == 0);
        }
    }

}
