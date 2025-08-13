package dev.tr7zw.minimepets.mixin;

import com.google.gson.*;
import com.mojang.authlib.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider.*;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.resources.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import org.apache.commons.io.*;
import org.apache.http.*;
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

    private static ExecutorService exector = Executors.newFixedThreadPool(1);
    private static Map<UUID, GameProfile> cachedProfiles = new HashMap<>();
    private static WeakHashMap<String, GameProfile> profiles = new WeakHashMap<>();
    private static Map<String, UUID> nameCache = new HashMap<>();
    private static Set<String> invalidNames = new HashSet<>();
    boolean rendering = false;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(LivingEntityRenderState livingEntity, PoseStack poseStack, MultiBufferSource multiBufferSource,
            int packedLight, CallbackInfo info) {
        if (rendering) {
            return;
        }
        if ((livingEntity instanceof FelineRenderState || livingEntity instanceof WolfRenderState)
                && !livingEntity.isInvisible) {
            GameProfile profile = getProfile(livingEntity.customName);
            if (profile == null) {
                return;
            }
            PlayerSkin skin = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);
            if (skin == null) {
                return;
            }
            ResourceLocation id = skin.texture();
            EntityRenderer playerRenderer = Minecraft.getInstance().getEntityRenderDispatcher()
                    .getRenderer(Minecraft.getInstance().player);
            rendering = true;
            PlayerRenderState fakePlayer = (PlayerRenderState) playerRenderer.createRenderState();
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
            playerRenderer.render(fakePlayer, poseStack, multiBufferSource, packedLight);
            rendering = false;
            info.cancel();
        }
    }

    private GameProfile getProfile(Component customName) {
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
        } catch (IOException | ParseException e) {
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

    private static void remapRenderState(EntityRenderState from, PlayerRenderState to) {
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
