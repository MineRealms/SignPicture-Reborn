package cn.minerealms.signpicture.handler;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * 按键处理器
 */
@OnlyIn(Dist.CLIENT)
public class KeyHandler {
    public static final KeyHandler instance = new KeyHandler();
    
    // 按键定义
    public KeyMapping keyOpenGui;
    public KeyMapping keyTogglePreview;
    
    public void registerKeys(RegisterKeyMappingsEvent event) {
        keyOpenGui = new KeyMapping(
            "key.signpicture.open_gui",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.signpicture"
        );
        
        keyTogglePreview = new KeyMapping(
            "key.signpicture.toggle_preview",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.signpicture"
        );
        
        event.register(keyOpenGui);
        event.register(keyTogglePreview);
    }
    
    public void handleKeyInput() {
        // TODO: 处理按键输入
        if (keyOpenGui.consumeClick()) {
            // 打开GUI
        }
        
        if (keyTogglePreview.consumeClick()) {
            // 切换预览模式
        }
    }
}
