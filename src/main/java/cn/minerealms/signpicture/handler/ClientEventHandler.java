package cn.minerealms.signpicture.handler;

import cn.minerealms.signpicture.entry.EntryManager;
import cn.minerealms.signpicture.entry.content.ContentManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
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
            
            // 定期垃圾回收
            if (tickCount % 300 == 0) { // 每15秒
                EntryManager.instance.collectGarbage();
                ContentManager.instance.collectGarbage();
            }
        }
    }
    
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // TODO: 渲染tick处理
        }
    }
}
