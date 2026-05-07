package cn.minerealms.signpicture.gui;

import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.attr.prop.OffsetData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 偏移调整界面
 */
public class GuiOffset extends BaseGuiScreen {

    @Nullable
    private final String imageUrl;
    @Nullable
    private EditBox xField;
    @Nullable
    private EditBox yField;
    @Nullable
    private EditBox zField;
    @Nullable
    private Button doneButton;
    @Nullable
    private Button cancelButton;
    @Nullable
    private Button resetButton;

    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float offsetZ = 0.0f;

    public GuiOffset(@Nullable Screen parentScreen, @Nullable String imageUrl) {
        super(Component.literal("Offset Adjustment"), parentScreen);
        this.imageUrl = imageUrl;
        this.xSize = 200;
        this.ySize = 180;
    }

    @Override
    protected void init() {
        super.init();

        // X轴偏移输入框
        this.xField = new EditBox(this.font,
                this.guiLeft + 70, this.guiTop + 40,
                100, 20,
                Component.literal("X Offset"));
        this.xField.setValue(String.valueOf(this.offsetX));
        this.xField.setResponder(this::onXChanged);
        this.addRenderableWidget(this.xField);

        // Y轴偏移输入框
        this.yField = new EditBox(this.font,
                this.guiLeft + 70, this.guiTop + 70,
                100, 20,
                Component.literal("Y Offset"));
        this.yField.setValue(String.valueOf(this.offsetY));
        this.yField.setResponder(this::onYChanged);
        this.addRenderableWidget(this.yField);

        // Z轴偏移输入框
        this.zField = new EditBox(this.font,
                this.guiLeft + 70, this.guiTop + 100,
                100, 20,
                Component.literal("Z Offset"));
        this.zField.setValue(String.valueOf(this.offsetZ));
        this.zField.setResponder(this::onZChanged);
        this.addRenderableWidget(this.zField);

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

    private void onXChanged(@Nonnull String value) {
        try {
            this.offsetX = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // 忽略无效输入
        }
    }

    private void onYChanged(@Nonnull String value) {
        try {
            this.offsetY = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // 忽略无效输入
        }
    }

    private void onZChanged(@Nonnull String value) {
        try {
            this.offsetZ = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // 忽略无效输入
        }
    }

    private void onDone() {
        // 保存偏移设置到父GUI
        if (this.parentScreen instanceof GuiMainFull) {
            GuiMainFull parent = (GuiMainFull) this.parentScreen;
            parent.setOffset(this.offsetX, this.offsetY, this.offsetZ);
            Log.debug("Offset saved: " + this.offsetX + ", " + this.offsetY + ", " + this.offsetZ);
        } else if (this.parentScreen instanceof GuiMain) {
            GuiMain parent = (GuiMain) this.parentScreen;
            parent.setOffset(this.offsetX, this.offsetY, this.offsetZ);
            Log.debug("Offset saved: " + this.offsetX + ", " + this.offsetY + ", " + this.offsetZ);
        }
        this.onClose();
    }

    private void onReset() {
        this.offsetX = 0.0f;
        this.offsetY = 0.0f;
        this.offsetZ = 0.0f;
        if (this.xField != null) {
            this.xField.setValue("0.0");
        }
        if (this.yField != null) {
            this.yField.setValue("0.0");
        }
        if (this.zField != null) {
            this.zField.setValue("0.0");
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制GUI背景
        renderGuiBackground(guiGraphics);

        // 绘制标题
        renderTitle(guiGraphics, "Offset Adjustment");

        // 绘制标签
        guiGraphics.drawString(this.font, "X Offset:",
                this.guiLeft + 10, this.guiTop + 45, 0x404040, false);
        guiGraphics.drawString(this.font, "Y Offset:",
                this.guiLeft + 10, this.guiTop + 75, 0x404040, false);
        guiGraphics.drawString(this.font, "Z Offset:",
                this.guiLeft + 10, this.guiTop + 105, 0x404040, false);
    }

    public float getOffsetX() {
        return this.offsetX;
    }

    public float getOffsetY() {
        return this.offsetY;
    }

    public float getOffsetZ() {
        return this.offsetZ;
    }
}
