package cn.minerealms.signpicture.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * GUI基础类
 * 提供通用的GUI功能
 */
public abstract class BaseGuiScreen extends Screen {

    protected final Minecraft mc;
    @Nullable
    protected final Screen parentScreen;

    protected int guiLeft;
    protected int guiTop;
    protected int xSize = 176;
    protected int ySize = 166;

    public BaseGuiScreen(@Nonnull Component title, @Nullable Screen parentScreen) {
        super(title);
        this.mc = Minecraft.getInstance();
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.parentScreen != null) {
            this.mc.setScreen(this.parentScreen);
        } else {
            super.onClose();
        }
    }

    /**
     * 绘制背景
     */
    protected void renderBg(@Nonnull GuiGraphics guiGraphics) {
        this.renderBg(guiGraphics, 0, 0, 0);
    }

    /**
     * 绘制带颜色的背景
     */
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 半透明黑色背景
        guiGraphics.fill(0, 0, this.width, this.height, 0xC0101010);
    }

    /**
     * 绘制GUI背景框
     */
    protected void renderGuiBackground(@Nonnull GuiGraphics guiGraphics) {
        // 绘制GUI背景框
        guiGraphics.fill(this.guiLeft, this.guiTop,
                        this.guiLeft + this.xSize, this.guiTop + this.ySize,
                        0xFF8B8B8B);

        // 绘制内部背景
        guiGraphics.fill(this.guiLeft + 1, this.guiTop + 1,
                        this.guiLeft + this.xSize - 1, this.guiTop + this.ySize - 1,
                        0xFFC6C6C6);
    }

    /**
     * 绘制标题
     */
    protected void renderTitle(@Nonnull GuiGraphics guiGraphics, @Nonnull String title) {
        int titleWidth = this.font.width(title);
        int titleX = this.guiLeft + (this.xSize - titleWidth) / 2;
        int titleY = this.guiTop + 6;
        guiGraphics.drawString(this.font, title, titleX, titleY, 0x404040, false);
    }

    /**
     * 检查鼠标是否在指定区域内
     */
    protected boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /**
     * 绘制工具提示
     */
    protected void renderTooltip(@Nonnull GuiGraphics guiGraphics, @Nonnull String text, int mouseX, int mouseY) {
        guiGraphics.renderTooltip(this.font, Component.literal(text), mouseX, mouseY);
    }
}
