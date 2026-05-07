package cn.minerealms.signpicture;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

/**
 * 统一日志工具类
 * 提供带[SignPicture]前缀的日志输出和游戏内通知
 */
public class Log {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PREFIX = "[SignPicture] ";

    /**
     * 是否启用DEBUG日志（从Config读取）
     */
    public static boolean isDebugEnabled() {
        return Config.CLIENT.debugLog.get();
    }

    // ========== 标准日志方法 ==========

    public static void info(String message) {
        LOGGER.info(PREFIX + message);
    }

    public static void info(String message, Object... args) {
        LOGGER.info(PREFIX + message, args);
    }

    public static void warn(String message) {
        LOGGER.warn(PREFIX + message);
    }

    public static void warn(String message, Object... args) {
        LOGGER.warn(PREFIX + message, args);
    }

    public static void error(String message) {
        LOGGER.error(PREFIX + message);
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.error(PREFIX + message, throwable);
    }

    public static void error(String message, Object... args) {
        LOGGER.error(PREFIX + message, args);
    }

    public static void debug(String message) {
        if (isDebugEnabled()) {
            LOGGER.info(PREFIX + "[DEBUG] " + message);
        }
    }

    public static void debug(String message, Object... args) {
        if (isDebugEnabled()) {
            LOGGER.info(PREFIX + "[DEBUG] " + message, args);
        }
    }

    // ========== 游戏内通知方法 ==========

    /**
     * 显示游戏内通知消息（聊天框）
     */
    public static void notice(final @Nonnull Object notice, final float duration) {
        String message = notice.toString();
        debug("Notice: " + message);

        // 在聊天框显示通知
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            player.displayClientMessage(Component.literal("§7[§6SignPicture§7] §f" + message), false);
        }
    }

    /**
     * 显示通知消息和异常
     */
    public static void notice(final @Nonnull String notice, final @Nonnull Throwable e, final float duration) {
        error(notice, e);

        // 在聊天框显示错误通知
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            player.displayClientMessage(Component.literal("§7[§6SignPicture§7] §c" + notice), false);
        }
    }

    /**
     * 显示通知消息（默认2秒）
     */
    public static void notice(final @Nonnull String notice) {
        notice(notice, 2f);
    }

    /**
     * 显示通知消息和异常（默认2秒）
     */
    public static void notice(final @Nonnull String notice, final @Nonnull Throwable e) {
        notice(notice, e, 2f);
    }
}
