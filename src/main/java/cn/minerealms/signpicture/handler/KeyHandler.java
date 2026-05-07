package cn.minerealms.signpicture.handler;

import cn.minerealms.signpicture.gui.GuiMainFull;
import cn.minerealms.signpicture.gui.GuiSettings;
import cn.minerealms.signpicture.gui.ScreenshotUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.io.File;

/**
 * 按键处理器
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "signpicture", value = Dist.CLIENT)
public class KeyHandler {
    public static final KeyHandler instance = new KeyHandler();

    // 按键定义
    public KeyMapping keyOpenGui;
    public KeyMapping keyOpenSettings;
    public KeyMapping keyScreenshot;

    public void registerKeys(RegisterKeyMappingsEvent event) {
        // 打开主GUI (默认: P)
        keyOpenGui = new KeyMapping(
            "key.signpicture.open_gui",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.signpicture"
        );

        // 打开设置 (默认: Shift+P)
        keyOpenSettings = new KeyMapping(
            "key.signpicture.open_settings",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.signpicture"
        );

        // 截图 (默认: F9)
        keyScreenshot = new KeyMapping(
            "key.signpicture.screenshot",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key.categories.signpicture"
        );

        event.register(keyOpenGui);
        event.register(keyOpenSettings);
        event.register(keyScreenshot);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        instance.handleKeyInput();
    }

    public void handleKeyInput() {
        Minecraft mc = Minecraft.getInstance();

        // 如果已经有GUI打开，不处理按键
        if (mc.player == null || mc.screen != null) {
            return;
        }

        // 打开主GUI
        if (keyOpenGui.consumeClick()) {
            mc.setScreen(new GuiMainFull(null, null));
        }

        // 打开设置
        if (keyOpenSettings.consumeClick()) {
            mc.setScreen(new GuiSettings(null));
        }

        // 截图
        if (keyScreenshot.consumeClick()) {
            try {
                File screenshotDir = new File(mc.gameDirectory, "screenshots");
                ScreenshotUtil.takeAndSaveScreenshot(screenshotDir);
                // TODO: 显示成功消息
            } catch (Exception e) {
                // TODO: 显示错误消息
            }
        }
    }
}
