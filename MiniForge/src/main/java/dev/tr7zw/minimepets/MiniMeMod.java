package dev.tr7zw.minimepets;

import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod("minimepets")
public class MiniMeMod extends MiniMeShared {

    public MiniMeMod() {
        try {
            Class clientClass = net.minecraft.client.Minecraft.class;
        }catch(Throwable ex) {
            LOGGER.warn("MiniMePets Mod installed on a Server. Going to sleep.");
            return;
        }
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString(),
                        (remote, isServer) -> true));
        init();
        renderPreEvent.register((event) -> {
            RenderLivingEvent.Pre preEvent = new RenderLivingEvent.Pre<>(event.entity(), event.renderer(), event.partialTick(), event.poseStack(), event.multiBufferSource(), event.packedLight());
            MinecraftForge.EVENT_BUS.post(preEvent);
            event.cancled().set(preEvent.isCanceled());
        });
        renderPostEvent.register((event) -> {
            RenderLivingEvent.Post postEvent = new RenderLivingEvent.Post<>(event.entity(), event.renderer(), event.partialTick(), event.poseStack(), event.multiBufferSource(), event.packedLight());
            MinecraftForge.EVENT_BUS.post(postEvent);
            event.cancled().set(postEvent.isCanceled());
        });
    }

}
