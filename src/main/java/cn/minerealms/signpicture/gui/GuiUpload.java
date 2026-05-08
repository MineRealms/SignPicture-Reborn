package cn.minerealms.signpicture.gui;

import cn.minerealms.signpicture.ClientLog;
import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.api.ImgurUploader;
import cn.minerealms.signpicture.api.ImageUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 图片上传界面
 * 支持上传截图到图床
 */
public class GuiUpload extends BaseGuiScreen {

    @Nullable
    private final BufferedImage image;
    @Nullable
    private final File imageFile;

    @Nullable
    private EditBox apiKeyField;
    @Nullable
    private Button uploadButton;
    @Nullable
    private Button cancelButton;
    @Nullable
    private Button selectFileButton;

    private String uploadStatus = "";
    private String resultUrl = "";

    public GuiUpload(@Nullable Screen parentScreen) {
        this(parentScreen, null, null);
    }

    public GuiUpload(@Nullable Screen parentScreen, @Nullable BufferedImage image) {
        this(parentScreen, image, null);
    }

    public GuiUpload(@Nullable Screen parentScreen, @Nullable File imageFile) {
        this(parentScreen, null, imageFile);
    }

    private GuiUpload(@Nullable Screen parentScreen, @Nullable BufferedImage image, @Nullable File imageFile) {
        super(Component.literal("Upload Image"), parentScreen);
        this.image = image;
        this.imageFile = imageFile;
        this.xSize = 300;
        this.ySize = 200;
    }

    @Override
    protected void init() {
        super.init();

        // API Key输入框
        this.apiKeyField = new EditBox(this.font,
                this.guiLeft + 10, this.guiTop + 50,
                this.xSize - 20, 20,
                Component.literal("API Key"));
        this.apiKeyField.setMaxLength(200);
        this.apiKeyField.setValue(Config.COMMON.apiUploaderKey.get());
        this.addRenderableWidget(this.apiKeyField);

        // 选择文件按钮
        this.selectFileButton = Button.builder(Component.literal("Select File"),
                button -> this.onSelectFile())
                .bounds(this.guiLeft + 10, this.guiTop + 80, 120, 20)
                .build();
        this.addRenderableWidget(this.selectFileButton);

        // 上传按钮
        this.uploadButton = Button.builder(Component.literal("Upload"),
                button -> this.onUpload())
                .bounds(this.guiLeft + 10, this.guiTop + this.ySize - 30, 80, 20)
                .build();
        this.addRenderableWidget(this.uploadButton);

        // 取消按钮
        this.cancelButton = Button.builder(Component.literal("Cancel"),
                button -> this.onClose())
                .bounds(this.guiLeft + 100, this.guiTop + this.ySize - 30, 80, 20)
                .build();
        this.addRenderableWidget(this.cancelButton);
    }

    private void onSelectFile() {
        // TODO: 打开文件选择对话框
        this.uploadStatus = "File selection not implemented yet";
    }

    private void onUpload() {
        if (this.image == null && this.imageFile == null) {
            this.uploadStatus = "No image to upload";
            return;
        }

        String apiKey = this.apiKeyField != null ? this.apiKeyField.getValue() : "";
        if (apiKey.isEmpty()) {
            // 尝试从配置读取
            apiKey = Config.COMMON.apiUploaderKey.get();
            if (apiKey.isEmpty()) {
                this.uploadStatus = "Please enter API key";
                return;
            }
        }

        this.uploadStatus = "Uploading...";
        Log.debug("Starting upload to Imgur...");

        // 创建上传器
        ImgurUploader uploader = new ImgurUploader();

        // 异步上传
        final String finalApiKey = apiKey;
        if (this.imageFile != null) {
            // 上传文件
            uploader.upload(this.imageFile, finalApiKey).thenAccept(result -> {
                handleUploadResult(result);
            }).exceptionally(throwable -> {
                this.mc.execute(() -> {
                    this.uploadStatus = "Upload error: " + throwable.getMessage();
                    Log.error("Upload error", throwable);
                });
                return null;
            });
        } else if (this.image != null) {
            // 上传BufferedImage
            uploader.upload(this.image, finalApiKey).thenAccept(result -> {
                handleUploadResult(result);
            }).exceptionally(throwable -> {
                this.mc.execute(() -> {
                    this.uploadStatus = "Upload error: " + throwable.getMessage();
                    Log.error("Upload error", throwable);
                });
                return null;
            });
        }
    }

    private void handleUploadResult(ImageUploader.UploadResult result) {
        // 在主线程更新UI
        this.mc.execute(() -> {
            if (result.isSuccess()) {
                this.resultUrl = result.getUrl();
                this.uploadStatus = "Upload successful!";
                Log.info("Upload successful: " + this.resultUrl);
                ClientLog.notice("Image uploaded successfully!");

                // 如果父界面是GuiMainFull，自动填充URL
                if (this.parentScreen instanceof GuiMainFull) {
                    GuiMainFull parent = (GuiMainFull) this.parentScreen;
                    // 这里需要添加一个方法来设置URL
                    // parent.setUrl(this.resultUrl);
                }
            } else {
                this.uploadStatus = "Upload failed: " + result.getError();
                Log.error("Upload failed: " + result.getError());
                ClientLog.notice("Upload failed: " + result.getError());
            }
        });
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制GUI背景
        renderGuiBackground(guiGraphics);

        // 绘制标题
        renderTitle(guiGraphics, "Upload Image");

        // 绘制说明
        guiGraphics.drawString(this.font, "Upload to image hosting service",
                this.guiLeft + 10, this.guiTop + 20, 0x404040, false);

        // 绘制API Key标签
        guiGraphics.drawString(this.font, "API Key:",
                this.guiLeft + 10, this.guiTop + 38, 0x404040, false);

        // 绘制状态
        if (!this.uploadStatus.isEmpty()) {
            guiGraphics.drawString(this.font, this.uploadStatus,
                    this.guiLeft + 10, this.guiTop + 110, 0x404040, false);
        }

        // 绘制结果URL
        if (!this.resultUrl.isEmpty()) {
            guiGraphics.drawString(this.font, "URL: " + this.resultUrl,
                    this.guiLeft + 10, this.guiTop + 130, 0x0000FF, false);
        }

        // 绘制提示
        guiGraphics.drawString(this.font, "Supported: Imgur, Gyazo",
                this.guiLeft + 10, this.guiTop + 150, 0x808080, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.apiKeyField != null && this.apiKeyField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.apiKeyField != null && this.apiKeyField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
}
