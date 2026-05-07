package cn.minerealms.signpicture.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 聊天组件Mixin
 * 用于在聊天中渲染图片
 */
@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    
    /**
     * 注入render方法
     * TODO: 实现聊天中的图片渲染
     */
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, CallbackInfo ci) {
        // TODO: 渲染聊天中的图片
    }
}
