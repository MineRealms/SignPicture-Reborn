package cn.minerealms.signpicture.util;

import com.google.common.collect.Maps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * 聊天消息构建器
 * 用于构建和发送聊天消息
 */
public class ChatBuilder {
    public static final int DEFAULT_ID = 877;
    
    private @Nullable Component chat = null;
    private @Nullable Style style = null;
    private @Nonnull String text = "";
    private @Nonnull Object[] params = new Object[0];
    private boolean useTranslation = false;
    private boolean useJson = false;
    private boolean useId = false;
    private final @Nonnull Map<String, String> replace = Maps.newHashMap();
    private int id = -1;
    
    /**
     * 构建Component
     */
    public @Nonnull Component build() {
        MutableComponent component;
        
        if (this.chat != null) {
            component = this.chat.copy();
        } else if (this.useTranslation && !this.useJson) {
            component = Component.translatable(this.text, this.params);
        } else {
            String s;
            if (this.useTranslation) {
                // 使用翻译
                component = Component.translatable(this.text);
                s = component.getString();
            } else {
                s = this.text;
            }
            
            // 应用替换
            for (final Map.Entry<String, String> entry : this.replace.entrySet()) {
                s = StringUtils.replace(s, entry.getKey(), entry.getValue());
            }
            
            // 应用参数
            if (this.params.length > 0) {
                s = String.format(s, this.params);
            }
            
            if (this.useJson) {
                try {
                    component = Component.Serializer.fromJson(s);
                    if (component == null) {
                        component = Component.literal("Invalid Json: " + this.text);
                    }
                } catch (final Exception e) {
                    component = Component.literal("Invalid Json: " + this.text);
                }
            } else {
                component = Component.literal(s);
            }
        }
        
        if (this.style != null) {
            component.setStyle(this.style);
        }
        
        return component;
    }
    
    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        if (StringUtils.isEmpty(this.text)) {
            if (this.chat != null) {
                return StringUtils.isEmpty(this.chat.getString());
            }
            return true;
        }
        return false;
    }
    
    /**
     * 设置消息ID
     */
    public @Nonnull ChatBuilder setId(final int id) {
        this.useId = true;
        this.id = id;
        return this;
    }
    
    /**
     * 使用默认ID
     */
    public @Nonnull ChatBuilder setId() {
        return setId(DEFAULT_ID);
    }
    
    /**
     * 设置Component
     */
    public @Nonnull ChatBuilder setChat(final @Nullable Component chat) {
        this.chat = chat;
        return this;
    }
    
    /**
     * 设置文本
     */
    public @Nonnull ChatBuilder setText(final @Nonnull String text) {
        this.text = text;
        return this;
    }
    
    /**
     * 设置参数
     */
    public @Nonnull ChatBuilder setParams(final @Nonnull Object... params) {
        this.params = params;
        return this;
    }
    
    /**
     * 设置样式
     */
    public @Nonnull ChatBuilder setStyle(final @Nullable Style style) {
        this.style = style;
        return this;
    }
    
    /**
     * 使用翻译
     */
    public @Nonnull ChatBuilder useTranslation() {
        this.useTranslation = true;
        return this;
    }
    
    /**
     * 使用JSON格式
     */
    public @Nonnull ChatBuilder useJson() {
        this.useJson = true;
        return this;
    }
    
    /**
     * 添加替换规则
     */
    public @Nonnull ChatBuilder replace(final @Nonnull String from, final @Nonnull String to) {
        this.replace.put(from, to);
        return this;
    }
    
    /**
     * 创建ChatBuilder
     */
    public static @Nonnull ChatBuilder create(final @Nonnull String text) {
        return new ChatBuilder().setText(text);
    }
    
    /**
     * 发送给玩家
     */
    public void sendPlayer(final @Nonnull CommandSourceStack source) {
        if (!isEmpty()) {
            source.sendSuccess(() -> build(), false);
        }
    }
    
    /**
     * 发送给玩家（静态方法）
     */
    public static void sendPlayer(final @Nonnull CommandSourceStack source, final @Nonnull ChatBuilder chat) {
        chat.sendPlayer(source);
    }
    
    /**
     * 广播消息
     */
    public static void sendServer(final @Nonnull ChatBuilder chat) {
        // TODO: 实现服务器广播
        // 需要访问MinecraftServer实例
    }
}
