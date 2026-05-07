package cn.minerealms.signpicture.client;

import cn.minerealms.signpicture.gui.GuiMainFull;
import cn.minerealms.signpicture.gui.GuiSettings;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 按键绑定处理器
 * 管理所有按键绑定和输入处理
 */
@Mod.EventBusSubscriber(modid = "signpicture", value = Dist.CLIENT)
public class KeyBindings {

    // 按键映射
    public static KeyMapping openGuiKey;
    public static KeyMapping openSettingsKey;
    public static KeyMapping takeScreenshotKey;

    /**
     * 注册按键绑定
     */
    public static void register(RegisterKeyMappingsEvent event) {
        // 打开主GUI的按键 (默认: G)
        openGuiKey = new KeyMapping(
                "key.signpicture.open_gui",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key.categories.signpicture"
        );
        event.register(openGuiKey);

        // 打开设置的按键 (默认: Shift+G)
        openSettingsKey = new KeyMapping(
                "key.signpicture.open_settings",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key.categories.signpicture"
        );
        event.register(openSettingsKey);

        // 截图按键 (默认: F9)
        takeScreenshotKey = new KeyMapping(
                "key.signpicture.screenshot",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                "key.categories.signpicture"
        );
        event.register(takeScreenshotKey);
    }

    /**
     * 处理按键输入
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }

        // 检查打开主GUI按键
        if (openGuiKey.consumeClick()) {
            mc.setScreen(new GuiMainFull(null, null));
        }

        // 检查打开设置按键
        if (openSettingsKey.consumeClick()) {
            mc.setScreen(new GuiSettings(null));
        }

        // 检查截图按键
        if (takeScreenshotKey.consumeClick()) {
            // TODO: 打开截图界面或直接截图
        }
    }
}
