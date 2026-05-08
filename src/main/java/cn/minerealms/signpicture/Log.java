package cn.minerealms.signpicture;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * 日志工具类
 */
public class Log {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PREFIX = "[SignPicture] ";

    public static boolean isDebugEnabled() {
        return false;
    }

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
    }

    public static void debug(String message, Object... args) {
    }
}