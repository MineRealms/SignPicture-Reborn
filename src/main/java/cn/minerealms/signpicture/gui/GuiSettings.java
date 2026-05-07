package cn.minerealms.signpicture.gui;

import cn.minerealms.signpicture.Config;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 配置界面
 * 允许用户在游戏内修改mod配置
 */
public class GuiSettings extends BaseGuiScreen {

    @Nullable
    private Button doneButton;
    @Nullable
    private Button cancelButton;
    @Nullable
    private CycleButton<Boolean> chatPicButton;
    @Nullable
    private CycleButton<Boolean> animateGifButton;
    @Nullable
    private CycleButton<Boolean> debugLogButton;

    public GuiSettings(@Nullable Screen parentScreen) {
        super(Component.literal("SignPicture Settings"), parentScreen);
        this.xSize = 300;
        this.ySize = 200;
    }

    @Override
    protected void init() {
        super.init();

        int buttonY = this.guiTop + 40;
        int buttonSpacing = 30;

        // 聊天图片开关
        this.chatPicButton = CycleButton.booleanBuilder(
                Component.literal("ON"),
                Component.literal("OFF"))
                .withInitialValue(Config.CLIENT.chatpicEnable.get())
                .create(this.guiLeft + 10, buttonY,
                        this.xSize - 20, 20,
                        Component.literal("Chat Picture: "),
                        (button, value) -> Config.CLIENT.chatpicEnable.set(value));
        this.addRenderableWidget(this.chatPicButton);
        buttonY += buttonSpacing;

        // GIF动画开关
        this.animateGifButton = CycleButton.booleanBuilder(
                Component.literal("ON"),
                Component.literal("OFF"))
                .withInitialValue(Config.COMMON.imageAnimationGif.get())
                .create(this.guiLeft + 10, buttonY,
                        this.xSize - 20, 20,
                        Component.literal("Animate GIF: "),
                        (button, value) -> Config.COMMON.imageAnimationGif.set(value));
        this.addRenderableWidget(this.animateGifButton);
        buttonY += buttonSpacing;

        // 调试日志开关
        this.debugLogButton = CycleButton.booleanBuilder(
                Component.literal("ON"),
                Component.literal("OFF"))
                .withInitialValue(Config.CLIENT.debugLog.get())
                .create(this.guiLeft + 10, buttonY,
                        this.xSize - 20, 20,
                        Component.literal("Debug Log: "),
                        (button, value) -> Config.CLIENT.debugLog.set(value));
        this.addRenderableWidget(this.debugLogButton);

        // 完成按钮
        this.doneButton = Button.builder(Component.literal("Done"),
                button -> this.onDone())
                .bounds(this.guiLeft + 10, this.guiTop + this.ySize - 30, 80, 20)
                .build();
        this.addRenderableWidget(this.doneButton);

        // 取消按钮
        this.cancelButton = Button.builder(Component.literal("Cancel"),
                button -> this.onClose())
                .bounds(this.guiLeft + 100, this.guiTop + this.ySize - 30, 80, 20)
                .build();
        this.addRenderableWidget(this.cancelButton);
    }

    private void onDone() {
        // 保存配置
        Config.CLIENT_SPEC.save();
        Config.COMMON_SPEC.save();
        this.onClose();
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制GUI背景
        renderGuiBackground(guiGraphics);

        // 绘制标题
        renderTitle(guiGraphics, "SignPicture Settings");

        // 绘制说明文本
        guiGraphics.drawString(this.font, "Configure SignPicture options:",
                this.guiLeft + 10, this.guiTop + 20, 0x404040, false);
    }
}
