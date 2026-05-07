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
import java.io.File;

/**
 * 告示牌编辑主界面 - 完整版
 * 提供完整的图片编辑功能，类似1.12.2版本
 */
public class GuiMainFull extends BaseGuiScreen {

    @Nullable
    private final SignBlockEntity sign;

    // 输入框
    @Nullable
    private EditBox urlField;

    // 主要按钮
    @Nullable
    private Button doneButton;
    @Nullable
    private Button cancelButton;
    @Nullable
    private Button applyButton;

    // 属性调整按钮
    @Nullable
    private Button sizeButton;
    @Nullable
    private Button rotationButton;
    @Nullable
    private Button offsetButton;

    // 功能按钮
    @Nullable
    private Button screenshotButton;
    @Nullable
    private Button uploadButton;
    @Nullable
    private Button shortenButton;
    @Nullable
    private Button settingsButton;
    @Nullable
    private Button clearButton;
    @Nullable
    private Button pasteButton;

    private String currentUrl = "";
    @Nullable
    private BufferedImage previewImage = null;
    private boolean showPreview = true;

    public GuiMainFull(@Nullable Screen parentScreen, @Nullable SignBlockEntity sign) {
        super(Component.literal("SignPicture Editor"), parentScreen);
        this.sign = sign;
        this.xSize = 400;
        this.ySize = 300;
    }

