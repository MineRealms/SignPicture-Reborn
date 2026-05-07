package cn.minerealms.signpicture.mixin;

import cn.minerealms.signpicture.gui.GuiMainFull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 告示牌编辑屏幕Mixin
 * 在打开告示牌编辑时自动打开SignPicture GUI
 */
@Mixin(AbstractSignEditScreen.class)
public class SignEditScreenMixin {

    @Shadow @Final
    private SignBlockEntity sign;

    private boolean signPictureGuiOpened = false;

    /**
     * 在告示牌编辑屏幕初始化时注入
     */
    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // 只打开一次
        if (!signPictureGuiOpened) {
            signPictureGuiOpened = true;

            // 延迟打开GUI，避免与原版GUI冲突
            Minecraft mc = Minecraft.getInstance();
            mc.tell(() -> {
                // 关闭原版告示牌编辑界面
                mc.setScreen(null);
                // 打开SignPicture GUI
                mc.setScreen(new GuiMainFull(null, this.sign));
            });
        }
    }
}
