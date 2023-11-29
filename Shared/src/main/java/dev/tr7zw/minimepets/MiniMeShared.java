package dev.tr7zw.minimepets;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.minimepets.api.Event;
import dev.tr7zw.minimepets.api.EventFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public class MiniMeShared {

    public static final Logger LOGGER = LogManager.getLogger("MiniMePets");
    public static MiniMeShared instance;
    public Event<RenderEvent> renderPreEvent = EventFactory.createEvent();
    public Event<RenderEvent> renderPostEvent = EventFactory.createEvent();
    
    public void init() {
        instance = this;
        LOGGER.info("Loading MiniMePets!");
    }
    
    public record RenderEvent(LivingEntity entity, LivingEntityRenderer renderer, float partialTick, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int packedLight, AtomicBoolean cancled) {}
    
}
