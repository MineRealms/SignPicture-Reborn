package cn.minerealms.signpicture.mixin;

import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.entry.Entry;
import cn.minerealms.signpicture.entry.EntryId;
import cn.minerealms.signpicture.entry.EntryManager;
import cn.minerealms.signpicture.entry.content.Content;
import cn.minerealms.signpicture.entry.content.ContentId;
import cn.minerealms.signpicture.render.ImageRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 聊天组件Mixin
 * 用于在聊天中渲染图片
 */
@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private List<GuiMessage> allMessages;

    // URL匹配模式
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(https?://[^\\s]+\\.(png|jpg|jpeg|gif|bmp|webp))",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 注入render方法
     * 在聊天渲染后渲染图片
     */
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, CallbackInfo ci) {
        // 检查是否启用聊天图片
        if (!Config.CLIENT.chatpicEnable.get()) {
            return;
        }

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        try {
            // 遍历聊天消息
            int yOffset = 0;
            for (int i = 0; i < Math.min(allMessages.size(), 100); i++) {
                GuiMessage message = allMessages.get(i);
                Component component = message.content();
                String text = component.getString();

                // 查找URL
                Matcher matcher = URL_PATTERN.matcher(text);
                if (matcher.find()) {
                    String url = matcher.group(1);

                    // 获取Entry和Content
                    EntryId entryId = EntryId.from("chat_" + i);
                    ContentId contentId = ContentId.from(url);
                    Entry entry = EntryManager.instance.get(entryId, contentId);
                    Content content = entry.getContent();

                    if (content != null && content.isAvailable()) {
                        BufferedImage image = content.getImage();
                        if (image != null) {
                            // 计算渲染位置
                            int chatWidth = this.minecraft.getWindow().getGuiScaledWidth();
                            int x = 2;
                            int y = this.minecraft.getWindow().getGuiScaledHeight() - 40 - (i * 12) - yOffset;

                            // 渲染图片（缩小尺寸以适应聊天）
                            int maxWidth = 100;
                            int maxHeight = 100;
                            ImageRenderer.renderImage(guiGraphics, image, x, y, maxWidth, maxHeight);

                            yOffset += maxHeight;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略渲染错误
        }

        poseStack.popPose();
    }
}
