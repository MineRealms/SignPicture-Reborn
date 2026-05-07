package cn.minerealms.signpicture.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 书本编辑界面Mixin
 * 用于在书本GUI中渲染图片
 */
@Mixin(BookEditScreen.class)
public class BookEditScreenMixin {
    
    /**
     * 注入render方法
     * TODO: 实现书本中的图片渲染
     */
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // TODO: 渲染书本中的图片
    }
}
