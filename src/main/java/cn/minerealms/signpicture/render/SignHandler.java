package cn.minerealms.signpicture.render;

import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.attr.AttrReaders;
import cn.minerealms.signpicture.attr.prop.OffsetData;
import cn.minerealms.signpicture.attr.prop.RotationData;
import cn.minerealms.signpicture.attr.prop.SizeData;
import cn.minerealms.signpicture.entry.Entry;
import cn.minerealms.signpicture.entry.EntryId;
import cn.minerealms.signpicture.entry.EntryManager;
import cn.minerealms.signpicture.entry.content.Content;
import cn.minerealms.signpicture.entry.content.ContentId;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告示牌渲染处理器 - 鲁棒性增强版本
 * 使用Minecraft原生纹理系统，完全兼容渲染管线
 *
 * 鲁棒性改进：
 * - 线程安全的缓存（ConcurrentHashMap）
 * - 完整的null检查和边界验证
 * - 异常捕获和降级处理
 * - 资源泄漏防护
 * - 图片尺寸限制
 */
public class SignHandler {
    public static final SignHandler instance = new SignHandler();
    private final Minecraft mc = Minecraft.getInstance();

    // 纹理缓存：使用线程安全的Map
    private final Map<String, ResourceLocation> textureCache = new ConcurrentHashMap<>();
    private final Map<String, Long> textureAccessTime = new ConcurrentHashMap<>();

    // 配置常量
    private static final long TEXTURE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟
    private static final int MAX_TEXTURE_SIZE = 4096; // 最大纹理尺寸
    private static final int MAX_CACHE_SIZE = 100; // 最大缓存数量
    private static final float MIN_SIZE = 0.01f; // 最小渲染尺寸
    private static final float MAX_SIZE = 10.0f; // 最大渲染尺寸

    private SignHandler() {
    }

