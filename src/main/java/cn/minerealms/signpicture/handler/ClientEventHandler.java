package cn.minerealms.signpicture.handler;

import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.entry.EntryManager;
import cn.minerealms.signpicture.entry.content.ContentManager;
import cn.minerealms.signpicture.render.SignHandlerV2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 客户端事件处理器
 */
@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {

    private int tickCount = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCount++;

            // Tick所有Entry
            EntryManager.instance.tickAll();

            // 定期垃圾回收和纹理清理
            if (tickCount % 300 == 0) { // 每15秒
                EntryManager.instance.collectGarbage();
                ContentManager.instance.collectGarbage();
                SignHandlerV2.INSTANCE.cleanupExpiredTextures();
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // 渲染tick处理
        }
    }

    /**
     * 世界卸载时清理所有纹理
     */
    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            Log.info("World unloading, clearing all textures");
            SignHandlerV2.INSTANCE.clearAllTextures();
            EntryManager.instance.clear();
            ContentManager.instance.clear();
        }
    }

    /**
     * 资源重载时清理纹理
     * 支持F3+T重载资源包
     */
    @SubscribeEvent
    public void onResourceReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
            return preparationBarrier.wait(null).thenRunAsync(() -> {
                Log.info("Resource reloading, clearing dynamic textures");
                SignHandlerV2.INSTANCE.clearAllTextures();
            }, gameExecutor);
        });
    }
}
