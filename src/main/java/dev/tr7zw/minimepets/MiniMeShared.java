package dev.tr7zw.minimepets;

import com.mojang.blaze3d.vertex.*;
import dev.tr7zw.transition.event.*;
import dev.tr7zw.transition.loader.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.entity.*;
import org.apache.logging.log4j.*;

import java.util.concurrent.atomic.*;

public class MiniMeShared {

    public static final Logger LOGGER = LogManager.getLogger("MiniMePets");
    public static MiniMeShared instance;
    public Event<RenderEvent> renderPreEvent = EventFactory.createEvent();
    public Event<RenderEvent> renderPostEvent = EventFactory.createEvent();

    public void init() {
        instance = this;
        LOGGER.info("Loading MiniMePets!");
        ModLoaderUtil.disableDisplayTest();
    }

    public record RenderEvent(LivingEntity entity, LivingEntityRenderer renderer, float partialTick,
            PoseStack poseStack,
  //? if < 26.3 {
  /*
  MultiBufferSource multiBufferSource,
   */
  //? }
                              int packedLight, AtomicBoolean cancled) {
    }

}
