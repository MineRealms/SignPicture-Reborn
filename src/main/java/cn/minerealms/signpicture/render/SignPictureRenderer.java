package cn.minerealms.signpicture.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import javax.annotation.Nonnull;

/**
 * 自定义告示牌渲染器
 * 先渲染原版告示牌，然后在上面添加图片
 */
public class SignPictureRenderer implements BlockEntityRenderer<SignBlockEntity> {

    private final SignRenderer vanillaRenderer;

    public SignPictureRenderer(BlockEntityRendererProvider.Context context) {
        // 创建原版渲染器实例
        this.vanillaRenderer = new SignRenderer(context);
    }

    @Override
    public void render(@Nonnull SignBlockEntity blockEntity,
                      float partialTick,
                      @Nonnull PoseStack poseStack,
                      @Nonnull MultiBufferSource bufferSource,
                      int packedLight,
                      int packedOverlay) {
        // 先渲染原版告示牌（文本和木板）
        vanillaRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        // 然后在上面渲染图片（使用新的SignHandlerV2）
        SignHandlerV2.INSTANCE.render(blockEntity, poseStack, bufferSource, packedLight, packedOverlay);
    }

    @Override
    public int getViewDistance() {
        return 256; // 扩大渲染距离以支持大图片
    }
}
