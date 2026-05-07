package cn.minerealms.signpicture.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

/**
 * 图片渲染工具类 - 简化版，先测试流程
 */
public class ImageRenderer {

    public static void renderImage(@Nonnull GuiGraphics guiGraphics,
                                   @Nonnull BufferedImage image,
                                   int x, int y,
                                   int maxWidth, int maxHeight) {
        try {
            // 先测试 - 画一个彩色方块
            int w = Math.min(image.getWidth(), maxWidth);
            int h = Math.min(image.getHeight(), maxHeight);

            // 蓝色测试方块
            guiGraphics.fill(x, y, x + w, y + h, 0xFF4444FF);
        } catch (Exception e) {
            // 错误时画灰色方块
            guiGraphics.fill(x, y, x + maxWidth, y + maxHeight, 0xFF444444);
        }
    }
}