//#if FORGE
//$$package dev.tr7zw.minimepets;
//$$
//$$import net.minecraftforge.api.distmarker.Dist;
//$$import net.minecraftforge.fml.DistExecutor;
//$$import net.minecraftforge.fml.common.Mod;
//$$
//$$@Mod("minimepets")
//$$public class MiniMeBootstrap {
//$$
//$$	public MiniMeBootstrap() {
//$$		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> { 
//$$         new MiniMeShared().init();
//$$        });
//$$	}
//$$	
//$$}
//#elseif NEOFORGE
//$$package dev.tr7zw.minimepets;
//$$
//$$import net.neoforged.api.distmarker.Dist;
//$$import net.neoforged.fml.DistExecutor;
//$$import net.neoforged.fml.common.Mod;
//$$
//$$@Mod("minimepets")
//$$public class MiniMeBootstrap {
//$$
//$$	public MiniMeBootstrap() {
//$$		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> { 
//$$         new MiniMeShared().init();
//$$        });
//$$	}
//$$	
//$$}
//#endif
