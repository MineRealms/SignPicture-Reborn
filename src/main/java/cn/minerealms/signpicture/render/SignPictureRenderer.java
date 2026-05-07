package cn.minerealms.signpicture.render;

import cn.minerealms.signpicture.render.SignHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import javax.annotation.Nonnull;

/**
 * 自定义告示牌渲染器
 * 在原版渲染后添加图片渲染
 */
public class SignPictureRenderer implements BlockEntityRenderer<SignBlockEntity> {
    
    public SignPictureRenderer(BlockEntityRendererProvider.Context context) {
        // 初始化
    }
    
    @Override
    public void render(@Nonnull SignBlockEntity blockEntity,
                      float partialTick,
                      @Nonnull PoseStack poseStack,
                      @Nonnull MultiBufferSource bufferSource,
                      int packedLight,
                      int packedOverlay) {
        // 调用SignHandler渲染图片
        SignHandler.instance.render(blockEntity, poseStack, bufferSource, packedLight, packedOverlay);
    }
    
    @Override
    public int getViewDistance() {
        return 256; // 扩大渲染距离以支持大图片
    }
}
