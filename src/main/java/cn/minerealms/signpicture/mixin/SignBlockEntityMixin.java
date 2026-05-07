package cn.minerealms.signpicture.mixin;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 告示牌方块实体Mixin
 * 扩展告示牌的渲染边界，使其可以渲染更大的图片
 */
@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin {
    
    /**
     * 注入getRenderBoundingBox方法
     * 返回无限边界以允许渲染大图片
     */
    @Inject(method = "getRenderBoundingBox()Lnet/minecraft/world/phys/AABB;", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetRenderBoundingBox(CallbackInfoReturnable<AABB> cir) {
        // 返回无限边界，允许告示牌渲染超出正常范围的图片
        cir.setReturnValue(new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                                     Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
    }
}
