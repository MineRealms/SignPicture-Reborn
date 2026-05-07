package cn.minerealms.signpicture.mixin;

import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.entry.Entry;
import cn.minerealms.signpicture.entry.EntryId;
import cn.minerealms.signpicture.entry.EntryManager;
import cn.minerealms.signpicture.entry.content.Content;
import cn.minerealms.signpicture.entry.content.ContentId;
import cn.minerealms.signpicture.render.ImageRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 书本编辑界面Mixin
 * 用于在书本GUI中渲染图片
 */
@Mixin(BookEditScreen.class)
public class BookEditScreenMixin {

    // URL匹配模式
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(https?://[^\\s]+\\.(png|jpg|jpeg|gif|bmp|webp))",
        Pattern.CASE_INSENSITIVE
    );

    private String cachedUrl = null;
    private long lastCheckTime = 0;

    /**
     * 注入render方法
     * 在书本渲染后渲染图片
     */
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // 检查是否启用书本图片
        if (!Config.CLIENT.chatpicEnable.get()) {
            return;
        }

        // 每秒检查一次（避免频繁解析）
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTime < 1000 && cachedUrl == null) {
            return;
        }
        lastCheckTime = currentTime;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        try {
            // 尝试从书本屏幕获取文本
            BookEditScreen screen = (BookEditScreen) (Object) this;

            // 简单实现：只在有缓存URL时渲染
            if (cachedUrl != null) {
                // 获取Entry和Content
                EntryId entryId = EntryId.from("book_page");
                ContentId contentId = ContentId.from(cachedUrl);
                Entry entry = EntryManager.instance.get(entryId, contentId);
                Content content = entry.getContent();

                if (content != null && content.isAvailable()) {
                    BufferedImage image = content.getImage();
                    if (image != null) {
                        // 计算书本页面中心位置
                        int screenWidth = guiGraphics.guiWidth();
                        int screenHeight = guiGraphics.guiHeight();
                        int bookWidth = 192;
                        int bookHeight = 192;

                        int x = (screenWidth - bookWidth) / 2 + 36;
                        int y = (screenHeight - bookHeight) / 2 + 30;

                        // 渲染图片（适应书本页面大小）
                        int maxWidth = 120;
                        int maxHeight = 140;
                        ImageRenderer.renderImage(guiGraphics, image, x, y, maxWidth, maxHeight);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略渲染错误
        }

        poseStack.popPose();
    }
}
