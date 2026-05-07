package cn.minerealms.signpicture.render;

import cn.minerealms.signpicture.entry.Entry;
import cn.minerealms.signpicture.entry.EntryId;
import cn.minerealms.signpicture.entry.EntryManager;
import cn.minerealms.signpicture.entry.content.Content;
import cn.minerealms.signpicture.entry.content.ContentId;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

/**
 * 告示牌渲染处理器
 * 负责在告示牌上渲染图片
 */
public class SignHandler {
    public static final SignHandler instance = new SignHandler();
    
    private final Minecraft mc = Minecraft.getInstance();
    
    /**
     * 渲染告示牌图片
     */
    public void render(@Nonnull SignBlockEntity sign, 
                      @Nonnull PoseStack poseStack,
                      @Nonnull MultiBufferSource bufferSource,
                      int packedLight,
                      int packedOverlay) {
        
        // 从告示牌文本提取URL
        String url = extractImageUrl(sign);
        if (url == null || url.isEmpty()) {
            return;
        }
        
        // 获取Entry
        EntryId entryId = EntryId.from(sign.getBlockPos().toString());
        ContentId contentId = ContentId.from(url);
        Entry entry = EntryManager.instance.get(entryId, contentId);
        
        // 检查内容是否可用
        Content content = entry.getContent();
        if (!content.isAvailable()) {
            return;
        }
        
        // 渲染图片
        BufferedImage image = content.getImage();
        if (image != null) {
            renderImage(poseStack, image, packedLight);
        }
    }
    
    /**
     * 从告示牌文本提取图片URL
     */
    private String extractImageUrl(@Nonnull SignBlockEntity sign) {
        // 获取告示牌正面文本
        var frontText = sign.getFrontText();
        StringBuilder url = new StringBuilder();
        
        // 拼接4行文本
        for (int i = 0; i < 4; i++) {
            Component line = frontText.getMessage(i, false);
            String text = line.getString().trim();
            if (!text.isEmpty()) {
                url.append(text);
            }
        }
        
        String result = url.toString();
        
        // 检查是否是有效URL
        if (result.startsWith("http://") || result.startsWith("https://") || 
            result.startsWith("signpic:") || result.contains(".")) {
            return result;
        }
        
        return null;
    }
    
    /**
     * 渲染图片到告示牌位置
     */
    private void renderImage(@Nonnull PoseStack poseStack, @Nonnull BufferedImage image, int packedLight) {
        poseStack.pushPose();
        
        // 移动到告示牌中心
        poseStack.translate(0.5, 0.5, 0.5);
        
        // 缩放到合适大小
        float scale = 0.01f;
        poseStack.scale(scale, scale, scale);
        
        // 转换BufferedImage到NativeImage
        com.mojang.blaze3d.platform.NativeImage nativeImage = convertToNativeImage(image);
        
        // 上传纹理
        DynamicTexture texture = new DynamicTexture(nativeImage);
        ResourceLocation textureLocation = mc.getTextureManager().register("signpicture_temp", texture);
        
        // 绑定纹理
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, textureLocation);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        
        // 渲染四边形
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        float w = image.getWidth() / 2f;
        float h = image.getHeight() / 2f;
        
        builder.vertex(matrix, -w, -h, 0).uv(0, 0).endVertex();
        builder.vertex(matrix, -w, h, 0).uv(0, 1).endVertex();
        builder.vertex(matrix, w, h, 0).uv(1, 1).endVertex();
        builder.vertex(matrix, w, -h, 0).uv(1, 0).endVertex();
        
        BufferUploader.drawWithShader(builder.end());
        
        RenderSystem.disableBlend();
        
        poseStack.popPose();
        
        // 释放纹理
        mc.getTextureManager().release(textureLocation);
        texture.close();
    }
    
    /**
     * 转换BufferedImage到NativeImage
     */
    private com.mojang.blaze3d.platform.NativeImage convertToNativeImage(@Nonnull BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        com.mojang.blaze3d.platform.NativeImage nativeImage = 
            new com.mojang.blaze3d.platform.NativeImage(width, height, true);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = bufferedImage.getRGB(x, y);
                nativeImage.setPixelRGBA(x, y, rgb);
            }
        }
        
        return nativeImage;
    }
}
