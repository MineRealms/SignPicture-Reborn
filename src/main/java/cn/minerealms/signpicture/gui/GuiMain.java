package cn.minerealms.signpicture.gui;

import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.entry.Entry;
import cn.minerealms.signpicture.entry.EntryId;
import cn.minerealms.signpicture.entry.EntryManager;
import cn.minerealms.signpicture.entry.content.Content;
import cn.minerealms.signpicture.entry.content.ContentId;
import cn.minerealms.signpicture.render.ImageRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;

/**
 * 告示牌编辑主界面
 * 用于编辑告示牌上的图片URL和属性
 */
public class GuiMain extends BaseGuiScreen {

    @Nullable
    private final SignBlockEntity sign;
    @Nullable
    private EditBox urlField;
    @Nullable
    private Button doneButton;
    @Nullable
    private Button cancelButton;
    @Nullable
    private Button sizeButton;
    @Nullable
    private Button rotationButton;
    @Nullable
    private Button offsetButton;

    private String currentUrl = "";
    @Nullable
    private BufferedImage previewImage = null;

    public GuiMain(@Nullable Screen parentScreen, @Nullable SignBlockEntity sign) {
        super(Component.literal("SignPicture Editor"), parentScreen);
        this.sign = sign;
        this.xSize = 300;
        this.ySize = 200;
    }

    @Override
    protected void init() {
        super.init();

        // URL输入框
        this.urlField = new EditBox(this.font,
                this.guiLeft + 10, this.guiTop + 30,
                this.xSize - 20, 20,
                Component.literal("URL"));
        this.urlField.setMaxLength(500);
        this.urlField.setValue(this.currentUrl);
        this.urlField.setResponder(this::onUrlChanged);
        this.addRenderableWidget(this.urlField);

        // 完成按钮
        this.doneButton = Button.builder(Component.literal("Done"),
                button -> this.onDone())
                .bounds(this.guiLeft + 10, this.guiTop + this.ySize - 30, 60, 20)
                .build();
        this.addRenderableWidget(this.doneButton);

        // 取消按钮
        this.cancelButton = Button.builder(Component.literal("Cancel"),
                button -> this.onClose())
                .bounds(this.guiLeft + 80, this.guiTop + this.ySize - 30, 60, 20)
                .build();
        this.addRenderableWidget(this.cancelButton);

        // 大小调整按钮
        this.sizeButton = Button.builder(Component.literal("Size"),
                button -> this.openSizeGui())
                .bounds(this.guiLeft + 10, this.guiTop + 60, 80, 20)
                .build();
        this.addRenderableWidget(this.sizeButton);

        // 旋转调整按钮
        this.rotationButton = Button.builder(Component.literal("Rotation"),
                button -> this.openRotationGui())
                .bounds(this.guiLeft + 100, this.guiTop + 60, 80, 20)
                .build();
        this.addRenderableWidget(this.rotationButton);

        // 偏移调整按钮
        this.offsetButton = Button.builder(Component.literal("Offset"),
                button -> this.openOffsetGui())
                .bounds(this.guiLeft + 190, this.guiTop + 60, 80, 20)
                .build();
        this.addRenderableWidget(this.offsetButton);

        // 从告示牌加载URL
        if (this.sign != null) {
            loadUrlFromSign();
        }
    }

    private void onUrlChanged(@Nonnull String url) {
        this.currentUrl = url;
        loadPreview();
    }

    private void loadUrlFromSign() {
        if (this.sign == null) return;

        // 从告示牌文本提取URL
        var frontText = this.sign.getFrontText();
        StringBuilder url = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            Component line = frontText.getMessage(i, false);
            String text = line.getString().trim();
            if (!text.isEmpty()) {
                url.append(text);
            }
        }

        this.currentUrl = url.toString();
        if (this.urlField != null) {
            this.urlField.setValue(this.currentUrl);
        }
        loadPreview();
    }

    private void loadPreview() {
        if (this.currentUrl.isEmpty()) {
            this.previewImage = null;
            return;
        }

        // 获取图片内容
        EntryId entryId = EntryId.from("gui_preview");
        ContentId contentId = ContentId.from(this.currentUrl);
        Entry entry = EntryManager.instance.get(entryId, contentId);
        Content content = entry.getContent();

        if (content != null && content.isAvailable()) {
            this.previewImage = content.getImage();
        }
    }

    private void onDone() {
        if (this.sign != null && !this.currentUrl.isEmpty()) {
            // 将URL写入告示牌
            writeUrlToSign();
        }
        this.onClose();
    }

    private void writeUrlToSign() {
        if (this.sign == null) return;

        // 简单实现：将URL分成4行写入告示牌
        String[] lines = splitUrl(this.currentUrl, 4);

        // TODO: 使用网络包更新告示牌
        // 这里需要发送数据包到服务器
    }

    private String[] splitUrl(@Nonnull String url, int maxLines) {
        String[] lines = new String[maxLines];
        int maxLength = 15; // 每行最大字符数

        for (int i = 0; i < maxLines; i++) {
            int start = i * maxLength;
            if (start >= url.length()) {
                lines[i] = "";
            } else {
                int end = Math.min(start + maxLength, url.length());
                lines[i] = url.substring(start, end);
            }
        }

        return lines;
    }

    private void openSizeGui() {
        // TODO: 打开大小调整界面
        this.mc.setScreen(new GuiSize(this, this.currentUrl));
    }

    private void openRotationGui() {
        // TODO: 打开旋转调整界面
        this.mc.setScreen(new GuiRotation(this, this.currentUrl));
    }

    private void openOffsetGui() {
        // TODO: 打开偏移调整界面
        this.mc.setScreen(new GuiOffset(this, this.currentUrl));
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制GUI背景
        renderGuiBackground(guiGraphics);

        // 绘制标题
        renderTitle(guiGraphics, "SignPicture Editor");

        // 绘制URL标签
        guiGraphics.drawString(this.font, "Image URL:",
                this.guiLeft + 10, this.guiTop + 18, 0x404040, false);

        // 绘制预览
        if (this.previewImage != null) {
            int previewX = this.guiLeft + 10;
            int previewY = this.guiTop + 90;
            int maxWidth = this.xSize - 20;
            int maxHeight = 80;

            try {
                ImageRenderer.renderImage(guiGraphics, this.previewImage,
                        previewX, previewY, maxWidth, maxHeight);
            } catch (Exception e) {
                // 忽略渲染错误
            }
        } else if (!this.currentUrl.isEmpty()) {
            // 显示加载中
            guiGraphics.drawString(this.font, "Loading preview...",
                    this.guiLeft + 10, this.guiTop + 90, 0x808080, false);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.urlField != null && this.urlField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.urlField != null && this.urlField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    // ========== 属性设置方法（占位符，GuiMain不保存属性）==========

    public void setSize(float width, float height) {
        // GuiMain是简化版，不保存属性
        // 属性只在GuiMainFull中保存
    }

    public void setRotation(float x, float y, float z) {
        // GuiMain是简化版，不保存属性
    }

    public void setOffset(float x, float y, float z) {
        // GuiMain是简化版，不保存属性
    }
}