    @Override
    protected void init() {
        super.init();

        int leftPanel = this.guiLeft + 10;
        int rightPanel = this.guiLeft + this.xSize - 90;
        int buttonWidth = 80;
        int buttonHeight = 20;
        int spacing = 25;

        // URL输入框
        this.urlField = new EditBox(this.font,
                leftPanel, this.guiTop + 30,
                this.xSize - 120, 20,
                Component.literal("URL"));
        this.urlField.setMaxLength(1000);
        this.urlField.setValue(this.currentUrl);
        this.urlField.setResponder(this::onUrlChanged);
        this.addRenderableWidget(this.urlField);

        // 右侧功能按钮列
        int buttonY = this.guiTop + 30;

        // 截图按钮
        this.screenshotButton = Button.builder(Component.literal("Screenshot"),
                button -> this.onScreenshot())
                .bounds(rightPanel, buttonY, buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(this.screenshotButton);
        buttonY += spacing;

        // 上传按钮
        this.uploadButton = Button.builder(Component.literal("Upload"),
                button -> this.onUpload())
                .bounds(rightPanel, buttonY, buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(this.uploadButton);
        buttonY += spacing;

        // URL缩短按钮
        this.shortenButton = Button.builder(Component.literal("Shorten URL"),
                button -> this.onShortenUrl())
                .bounds(rightPanel, buttonY, buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(this.shortenButton);
        buttonY += spacing;

        // 设置按钮
        this.settingsButton = Button.builder(Component.literal("Settings"),
                button -> this.onSettings())
                .bounds(rightPanel, buttonY, buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(this.settingsButton);
        buttonY += spacing;

        // 清除按钮
        this.clearButton = Button.builder(Component.literal("Clear"),
                button -> this.onClear())
                .bounds(rightPanel, buttonY, buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(this.clearButton);
        buttonY += spacing;

        // 粘贴按钮
        this.pasteButton = Button.builder(Component.literal("Paste"),
                button -> this.onPaste())
                .bounds(rightPanel, buttonY, buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(this.pasteButton);

        // 底部属性调整按钮
        int bottomY = this.guiTop + this.ySize - 60;

        // 大小调整按钮
        this.sizeButton = Button.builder(Component.literal("Size"),
                button -> this.openSizeGui())
                .bounds(leftPanel, bottomY, 80, buttonHeight)
                .build();
        this.addRenderableWidget(this.sizeButton);

        // 旋转调整按钮
        this.rotationButton = Button.builder(Component.literal("Rotation"),
                button -> this.openRotationGui())
                .bounds(leftPanel + 90, bottomY, 80, buttonHeight)
                .build();
        this.addRenderableWidget(this.rotationButton);

        // 偏移调整按钮
        this.offsetButton = Button.builder(Component.literal("Offset"),
                button -> this.openOffsetGui())
                .bounds(leftPanel + 180, bottomY, 80, buttonHeight)
                .build();
        this.addRenderableWidget(this.offsetButton);

        // 底部主要按钮
        bottomY = this.guiTop + this.ySize - 30;

        // 应用按钮
        this.applyButton = Button.builder(Component.literal("Apply"),
                button -> this.onApply())
                .bounds(leftPanel, bottomY, 60, buttonHeight)
                .build();
        this.addRenderableWidget(this.applyButton);

        // 完成按钮
        this.doneButton = Button.builder(Component.literal("Done"),
                button -> this.onDone())
                .bounds(leftPanel + 70, bottomY, 60, buttonHeight)
                .build();
        this.addRenderableWidget(this.doneButton);

        // 取消按钮
        this.cancelButton = Button.builder(Component.literal("Cancel"),
                button -> this.onClose())
                .bounds(leftPanel + 140, bottomY, 60, buttonHeight)
                .build();
        this.addRenderableWidget(this.cancelButton);

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

        EntryId entryId = EntryId.from("gui_preview");
        ContentId contentId = ContentId.from(this.currentUrl);
        Entry entry = EntryManager.instance.get(entryId, contentId);
        Content content = entry.getContent();

        if (content != null && content.isAvailable()) {
            this.previewImage = content.getImage();
        }
    }

    private void onScreenshot() {
        // TODO: 打开截图界面
        try {
            File screenshotDir = new File(this.mc.gameDirectory, "screenshots");
            File screenshot = ScreenshotUtil.takeAndSaveScreenshot(screenshotDir);
            // 可以选择自动上传截图
        } catch (Exception e) {
            // 显示错误消息
        }
    }

    private void onUpload() {
        // TODO: 打开上传界面
        this.mc.setScreen(new GuiUpload(this));
    }

    private void onShortenUrl() {
        // TODO: 缩短当前URL
        if (!this.currentUrl.isEmpty()) {
            // 调用URL缩短服务
        }
    }

    private void onSettings() {
        this.mc.setScreen(new GuiSettings(this));
    }

    private void onClear() {
        this.currentUrl = "";
        if (this.urlField != null) {
            this.urlField.setValue("");
        }
        this.previewImage = null;
    }

    private void onPaste() {
        // 从剪贴板粘贴
        try {
            String clipboard = this.mc.keyboardHandler.getClipboard();
            if (clipboard != null && !clipboard.isEmpty()) {
                this.currentUrl = clipboard;
                if (this.urlField != null) {
                    this.urlField.setValue(clipboard);
                }
                loadPreview();
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }

    private void onApply() {
        if (this.sign != null && !this.currentUrl.isEmpty()) {
            writeUrlToSign();
        }
    }

    private void onDone() {
        onApply();
        this.onClose();
    }

    private void writeUrlToSign() {
        if (this.sign == null) return;

        // 将URL分成4行
        String[] lines = splitUrl(this.currentUrl, 4);

        // 发送网络包到服务器
        cn.minerealms.signpicture.network.NetworkHandler.INSTANCE.sendToServer(
                new cn.minerealms.signpicture.network.UpdateSignPacket(
                        this.sign.getBlockPos(),
                        lines,
                        true // 正面文本
                )
        );
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
        this.mc.setScreen(new GuiSize(this, this.currentUrl));
    }

    private void openRotationGui() {
        this.mc.setScreen(new GuiRotation(this, this.currentUrl));
    }

    private void openOffsetGui() {
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

        // 绘制预览区域标签
        guiGraphics.drawString(this.font, "Preview:",
                this.guiLeft + 10, this.guiTop + 58, 0x404040, false);

        // 绘制预览
        if (this.showPreview && this.previewImage != null) {
            int previewX = this.guiLeft + 10;
            int previewY = this.guiTop + 70;
            int maxWidth = this.xSize - 120;
            int maxHeight = 150;

            try {
                // 绘制预览边框
                guiGraphics.fill(previewX - 1, previewY - 1,
                        previewX + maxWidth + 1, previewY + maxHeight + 1,
                        0xFF000000);

                ImageRenderer.renderImage(guiGraphics, this.previewImage,
                        previewX, previewY, maxWidth, maxHeight);
            } catch (Exception e) {
                // 忽略渲染错误
            }
        } else if (!this.currentUrl.isEmpty()) {
            guiGraphics.drawString(this.font, "Loading preview...",
                    this.guiLeft + 10, this.guiTop + 70, 0x808080, false);
        } else {
            guiGraphics.drawString(this.font, "Enter an image URL above",
                    this.guiLeft + 10, this.guiTop + 70, 0x808080, false);
        }

        // 绘制提示信息
        guiGraphics.drawString(this.font, "Tip: Use Screenshot to capture, Upload to share",
                this.guiLeft + 10, this.guiTop + 230, 0x606060, false);
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
}
