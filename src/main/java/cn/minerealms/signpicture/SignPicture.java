package cn.minerealms.signpicture;

import cn.minerealms.signpicture.command.SignPicCommand;
import cn.minerealms.signpicture.data.SignPictureDataManagerServer;
import cn.minerealms.signpicture.entry.content.ContentManager;
import cn.minerealms.signpicture.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
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

@Mod(ModConstants.MOD_ID)
public class SignPicture {

    private static final Logger LOGGER = LogUtils.getLogger();

    public SignPicture() {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (proxyHost != null && proxyPort != null) {
            LOGGER.info("Using HTTP proxy: " + proxyHost + ":" + proxyPort);
        }

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        LOGGER.info("SignPicture-Rebornified initializing...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("SignPicture common setup");
        
        NetworkHandler.register();

        File gameDir = FMLPaths.GAMEDIR.get().toFile();
        File signpicDir = new File(gameDir, "signpic");
        ContentManager.instance.init(signpicDir);
        LOGGER.info("ContentManager initialized at: " + signpicDir.getAbsolutePath());

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        DistExecutor.unsafeRunWhenOn(
            net.minecraftforge.api.distmarker.Dist.CLIENT, 
            () -> () -> {
                try {
                    MinecraftForge.EVENT_BUS.register(
                        Class.forName("cn.minerealms.signpicture.handler.ClientEventHandler")
                            .getConstructor().newInstance()
                    );

                    File gameDir = FMLPaths.GAMEDIR.get().toFile();
                    Class.forName("cn.minerealms.signpicture.data.SignPictureDataManagerClient")
                        .getMethod("init", File.class).invoke(null, gameDir);

                    var beType = Class.forName("net.minecraft.world.level.block.entity.BlockEntityType");
                    var renderers = Class.forName("net.minecraft.client.renderer.blockentity.BlockEntityRenderers");
                    var ctx = Class.forName("net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context");
                    var renderer = Class.forName("cn.minerealms.signpicture.render.SignPictureRenderer");

                    renderers.getMethod("register", beType, ctx)
                        .invoke(null, beType.getField("SIGN").get(null), 
                            renderer.getConstructor(ctx).newInstance((Object)null));
                    renderers.getMethod("register", beType, ctx)
                        .invoke(null, beType.getField("HANGING_SIGN").get(null), 
                            renderer.getConstructor(ctx).newInstance((Object)null));

                    Class.forName("cn.minerealms.signpicture.handler.KeyHandler")
                        .getMethod("registerKeys").invoke(null);
                    
                    LOGGER.info("SignPicture client setup complete");
                } catch (Exception e) {
                    LOGGER.error("Failed client setup", e);
                }
            }
        );
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SignPicCommand.register(event.getDispatcher());
        LOGGER.info("SignPicture commands registered");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        SignPictureDataManagerServer.INSTANCE.init(event.getServer());
        LOGGER.info("SignPictureDataManagerServer initialized");
    }
}