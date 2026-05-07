package cn.minerealms.signpicture.gui;

import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.attr.prop.RotationData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 旋转调整界面
 */
public class GuiRotation extends BaseGuiScreen {

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

    private float rotationX = 0.0f;
    private float rotationY = 0.0f;
    private float rotationZ = 0.0f;

    public GuiRotation(@Nullable Screen parentScreen, @Nullable String imageUrl) {
        super(Component.literal("Rotation Adjustment"), parentScreen);
        this.imageUrl = imageUrl;
        this.xSize = 200;
        this.ySize = 180;
    }

    @Override
    protected void init() {
        super.init();

        // X轴旋转输入框
        this.xField = new EditBox(this.font,
                this.guiLeft + 70, this.guiTop + 40,
                100, 20,
                Component.literal("X Rotation"));
        this.xField.setValue(String.valueOf(this.rotationX));
        this.xField.setResponder(this::onXChanged);
        this.addRenderableWidget(this.xField);

        // Y轴旋转输入框
        this.yField = new EditBox(this.font,
                this.guiLeft + 70, this.guiTop + 70,
                100, 20,
                Component.literal("Y Rotation"));
        this.yField.setValue(String.valueOf(this.rotationY));
        this.yField.setResponder(this::onYChanged);
        this.addRenderableWidget(this.yField);

        // Z轴旋转输入框
        this.zField = new EditBox(this.font,
                this.guiLeft + 70, this.guiTop + 100,
                100, 20,
                Component.literal("Z Rotation"));
        this.zField.setValue(String.valueOf(this.rotationZ));
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
            this.rotationX = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // 忽略无效输入
        }
    }

    private void onYChanged(@Nonnull String value) {
        try {
            this.rotationY = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // 忽略无效输入
        }
    }

    private void onZChanged(@Nonnull String value) {
        try {
            this.rotationZ = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // 忽略无效输入
        }
    }

    private void onDone() {
        // 保存旋转设置到父GUI
        if (this.parentScreen instanceof GuiMainFull) {
            GuiMainFull parent = (GuiMainFull) this.parentScreen;
            parent.setRotation(this.rotationX, this.rotationY, this.rotationZ);
            Log.debug("Rotation saved: " + this.rotationX + ", " + this.rotationY + ", " + this.rotationZ);
        } else if (this.parentScreen instanceof GuiMain) {
            GuiMain parent = (GuiMain) this.parentScreen;
            parent.setRotation(this.rotationX, this.rotationY, this.rotationZ);
            Log.debug("Rotation saved: " + this.rotationX + ", " + this.rotationY + ", " + this.rotationZ);
        }
        this.onClose();
    }

    private void onReset() {
        this.rotationX = 0.0f;
        this.rotationY = 0.0f;
        this.rotationZ = 0.0f;
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
        renderTitle(guiGraphics, "Rotation Adjustment");

        // 绘制标签
        guiGraphics.drawString(this.font, "X Axis:",
                this.guiLeft + 10, this.guiTop + 45, 0x404040, false);
        guiGraphics.drawString(this.font, "Y Axis:",
                this.guiLeft + 10, this.guiTop + 75, 0x404040, false);
        guiGraphics.drawString(this.font, "Z Axis:",
                this.guiLeft + 10, this.guiTop + 105, 0x404040, false);
    }

    public float getRotationX() {
        return this.rotationX;
    }

    public float getRotationY() {
        return this.rotationY;
    }

    public float getRotationZ() {
        return this.rotationZ;
    }
}
