package cn.minerealms.signpicture.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

/**
 * 图片渲染工具类
 * 提供通用的图片渲染功能
 */
public class ImageRenderer {

    /**
     * 渲染图片到指定位置
     *
     * @param guiGraphics GUI图形上下文
     * @param image 要渲染的图片
     * @param x X坐标
     * @param y Y坐标
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     */
    public static void renderImage(@Nonnull GuiGraphics guiGraphics,
                                   @Nonnull BufferedImage image,
                                   int x, int y,
                                   int maxWidth, int maxHeight) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = guiGraphics.pose();

        // 计算缩放比例
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        float scale = Math.min(
            (float) maxWidth / imgWidth,
            (float) maxHeight / imgHeight
        );

        int renderWidth = (int) (imgWidth * scale);
        int renderHeight = (int) (imgHeight * scale);

        poseStack.pushPose();

        try {
            // 转换BufferedImage到NativeImage
            NativeImage nativeImage = convertToNativeImage(image);

            // 上传纹理
            DynamicTexture texture = new DynamicTexture(nativeImage);
            ResourceLocation textureLocation = mc.getTextureManager().register("signpicture_gui", texture);

            // 绑定纹理
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, textureLocation);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            // 渲染四边形
            Matrix4f matrix = poseStack.last().pose();
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            builder.vertex(matrix, x, y + renderHeight, 0).uv(0, 1).endVertex();
            builder.vertex(matrix, x + renderWidth, y + renderHeight, 0).uv(1, 1).endVertex();
            builder.vertex(matrix, x + renderWidth, y, 0).uv(1, 0).endVertex();
            builder.vertex(matrix, x, y, 0).uv(0, 0).endVertex();

            BufferUploader.drawWithShader(builder.end());

            RenderSystem.disableBlend();

            // 释放纹理
            mc.getTextureManager().release(textureLocation);
            texture.close();
        } catch (Exception e) {
            // 忽略渲染错误
        }

        poseStack.popPose();
    }

    /**
     * 转换BufferedImage到NativeImage
     */
    private static NativeImage convertToNativeImage(@Nonnull BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        NativeImage nativeImage = new NativeImage(width, height, true);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = bufferedImage.getRGB(x, y);
                // ARGB to ABGR conversion
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int abgr = (a << 24) | (b << 16) | (g << 8) | r;
                nativeImage.setPixelRGBA(x, y, abgr);
            }
        }

        return nativeImage;
    }
}
