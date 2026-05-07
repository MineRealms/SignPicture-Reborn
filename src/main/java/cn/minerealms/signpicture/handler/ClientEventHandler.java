package cn.minerealms.signpicture.handler;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 客户端事件处理器
 */
@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // TODO: 处理客户端tick
            // - 更新动画
            // - 垃圾回收
            // - 内容加载
        }
    }
    
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // TODO: 渲染tick处理
        }
    }
}
