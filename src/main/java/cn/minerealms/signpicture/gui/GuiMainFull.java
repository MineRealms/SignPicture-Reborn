package cn.minerealms.signpicture.gui;

import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.data.SignPictureData;
import cn.minerealms.signpicture.data.SignPictureDataManagerClient;
import cn.minerealms.signpicture.data.SignPictureHelper;
import cn.minerealms.signpicture.entry.Entry;
import cn.minerealms.signpicture.entry.EntryId;
import cn.minerealms.signpicture.entry.EntryManager;
import cn.minerealms.signpicture.entry.content.Content;
import cn.minerealms.signpicture.entry.content.ContentId;
import cn.minerealms.signpicture.network.NetworkHandler;
import cn.minerealms.signpicture.network.RequestSignPicturePacket;
import cn.minerealms.signpicture.render.ImageRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 告示牌编辑主界面 - 完整版
 * 提供完整的图片编辑功能，类似1.12.2版本
 */
@OnlyIn(Dist.CLIENT)
public class GuiMainFull extends BaseGuiScreen {

    @Nullable
    private final SignBlockEntity sign;

    // 当前编辑的SignPicture UUID（如果是编辑现有的）
    @Nullable
    private String editingUUID = null;

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

    // 数据加载状态
    private boolean dataLoaded = false;
    private int loadCheckTicks = 0;

    // 属性存储
    private float sizeWidth = 1.0f;
    private float sizeHeight = 1.0f;
    private float rotationX = 0.0f;
    private float rotationY = 0.0f;
    private float rotationZ = 0.0f;
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float offsetZ = 0.0f;

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
        // 检查告示牌是否已经是SignPicture
        if (this.sign == null) return;

        // 检查是否是SignPicture
        if (!SignPictureHelper.isSignPicture(this.sign)) {
            // 不是SignPicture，保持空白
            return;
        }

        // 提取UUID
        String uuid = SignPictureHelper.getUUID(this.sign);
        if (uuid == null || uuid.isEmpty()) {
            return;
        }

        this.editingUUID = uuid;

        // 从客户端缓存加载数据
        SignPictureData data = SignPictureDataManagerClient.INSTANCE.getMetadata(uuid);
        if (data == null) {
            // 缓存中没有，请求服务端
            NetworkHandler.sendToServer(new RequestSignPicturePacket(uuid));
            Log.info("[GUI] Requesting SignPicture data: " + uuid);
            // 数据会通过ResponseSignPicturePacket异步返回，暂时显示加载中
            return;
        }

