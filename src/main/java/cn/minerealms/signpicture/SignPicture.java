package cn.minerealms.signpicture;

import cn.minerealms.signpicture.command.SignPicCommand;
import cn.minerealms.signpicture.entry.content.ContentManager;
import cn.minerealms.signpicture.handler.ClientEventHandler;
import cn.minerealms.signpicture.handler.KeyHandler;
import cn.minerealms.signpicture.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.File;

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
        modEventBus.addListener(this::registerKeys);

        // 注册Forge事件总线
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("SignPicture-Rebornified initializing...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("SignPicture common setup");

        // 注册网络包
        event.enqueueWork(NetworkHandler::register);

        // 初始化ContentManager
        event.enqueueWork(() -> {
            File gameDir = FMLPaths.GAMEDIR.get().toFile();
            File signpicDir = new File(gameDir, "signpic");
            ContentManager.instance.init(signpicDir);
            LOGGER.info("ContentManager initialized at: " + signpicDir.getAbsolutePath());
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // 仅在客户端执行
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            LOGGER.info("SignPicture client setup");
            // 注册客户端事件处理器
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());

            event.enqueueWork(() -> {
                // 注册渲染器将在后续实现
                // EntityRenderers.register(BlockEntityType.SIGN, SignPictureRenderer::new);
            });
        });
    }

    private void registerKeys(final RegisterKeyMappingsEvent event) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            KeyHandler.instance.registerKeys(event);
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SignPicCommand.register(event.getDispatcher());
        LOGGER.info("SignPicture commands registered");
    }
}
