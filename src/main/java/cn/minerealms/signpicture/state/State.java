package cn.minerealms.signpicture.state;

import cn.minerealms.signpicture.LoadCanceledException;
import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.entry.content.ContentBlockedException;
import cn.minerealms.signpicture.entry.content.ContentCapacityOverException;
import cn.minerealms.signpicture.entry.content.RetryCountOverException;
import cn.minerealms.signpicture.image.InvalidImageException;
import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * 状态管理类
 * 跟踪内容加载状态、进度和错误信息
 */
public class State {
    private @Nonnull String name = "";
    private @Nonnull Progress progress = new Progress();
    private @Nonnull StateType type = StateType.INIT;
    private @Nonnull String message = "";
    private final @Nonnull Map<String, Object> meta = Maps.newHashMap();
    
    public @Nonnull State setName(final @Nonnull String name) {
        this.name = name;
        return this;
    }
    
    public @Nonnull String getName() {
        return this.name;
    }
    
    public @Nonnull State setProgress(final @Nonnull Progress progress) {
        this.progress = progress;
        return this;
    }
    
    public @Nonnull Progress getProgress() {
        return this.progress;
    }
    
    public @Nonnull State setMessage(final @Nonnull String message) {
        this.message = message;
        return this;
    }
    
    public @Nonnull Map<String, Object> getMeta() {
        return this.meta;
    }
    
    /**
     * 设置错误消息
     * 根据异常类型生成本地化的错误消息
     */
    public @Nonnull State setErrorMessage(final @Nullable Throwable throwable) {
        if (throwable != null) {
            setType(StateType.ERROR);
            try {
                throw throwable;
            } catch (final URISyntaxException e) {
                setMessage(translatable("signpic.advmsg.invalidurl", e.getMessage()));
            } catch (final LoadCanceledException e) {
                setMessage(translatable("signpic.advmsg.loadstopped", e.getMessage()));
            } catch (final RetryCountOverException e) {
                setMessage(translatable("signpic.advmsg.retryover", e.getMessage()));
            } catch (final ContentCapacityOverException e) {
                setMessage(translatable("signpic.advmsg.capacityover", e.getMessage()));
            } catch (final ContentBlockedException e) {
                setMessage(translatable("signpic.advmsg.blocked", e.getMessage()));
            } catch (final InvalidImageException e) {
                setMessage(translatable("signpic.advmsg.invalidimage", e.getMessage()));
            } catch (final IOException e) {
                setMessage(translatable("signpic.advmsg.ioerror", e.getMessage()));
            } catch (final Throwable e) {
                setMessage(translatable("signpic.advmsg.unknown", e.getMessage()));
            }
            Log.debug("Error: " + getMessage() + " - " + throwable.getMessage());
        }
        return this;
    }
    
    public @Nonnull String getMessage() {
        return this.message;
    }
    
    public @Nonnull State setType(final @Nonnull StateType type) {
        this.type = type;
        return this;
    }
    
    public @Nonnull StateType getType() {
        return this.type;
    }
    
    /**
     * 获取状态消息（包含进度百分比）
     */
    @OnlyIn(Dist.CLIENT)
    public @Nonnull String getStateMessage() {
        return translatable(this.type.translationKey, (int) (this.progress.getProgress() * 100));
    }
    
    private static String translatable(String key, Object... args) {
        try {
            return Component.translatable(key, args).getString();
        } catch (Exception e) {
            return key;
        }
    }
}