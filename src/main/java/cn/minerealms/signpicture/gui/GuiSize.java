package cn.minerealms.signpicture.gui;

import cn.minerealms.signpicture.attr.prop.SizeData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 大小调整界面
 */
public class GuiSize extends BaseGuiScreen {

    @Nullable
    private final String imageUrl;
    @Nullable
    private EditBox widthField;
    @Nullable
    private EditBox heightField;
    @Nullable
    private Button doneButton;
    @Nullable
    private Button cancelButton;
    @Nullable
    private Button resetButton;

    private float width = 1.0f;
    private float height = 1.0f;

    public GuiSize(@Nullable Screen parentScreen, @Nullable String imageUrl) {
        super(Component.literal("Size Adjustment"), parentScreen);
        this.imageUrl = imageUrl;
        this.xSize = 200;
        this.ySize = 150;
    }

    @Override
    protected void init() {
        super.init();

        // 宽度输入框
        this.widthField = new EditBox(this.font,
                this.guiLeft + 70, this.guiTop + 40,
                100, 20,
                Component.literal("Width"));
        this.widthField.setValue(String.valueOf(this.width));
        this.widthField.setResponder(this::onWidthChanged);
        this.addRenderableWidget(this.widthField);

        // 高度输入框
        this.heightField = new EditBox(this.font,
                this.guiLeft + 70, this.guiTop + 70,
                100, 20,
                Component.literal("Height"));
        this.heightField.setValue(String.valueOf(this.height));
        this.heightField.setResponder(this::onHeightChanged);
        this.addRenderableWidget(this.heightField);

        // 完成按钮
        this.doneButton = Button.builder(Component.literal("Done"),
                button -> this.onDone())
                .bounds(this.guiLeft + 10, this.guiTop + this.ySize - 30, 50, 20)
                .build();
        this.addRenderableWidget(this.doneButton);

        // 取消按钮
        this.cancelButton = Button.builder(Component.literal("Cancel"),
                button -> this.onClose())
                .bounds(this.guiLeft + 70, this.guiTop + this.ySize - 30, 50, 20)
                .build();
        this.addRenderableWidget(this.cancelButton);

        // 重置按钮
        this.resetButton = Button.builder(Component.literal("Reset"),
                button -> this.onReset())
                .bounds(this.guiLeft + 130, this.guiTop + this.ySize - 30, 50, 20)
                .build();
        this.addRenderableWidget(this.resetButton);
    }

    private void onWidthChanged(@Nonnull String value) {
        try {
            this.width = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // 忽略无效输入
        }
    }

    private void onHeightChanged(@Nonnull String value) {
        try {
            this.height = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // 忽略无效输入
        }
    }

    private void onDone() {
        // TODO: 保存大小设置
        this.onClose();
    }

    private void onReset() {
        this.width = 1.0f;
        this.height = 1.0f;
        if (this.widthField != null) {
            this.widthField.setValue("1.0");
        }
        if (this.heightField != null) {
            this.heightField.setValue("1.0");
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制GUI背景
        renderGuiBackground(guiGraphics);

        // 绘制标题
        renderTitle(guiGraphics, "Size Adjustment");

        // 绘制标签
        guiGraphics.drawString(this.font, "Width:",
                this.guiLeft + 10, this.guiTop + 45, 0x404040, false);
        guiGraphics.drawString(this.font, "Height:",
                this.guiLeft + 10, this.guiTop + 75, 0x404040, false);
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }
}
