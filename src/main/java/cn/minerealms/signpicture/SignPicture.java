package cn.minerealms.signpicture;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * SignPicture-Rebornified 主类
 * 允许在告示牌上显示图片的Minecraft模组
 */
@Mod(ModConstants.MOD_ID)
public class SignPicture {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public SignPicture() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // 注册配置
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        
        // 注册Mod事件
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        
        // 注册Forge事件总线
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("SignPicture-Rebornified initializing...");
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("SignPicture common setup");
        // 通用初始化逻辑
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
        // 仅在客户端执行
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            LOGGER.info("SignPicture client setup");
            // 客户端初始化逻辑
            // 注册渲染器、按键绑定等
        });
    }
}
