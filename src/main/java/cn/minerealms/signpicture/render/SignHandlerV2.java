package cn.minerealms.signpicture.render;

import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.data.SignPictureData;
import cn.minerealms.signpicture.data.SignPictureDataManagerClient;
import cn.minerealms.signpicture.data.SignPictureHelper;
import cn.minerealms.signpicture.entry.Entry;
import cn.minerealms.signpicture.entry.EntryId;
import cn.minerealms.signpicture.entry.EntryManager;
import cn.minerealms.signpicture.entry.content.Content;
import cn.minerealms.signpicture.entry.content.ContentId;
import cn.minerealms.signpicture.network.NetworkHandler;
import cn.minerealms.signpicture.network.RequestSignPicturePacket;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SignPicture渲染处理器 - UUID+NBT架构
 *
 * 渲染流程：
 * 1. 检查告示牌是否是SignPicture
 * 2. 提取UUID
 * 3. 从客户端DataManager获取元数据
 * 4. 使用ContentManager下载/加载图片
 * 5. 应用属性渲染
 */
@OnlyIn(Dist.CLIENT)
public class SignHandlerV2 {
    public static final SignHandlerV2 INSTANCE = new SignHandlerV2();
    private final Minecraft mc = Minecraft.getInstance();

    // 纹理缓存：UUID -> ResourceLocation
    private final Map<String, ResourceLocation> textureCache = new ConcurrentHashMap<>();
    private final Map<String, Long> textureAccessTime = new ConcurrentHashMap<>();

    // Entry缓存：UUID -> Entry（性能优化）
    private final Map<String, Entry> entryCache = new ConcurrentHashMap<>();

    // 请求追踪：避免重复请求
    private final Map<String, Long> requestedUUIDs = new ConcurrentHashMap<>();
    private static final long REQUEST_COOLDOWN = 5000; // 5秒冷却

    // 配置常量
    private static final long TEXTURE_EXPIRE_TIME = 5 * 60 * 1000;
    private static final int MAX_TEXTURE_SIZE = 4096;
    private static final int MAX_CACHE_SIZE = 100;

    private SignHandlerV2() {
    }

    /**
     * 主渲染方法
     */
    public void render(@Nonnull SignBlockEntity sign,
                       @Nonnull PoseStack poseStack,
                       @Nonnull MultiBufferSource bufferSource,
                       int packedLight,
                       int packedOverlay) {

        if (sign == null || poseStack == null || bufferSource == null) {
            return;
        }

        try {
            renderInternal(sign, poseStack, bufferSource, packedLight, packedOverlay);
        } catch (Exception e) {
            Log.error("Exception in SignHandlerV2.render()", e);
        }
    }

