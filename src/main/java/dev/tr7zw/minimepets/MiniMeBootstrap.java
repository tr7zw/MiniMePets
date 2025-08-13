//? if forge {
/*package dev.tr7zw.minimepets;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import dev.tr7zw.transition.loader.ModLoaderUtil;

@Mod("minimepets")
public class MiniMeBootstrap {

	public MiniMeBootstrap(FMLJavaModLoadingContext context) {
      ModLoaderUtil.setModLoadingContext(context);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> { 
         new MiniMeShared().init();
        });
      }
      public MiniMeBootstrap() {
          this(FMLJavaModLoadingContext.get());
      }
	
}
*///?} else if neoforge {
/*package dev.tr7zw.minimepets;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.common.Mod;
import dev.tr7zw.transition.loader.ModLoaderEventUtil;

@Mod("minimepets")
public class MiniMeBootstrap {

    public MiniMeBootstrap() {
            if (FMLEnvironment.dist.isClient()){
                ModLoaderEventUtil.registerClientSetupListener(() -> new MiniMeShared().init());
            }
    }
    
}
*///?}