    /**
     * 将字符串转换为MD5 hash
     * 用于生成合法的ResourceLocation名称
     */
    @Nonnull
    private String toHash(@Nonnull String input) {
        if (input == null || input.isEmpty()) {
            return "empty_" + System.currentTimeMillis();
        }

        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            Log.error("Failed to hash input, using fallback", e);
            // 降级方案：使用时间戳+简单替换
            return "fallback_" + System.currentTimeMillis() + "_" +
                   input.replaceAll("[^a-z0-9_.-]", "_").toLowerCase().substring(0, Math.min(input.length(), 20));
        }
    }

    /**
     * 主渲染方法 - 完整的错误处理
     */
    public void render(@Nonnull SignBlockEntity sign,
                       @Nonnull PoseStack poseStack,
                       @Nonnull MultiBufferSource bufferSource,
                       int packedLight,
                       int packedOverlay) {

        // 防御性检查
        if (sign == null || poseStack == null || bufferSource == null) {
            Log.error("Null parameter in render()");
            return;
        }

        try {
            renderInternal(sign, poseStack, bufferSource, packedLight, packedOverlay);
        } catch (Exception e) {
            // 捕获所有异常，避免崩溃渲染线程
            Log.error("Exception in SignHandler.render()", e);
        }
    }

    /**
     * 内部渲染方法
     */
    private void renderInternal(@Nonnull SignBlockEntity sign,
                                @Nonnull PoseStack poseStack,
                                @Nonnull MultiBufferSource bufferSource,
                                int packedLight,
                                int packedOverlay) {

        // 1. 提取完整URL（包含属性）
        String fullUrl = extractFullUrl(sign);
        if (fullUrl == null || fullUrl.isEmpty()) {
            return;
        }

        // 2. 获取图片内容
        EntryId entryId = EntryId.from(sign.getBlockPos().toString());
        ContentId contentId = ContentId.from(fullUrl);

        if (entryId == null || contentId == null) {
            Log.error("Failed to create EntryId or ContentId");
            return;
        }

        Entry entry = EntryManager.instance.get(entryId, contentId);
        if (entry == null) {
            return;
        }

        Content content = entry.getContent();
        if (content == null || !content.isAvailable()) {
            return;
        }

        // 3. 获取当前帧图片（支持GIF动画）
        BufferedImage image = null;
        try {
            if (content.isAnimated()) {
                int currentFrame = entry.getCurrentFrame();
                int frameCount = content.getFrameCount();
                // 边界检查
                if (currentFrame >= 0 && currentFrame < frameCount) {
                    image = content.getFrame(currentFrame);
                } else {
                    image = content.getFrame(0); // 降级到第一帧
                }
            } else {
                image = content.getImage();
            }
        } catch (Exception e) {
            Log.error("Failed to get image frame", e);
            return;
        }

        if (image == null) {
            return;
        }

        // 4. 验证图片尺寸
        if (!validateImageSize(image)) {
            Log.error("Image size exceeds maximum: " + image.getWidth() + "x" + image.getHeight());
            return;
        }

        // 5. 解析渲染属性（带降级处理）
        SizeData size;
        RotationData rotation;
        OffsetData offset;

        try {
            AttrReaders attrs = new AttrReaders(fullUrl);
            size = attrs.getSizeData();
            rotation = attrs.getRotationData();
            offset = attrs.getOffsetData();

            // null检查
            if (size == null) size = SizeData.DefaultSize;
            if (rotation == null) rotation = RotationData.DefaultRotation;
            if (offset == null) offset = OffsetData.DefaultOffset;

        } catch (Exception e) {
            Log.error("Failed to parse attributes, using defaults", e);
            size = SizeData.DefaultSize;
            rotation = RotationData.DefaultRotation;
            offset = OffsetData.DefaultOffset;
        }

        // 6. 验证和限制属性值
        size = clampSize(size);

        // 7. 获取或创建纹理
        String textureKey = toHash(contentId.getID());
        ResourceLocation textureLocation = getOrCreateTexture(textureKey, image);
        if (textureLocation == null) {
            return;
        }

        // 8. 渲染图片到告示牌
        try {
            renderImageToSign(poseStack, bufferSource, textureLocation,
                             size, rotation, offset, packedLight, packedOverlay);
        } catch (Exception e) {
            Log.error("Failed to render image to sign", e);
        }
    }

    /**
     * 验证图片尺寸
     */
    private boolean validateImageSize(@Nonnull BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        return width > 0 && height > 0 &&
               width <= MAX_TEXTURE_SIZE && height <= MAX_TEXTURE_SIZE;
    }

    /**
     * 限制size属性到合理范围
     */
    @Nonnull
    private SizeData clampSize(@Nonnull SizeData size) {
        float width = Math.max(MIN_SIZE, Math.min(MAX_SIZE, size.getWidth()));
        float height = Math.max(MIN_SIZE, Math.min(MAX_SIZE, size.getHeight()));

        if (width != size.getWidth() || height != size.getHeight()) {
            Log.debug("Clamped size from (" + size.getWidth() + "," + size.getHeight() +
                     ") to (" + width + "," + height + ")");
            return new SizeData(width, height);
        }

        return size;
    }

    /**
     * 从告示牌提取完整URL（包含4行文本）
     */
    @Nullable
    private String extractFullUrl(@Nonnull SignBlockEntity sign) {
        try {
            var frontText = sign.getFrontText();
            if (frontText == null) {
                return null;
            }

            StringBuilder url = new StringBuilder();

            for (int i = 0; i < 4; i++) {
                Component line = frontText.getMessage(i, false);
                if (line == null) continue;

                String text = line.getString();
                if (text != null && !text.trim().isEmpty()) {
                    url.append(text.trim());
                }
            }

            String result = url.toString();

            // 验证是否是有效URL
            if (result.isEmpty()) {
                return null;
            }

            if (result.startsWith("http://") || result.startsWith("https://") || result.contains(".")) {
                return result;
            }

            return null;

        } catch (Exception e) {
            Log.error("Failed to extract URL from sign", e);
            return null;
        }
    }

    /**
     * 获取或创建纹理 - 增强的错误处理和缓存管理
     */
    @Nullable
    private ResourceLocation getOrCreateTexture(@Nonnull String cacheKey, @Nonnull BufferedImage image) {
        if (cacheKey == null || cacheKey.isEmpty() || image == null) {
            return null;
        }

        // 检查缓存
        if (textureCache.containsKey(cacheKey)) {
            textureAccessTime.put(cacheKey, System.currentTimeMillis());
            return textureCache.get(cacheKey);
        }

        // 检查缓存大小限制
        if (textureCache.size() >= MAX_CACHE_SIZE) {
            Log.debug("Texture cache full, cleaning up old textures");
            cleanupOldestTextures(10); // 清理10个最老的纹理
        }

        try {
            // 转换BufferedImage到NativeImage
            NativeImage nativeImage = convertToNativeImage(image);
            if (nativeImage == null) {
                Log.error("Failed to convert BufferedImage to NativeImage");
                return null;
            }

            // 创建DynamicTexture
            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);

            // 注册到Minecraft的TextureManager
            ResourceLocation location = mc.getTextureManager().register(
                "signpicture/dynamic/" + cacheKey,
                dynamicTexture
            );

            if (location == null) {
                Log.error("TextureManager.register() returned null");
                nativeImage.close(); // 清理资源
                return null;
            }

            // 缓存
            textureCache.put(cacheKey, location);
            textureAccessTime.put(cacheKey, System.currentTimeMillis());

            Log.debug("Created texture: " + location + " (" + image.getWidth() + "x" + image.getHeight() + ")");
            return location;

        } catch (Exception e) {
            Log.error("Failed to create texture for: " + cacheKey, e);
            return null;
        }
    }

    /**
     * 清理最老的N个纹理
     */
    private void cleanupOldestTextures(int count) {
        textureAccessTime.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .limit(count)
            .forEach(entry -> {
                String key = entry.getKey();
                ResourceLocation location = textureCache.get(key);
                if (location != null) {
                    try {
                        mc.getTextureManager().release(location);
                        Log.debug("Released old texture: " + location);
                    } catch (Exception e) {
                        Log.error("Failed to release texture: " + location, e);
                    }
                }
                textureCache.remove(key);
                textureAccessTime.remove(key);
            });
    }

    /**
     * 转换BufferedImage到NativeImage
     * 处理ARGB到ABGR的颜色格式转换
     */
    @Nullable
    private NativeImage convertToNativeImage(@Nonnull BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            return null;
        }

        NativeImage nativeImage = null;
        try {
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            if (width <= 0 || height <= 0) {
                Log.error("Invalid image dimensions: " + width + "x" + height);
                return null;
            }

            // 创建NativeImage（RGBA格式）
            nativeImage = new NativeImage(width, height, true);

            // 复制像素数据并转换颜色格式
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = bufferedImage.getRGB(x, y);

                    // 提取ARGB分量
                    int alpha = (argb >> 24) & 0xFF;
                    int red = (argb >> 16) & 0xFF;
                    int green = (argb >> 8) & 0xFF;
                    int blue = argb & 0xFF;

                    // NativeImage使用ABGR格式
                    int abgr = (alpha << 24) | (blue << 16) | (green << 8) | red;
                    nativeImage.setPixelRGBA(x, y, abgr);
                }
            }

            return nativeImage;

        } catch (Exception e) {
            Log.error("Failed to convert BufferedImage to NativeImage", e);
            // 清理已分配的资源
            if (nativeImage != null) {
                try {
                    nativeImage.close();
                } catch (Exception closeEx) {
                    // 忽略关闭异常
                }
            }
            return null;
        }
    }

    /**
     * 渲染图片到告示牌
     * 应用所有变换：offset, rotation, size
     */
    private void renderImageToSign(
            @Nonnull PoseStack poseStack,
            @Nonnull MultiBufferSource bufferSource,
            @Nonnull ResourceLocation textureLocation,
            @Nonnull SizeData size,
            @Nonnull RotationData rotation,
            @Nonnull OffsetData offset,
            int packedLight,
            int packedOverlay) {

        if (poseStack == null || bufferSource == null || textureLocation == null) {
            return;
        }

        poseStack.pushPose();

        try {
            // === 1. 基础位置：告示牌表面中心 ===
            poseStack.translate(0.5, 0.5, 0.501);  // 稍微向前，避免z-fighting

            // === 2. 应用用户设置的偏移 ===
            if (offset != null) {
                poseStack.translate(offset.getX(), offset.getY(), offset.getZ());
            }

            // === 3. 应用用户设置的旋转 ===
            // 注意：旋转顺序很重要，通常是 Y -> X -> Z
            if (rotation != null) {
                if (rotation.getY() != 0) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(rotation.getY()));
                }
                if (rotation.getX() != 0) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(rotation.getX()));
                }
                if (rotation.getZ() != 0) {
                    poseStack.mulPose(Axis.ZP.rotationDegrees(rotation.getZ()));
                }
            }

            // === 4. 计算渲染大小 ===
            float baseSize = 0.4f;
            float width = baseSize * (size != null ? size.getWidth() : 1.0f);
            float height = baseSize * (size != null ? size.getHeight() : 1.0f);

            // === 5. 获取VertexConsumer ===
            VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(textureLocation)
            );

            if (consumer == null) {
                Log.error("Failed to get VertexConsumer");
                return;
            }

            Matrix4f matrix = poseStack.last().pose();

            // === 6. 绘制四边形 ===
            // 顶点顺序：左下 -> 右下 -> 右上 -> 左上（逆时针）
            // UV坐标：(0,0)左上 -> (1,1)右下

            // 左下角
            consumer.vertex(matrix, -width, -height, 0)
                    .color(255, 255, 255, 255)
                    .uv(0, 1)
                    .overlayCoords(packedOverlay)
                    .uv2(packedLight)
                    .normal(0, 0, 1)
                    .endVertex();

            // 右下角
            consumer.vertex(matrix, width, -height, 0)
                    .color(255, 255, 255, 255)
                    .uv(1, 1)
                    .overlayCoords(packedOverlay)
                    .uv2(packedLight)
                    .normal(0, 0, 1)
                    .endVertex();

            // 右上角
            consumer.vertex(matrix, width, height, 0)
                    .color(255, 255, 255, 255)
                    .uv(1, 0)
                    .overlayCoords(packedOverlay)
                    .uv2(packedLight)
                    .normal(0, 0, 1)
                    .endVertex();

            // 左上角
            consumer.vertex(matrix, -width, height, 0)
                    .color(255, 255, 255, 255)
                    .uv(0, 0)
                    .overlayCoords(packedOverlay)
                    .uv2(packedLight)
                    .normal(0, 0, 1)
                    .endVertex();

        } finally {
            // 确保popPose总是被调用
            poseStack.popPose();
        }
    }

    /**
     * 清理过期的纹理缓存
     * 线程安全，应该在客户端tick时定期调用
     */
    public void cleanupExpiredTextures() {
        try {
            long currentTime = System.currentTimeMillis();

            textureCache.keySet().removeIf(key -> {
                Long lastAccess = textureAccessTime.get(key);
                if (lastAccess == null || (currentTime - lastAccess) > TEXTURE_EXPIRE_TIME) {
                    ResourceLocation location = textureCache.get(key);
                    if (location != null) {
                        try {
                            mc.getTextureManager().release(location);
                            Log.debug("Released expired texture: " + location);
                        } catch (Exception e) {
                            Log.error("Failed to release texture: " + location, e);
                        }
                    }
                    textureAccessTime.remove(key);
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            Log.error("Exception in cleanupExpiredTextures()", e);
        }
    }

    /**
     * 清理所有纹理缓存
     * 线程安全，应该在退出世界时调用
     */
    public void clearAllTextures() {
        try {
            int count = textureCache.size();

            for (ResourceLocation location : textureCache.values()) {
                try {
                    mc.getTextureManager().release(location);
                } catch (Exception e) {
                    Log.error("Failed to release texture: " + location, e);
                }
            }

            textureCache.clear();
            textureAccessTime.clear();

            Log.info("Cleared all texture cache (" + count + " textures)");
        } catch (Exception e) {
            Log.error("Exception in clearAllTextures()", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public int getCachedTextureCount() {
        return textureCache.size();
    }

    /**
     * 获取缓存使用率
     */
    public float getCacheUsage() {
        return (float) textureCache.size() / MAX_CACHE_SIZE;
    }
}