    private void renderInternal(@Nonnull SignBlockEntity sign,
                                @Nonnull PoseStack poseStack,
                                @Nonnull MultiBufferSource bufferSource,
                                int packedLight,
                                int packedOverlay) {

        // 1. 检查是否是SignPicture
        if (!SignPictureHelper.isSignPicture(sign)) {
            return;
        }

        // 2. 提取UUID
        String uuid = SignPictureHelper.getUUID(sign);
        if (uuid == null || uuid.isEmpty()) {
            return;
        }

        // 3. 获取元数据（从客户端缓存）
        SignPictureData data = SignPictureDataManagerClient.INSTANCE.getMetadata(uuid);
        if (data == null) {
            // 请求服务端
            requestFromServer(uuid);
            return;
        }

        // 4. 获取图片内容（使用缓存的Entry）
        String url = data.getUrl();
        Entry entry = entryCache.get(uuid);

        // 如果缓存中没有，创建新的Entry
        if (entry == null) {
            EntryId entryId = EntryId.from("signpic_" + uuid);
            ContentId contentId = ContentId.from(url);
            entry = EntryManager.instance.get(entryId, contentId);
            entryCache.put(uuid, entry);
        }

        Content content = entry.getContent();
        if (!content.isAvailable()) {
            return; // 还在下载中
        }

        // 5. 获取当前帧图片
        BufferedImage image = null;
        try {
            if (content.isAnimated()) {
                int currentFrame = entry.getCurrentFrame();
                int frameCount = content.getFrameCount();
                if (currentFrame >= 0 && currentFrame < frameCount) {
                    image = content.getFrame(currentFrame);
                } else {
                    image = content.getFrame(0);
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

        // 6. 验证图片尺寸
        if (!validateImageSize(image)) {
            Log.error("Image size exceeds maximum: " + image.getWidth() + "x" + image.getHeight());
            return;
        }

        // 7. 获取或创建纹理
        ResourceLocation textureLocation = getOrCreateTexture(uuid, image);
        if (textureLocation == null) {
            return;
        }

        // 8. 渲染
        try {
            renderImageToSign(poseStack, bufferSource, textureLocation,
                             data, packedLight, packedOverlay);
        } catch (Exception e) {
            Log.error("Failed to render image to sign", e);
        }
    }

    /**
     * 请求服务端数据
     */
    private void requestFromServer(@Nonnull String uuid) {
        long currentTime = System.currentTimeMillis();
        Long lastRequest = requestedUUIDs.get(uuid);

        // 检查冷却时间
        if (lastRequest != null && (currentTime - lastRequest) < REQUEST_COOLDOWN) {
            return; // 还在冷却中
        }

        // 发送请求
        RequestSignPicturePacket packet = new RequestSignPicturePacket(uuid);
        NetworkHandler.sendToServer(packet);

        requestedUUIDs.put(uuid, currentTime);
        Log.info("[Client] Requested SignPicture data: " + uuid);
    }

    private boolean validateImageSize(@Nonnull BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        return width > 0 && height > 0 &&
               width <= MAX_TEXTURE_SIZE && height <= MAX_TEXTURE_SIZE;
    }

    @Nullable
    private ResourceLocation getOrCreateTexture(@Nonnull String uuid, @Nonnull BufferedImage image) {
        if (uuid == null || uuid.isEmpty() || image == null) {
            return null;
        }

        // 检查缓存
        if (textureCache.containsKey(uuid)) {
            textureAccessTime.put(uuid, System.currentTimeMillis());
            return textureCache.get(uuid);
        }

        // 检查缓存大小限制
        if (textureCache.size() >= MAX_CACHE_SIZE) {
            cleanupOldestTextures(10);
        }

        try {
            NativeImage nativeImage = convertToNativeImage(image);
            if (nativeImage == null) {
                return null;
            }

            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);

            ResourceLocation location = mc.getTextureManager().register(
                "signpicture/dynamic/" + uuid,
                dynamicTexture
            );

            if (location == null) {
                nativeImage.close();
                return null;
            }

            textureCache.put(uuid, location);
            textureAccessTime.put(uuid, System.currentTimeMillis());

            Log.info("[Client] Created texture for UUID: " + uuid +
                     " (" + image.getWidth() + "x" + image.getHeight() + ")");
            return location;

        } catch (Exception e) {
            Log.error("Failed to create texture for UUID: " + uuid, e);
            return null;
        }
    }

    private void cleanupOldestTextures(int count) {
        textureAccessTime.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .limit(count)
            .forEach(entry -> {
                String uuid = entry.getKey();
                releaseTexture(uuid);
            });
    }

    /**
     * 释放纹理
     */
    public void releaseTexture(@Nonnull String uuid) {
        ResourceLocation location = textureCache.get(uuid);
        if (location != null) {
            try {
                mc.getTextureManager().release(location);
            } catch (Exception e) {
                Log.error("Failed to release texture: " + location, e);
            }
        }
        textureCache.remove(uuid);
        textureAccessTime.remove(uuid);
    }

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
                return null;
            }

            nativeImage = new NativeImage(width, height, true);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = bufferedImage.getRGB(x, y);
                    int alpha = (argb >> 24) & 0xFF;
                    int red = (argb >> 16) & 0xFF;
                    int green = (argb >> 8) & 0xFF;
                    int blue = argb & 0xFF;
                    int abgr = (alpha << 24) | (blue << 16) | (green << 8) | red;
                    nativeImage.setPixelRGBA(x, y, abgr);
                }
            }

            return nativeImage;

        } catch (Exception e) {
            Log.error("Failed to convert BufferedImage to NativeImage", e);
            if (nativeImage != null) {
                try {
                    nativeImage.close();
                } catch (Exception closeEx) {
                    // 忽略
                }
            }
            return null;
        }
    }

    private void renderImageToSign(
            @Nonnull PoseStack poseStack,
            @Nonnull MultiBufferSource bufferSource,
            @Nonnull ResourceLocation textureLocation,
            @Nonnull SignPictureData data,
            int packedLight,
            int packedOverlay) {

        if (poseStack == null || bufferSource == null || textureLocation == null || data == null) {
            return;
        }

        poseStack.pushPose();

        try {
            // 1. 基础位置
            poseStack.translate(0.5, 0.5, 0.501);

            // 2. 应用偏移
            poseStack.translate(data.getOffsetX(), data.getOffsetY(), data.getOffsetZ());

            // 3. 应用旋转
            if (data.getRotationY() != 0) {
                poseStack.mulPose(Axis.YP.rotationDegrees(data.getRotationY()));
            }
            if (data.getRotationX() != 0) {
                poseStack.mulPose(Axis.XP.rotationDegrees(data.getRotationX()));
            }
            if (data.getRotationZ() != 0) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(data.getRotationZ()));
            }

            // 4. 计算渲染大小
            float baseSize = 0.4f;
            float width = baseSize * data.getSizeWidth();
            float height = baseSize * data.getSizeHeight();

            // 5. 获取VertexConsumer
            VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(textureLocation)
            );

            if (consumer == null) {
                return;
            }

            Matrix4f matrix = poseStack.last().pose();

            // 6. 绘制四边形
            consumer.vertex(matrix, -width, -height, 0)
                    .color(255, 255, 255, 255)
                    .uv(0, 1)
                    .overlayCoords(packedOverlay)
                    .uv2(packedLight)
                    .normal(0, 0, 1)
                    .endVertex();

            consumer.vertex(matrix, width, -height, 0)
                    .color(255, 255, 255, 255)
                    .uv(1, 1)
                    .overlayCoords(packedOverlay)
                    .uv2(packedLight)
                    .normal(0, 0, 1)
                    .endVertex();

            consumer.vertex(matrix, width, height, 0)
                    .color(255, 255, 255, 255)
                    .uv(1, 0)
                    .overlayCoords(packedOverlay)
                    .uv2(packedLight)
                    .normal(0, 0, 1)
                    .endVertex();

            consumer.vertex(matrix, -width, height, 0)
                    .color(255, 255, 255, 255)
                    .uv(0, 0)
                    .overlayCoords(packedOverlay)
                    .uv2(packedLight)
                    .normal(0, 0, 1)
                    .endVertex();

        } finally {
            poseStack.popPose();
        }
    }

    /**
     * 清理过期纹理和Entry缓存
     */
    public void cleanupExpiredTextures() {
        try {
            long currentTime = System.currentTimeMillis();

            textureCache.keySet().removeIf(uuid -> {
                Long lastAccess = textureAccessTime.get(uuid);
                if (lastAccess == null || (currentTime - lastAccess) > TEXTURE_EXPIRE_TIME) {
                    releaseTexture(uuid);
                    // 同时清理Entry缓存
                    entryCache.remove(uuid);
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            Log.error("Exception in cleanupExpiredTextures()", e);
        }
    }

    /**
     * 清理所有纹理和Entry缓存
     */
    public void clearAllTextures() {
        try {
            int count = textureCache.size();

            for (String uuid : textureCache.keySet()) {
                releaseTexture(uuid);
            }

            textureCache.clear();
            textureAccessTime.clear();
            entryCache.clear(); // 清理Entry缓存

            Log.info("Cleared all texture cache (" + count + " textures)");
        } catch (Exception e) {
            Log.error("Exception in clearAllTextures()", e);
        }
    }

    public int getCachedTextureCount() {
        return textureCache.size();
    }
}