        // 加载数据到GUI
        loadDataToGui(data);
        dataLoaded = true; // 标记数据已加载
    }

    /**
     * 加载SignPictureData到GUI（客户端）
     */
    @OnlyIn(Dist.CLIENT)
    public void loadDataToGui(@Nonnull SignPictureData data) {
        this.currentUrl = data.getUrl();
        this.sizeWidth = data.getSizeWidth();
        this.sizeHeight = data.getSizeHeight();
        this.rotationX = data.getRotationX();
        this.rotationY = data.getRotationY();
        this.rotationZ = data.getRotationZ();
        this.offsetX = data.getOffsetX();
        this.offsetY = data.getOffsetY();
        this.offsetZ = data.getOffsetZ();

        if (this.urlField != null) {
            this.urlField.setValue(this.currentUrl);
        }

        loadPreview();

        Log.info("[GUI] Loaded SignPicture data: " + data.getUuid() +
                 " - Size: " + sizeWidth + "x" + sizeHeight +
                 ", Rotation: " + rotationX + "," + rotationY + "," + rotationZ +
                 ", Offset: " + offsetX + "," + offsetY + "," + offsetZ);
    }

    private void loadPreview() {
        if (this.currentUrl.isEmpty()) {
            this.previewImage = null;
            return;
        }

        // 触发下载但不立即获取图片
        // 图片会在render()方法中异步获取
        EntryId entryId = EntryId.from("gui_preview");
        ContentId contentId = ContentId.from(this.currentUrl);
        EntryManager.instance.get(entryId, contentId);
        // 不设置previewImage，让render()方法处理
    }

    private void onScreenshot() {
        try {
            Log.debug("Taking screenshot...");
            File screenshotDir = new File(this.mc.gameDirectory, "screenshots");
            File screenshot = ScreenshotUtil.takeAndSaveScreenshot(screenshotDir);

            if (screenshot != null && screenshot.exists()) {
                Log.info("Screenshot saved: " + screenshot.getName());
                Log.notice("Screenshot saved: " + screenshot.getName());

                // 可以选择将截图路径放入URL框
                // this.currentUrl = screenshot.getAbsolutePath();
                // if (this.urlField != null) {
                //     this.urlField.setValue(this.currentUrl);
                // }
            } else {
                Log.error("Failed to save screenshot");
                Log.notice("Failed to save screenshot");
            }
        } catch (Exception e) {
            Log.error("Screenshot error", e);
            Log.notice("Screenshot error: " + e.getMessage());
        }
    }

    private void onUpload() {
        Log.debug("Opening upload GUI...");
        this.mc.setScreen(new GuiUpload(this));
    }

    private void onShortenUrl() {
        if (this.currentUrl.isEmpty()) {
            Log.notice("No URL to shorten");
            return;
        }

        Log.debug("Shortening URL: " + this.currentUrl);

        // 检查配置的缩短服务
        String shortenerType = Config.COMMON.apiShortenerType.get();
        String shortenerKey = Config.COMMON.apiShortenerKey.get();

        if (shortenerType.isEmpty()) {
            Log.notice("No URL shortener configured. Set apiShortenerType in config (e.g., 'bitly')");
            return;
        }

        if (shortenerKey.isEmpty()) {
            Log.notice("No API key configured for URL shortener. Set apiShortenerKey in config.");
            return;
        }

        // 创建缩短器
        cn.minerealms.signpicture.api.UrlShortener shortener = null;
        if ("bitly".equalsIgnoreCase(shortenerType)) {
            shortener = new cn.minerealms.signpicture.api.BitlyShortener();
        } else {
            Log.notice("Unknown shortener type: " + shortenerType + ". Supported: bitly");
            return;
        }

        Log.notice("Shortening URL...");

        // 异步缩短URL
        final cn.minerealms.signpicture.api.UrlShortener finalShortener = shortener;
        shortener.shorten(this.currentUrl, shortenerKey).thenAccept(result -> {
            // 在主线程更新UI
            this.mc.execute(() -> {
                if (result.isSuccess()) {
                    String shortUrl = result.getShortUrl();
                    Log.info("URL shortened: " + shortUrl);
                    Log.notice("URL shortened successfully!");

                    // 更新URL输入框
                    this.currentUrl = shortUrl;
                    if (this.urlField != null) {
                        this.urlField.setValue(shortUrl);
                    }
                } else {
                    Log.error("URL shorten failed: " + result.getError());
                    Log.notice("Failed to shorten URL: " + result.getError());
                }
            });
        }).exceptionally(throwable -> {
            this.mc.execute(() -> {
                Log.error("URL shorten error", throwable);
                Log.notice("Error: " + throwable.getMessage());
            });
            return null;
        });
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
            createSignPicture();
        }
    }

    private void onDone() {
        if (this.sign != null && !this.currentUrl.isEmpty()) {
            createSignPicture();
        }
        this.onClose();
    }

    /**
     * 创建或更新SignPicture - 发送到服务端
     */
    private void createSignPicture() {
        if (this.sign == null || this.currentUrl.isEmpty()) {
            return;
        }

        // 验证URL格式
        if (!this.currentUrl.startsWith("http://") && !this.currentUrl.startsWith("https://")) {
            Log.notice("Invalid URL: must start with http:// or https://");
            return;
        }

        // 区分创建和更新
        if (this.editingUUID != null) {
            // 更新现有SignPicture
            Log.info("[GUI] Updating SignPicture " + this.editingUUID + " at " + this.sign.getBlockPos());
            Log.info("[GUI] New parameters - Size: " + sizeWidth + "x" + sizeHeight +
                     ", Rotation: " + rotationX + "," + rotationY + "," + rotationZ +
                     ", Offset: " + offsetX + "," + offsetY + "," + offsetZ);

            SignPictureData data = new SignPictureData(this.editingUUID, this.currentUrl);
            data.setSize(this.sizeWidth, this.sizeHeight);
            data.setRotation(this.rotationX, this.rotationY, this.rotationZ);
            data.setOffset(this.offsetX, this.offsetY, this.offsetZ);

            Log.info("[GUI] Sending UpdateSignPicturePacket to server");
            cn.minerealms.signpicture.network.NetworkHandler.sendToServer(
                    new cn.minerealms.signpicture.network.UpdateSignPicturePacket(data)
            );
        } else {
            // 创建新SignPicture
            Log.info("[GUI] Creating SignPicture at " + this.sign.getBlockPos());

            cn.minerealms.signpicture.network.NetworkHandler.sendToServer(
                    new cn.minerealms.signpicture.network.CreateSignPicturePacket(
                            this.sign.getBlockPos(),
                            this.currentUrl,
                            this.sizeWidth, this.sizeHeight,
                            this.rotationX, this.rotationY, this.rotationZ,
                            this.offsetX, this.offsetY, this.offsetZ
                    )
            );
        }
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

        // 绘制预览（异步获取图片）
        if (this.showPreview && !this.currentUrl.isEmpty()) {
            int previewX = this.guiLeft + 10;
            int previewY = this.guiTop + 70;
            int maxWidth = this.xSize - 120;
            int maxHeight = 150;

            try {
                // 尝试获取图片
                EntryId entryId = EntryId.from("gui_preview");
                ContentId contentId = ContentId.from(this.currentUrl);
                Entry entry = EntryManager.instance.get(entryId, contentId);
                Content content = entry.getContent();

                // 严格检查：必须isAvailable且image不为null
                if (content != null && content.isAvailable()) {
                    BufferedImage image = content.getImage();
                    if (image != null && image.getWidth() > 0 && image.getHeight() > 0) {
                        // 绘制预览边框
                        guiGraphics.fill(previewX - 1, previewY - 1,
                                previewX + maxWidth + 1, previewY + maxHeight + 1,
                                0xFF000000);

                        ImageRenderer.renderImage(guiGraphics, image,
                                previewX, previewY, maxWidth, maxHeight);
                    } else {
                        // 图片无效，显示加载中
                        guiGraphics.drawString(this.font, "Loading preview...",
                                this.guiLeft + 10, this.guiTop + 70, 0x808080, false);
                    }
                } else {
                    // 还在下载，显示加载中
                    guiGraphics.drawString(this.font, "Loading preview...",
                            this.guiLeft + 10, this.guiTop + 70, 0x808080, false);
                }
            } catch (Exception e) {
                guiGraphics.drawString(this.font, "Failed to load preview",
                        this.guiLeft + 10, this.guiTop + 70, 0xFF0000, false);
            }
        } else if (this.currentUrl.isEmpty()) {
            guiGraphics.drawString(this.font, "Enter an image URL above",
                    this.guiLeft + 10, this.guiTop + 70, 0x808080, false);
        }

        // 绘制提示信息
        guiGraphics.drawString(this.font, "Tip: Use Screenshot to capture, Upload to share",
                this.guiLeft + 10, this.guiTop + 230, 0x606060, false);
    }

    @Override
    public void tick() {
        super.tick();

        // 定期检查数据是否已加载（仅在编辑模式且数据未加载时）
        if (!dataLoaded && editingUUID != null) {
            loadCheckTicks++;

            // 每10 ticks检查一次（0.5秒）
            if (loadCheckTicks >= 10) {
                loadCheckTicks = 0;

                SignPictureData data = SignPictureDataManagerClient.INSTANCE.getMetadata(editingUUID);
                if (data != null) {
                    loadDataToGui(data);
                    dataLoaded = true;
                    Log.info("[GUI] Data loaded from cache: " + editingUUID);
                }
            }
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

    // ========== 属性设置方法 ==========

    public void setSize(float width, float height) {
        this.sizeWidth = width;
        this.sizeHeight = height;
        Log.debug("Size set: " + width + " x " + height);
    }

    public void setRotation(float x, float y, float z) {
        this.rotationX = x;
        this.rotationY = y;
        this.rotationZ = z;
        Log.debug("Rotation set: " + x + ", " + y + ", " + z);
    }

    public void setOffset(float x, float y, float z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        Log.debug("Offset set: " + x + ", " + y + ", " + z);
    }
}
