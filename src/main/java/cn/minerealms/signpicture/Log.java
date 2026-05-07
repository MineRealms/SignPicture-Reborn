package cn.minerealms.signpicture;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

/**
 * 日志工具类
 */
public class Log {
    public static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * 显示通知消息（暂时只记录日志，GUI通知将在后续实现）
     */
    public static void notice(final @Nonnull Object notice, final float duration) {
        // TODO: 实现GUI通知系统 (OverlayFrame)
        LOGGER.debug(notice.toString());
    }
    
    /**
     * 显示通知消息和异常
     */
    public static void notice(final @Nonnull String notice, final @Nonnull Throwable e, final float duration) {
        // TODO: 实现GUI通知系统 (OverlayFrame)
        LOGGER.debug(notice, e);
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
