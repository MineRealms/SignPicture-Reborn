package cn.minerealms.signpicture.attr.prop;

import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 属性语法定义
 * 定义所有可用的单字符属性标识符
 */
public enum PropSyntax {
    // 动画属性
    ANIMATION_EASING("t"),
    ANIMATION_REDSTONE("k"),
    
    // 偏移属性
    OFFSET_LEFT("L"),
    OFFSET_RIGHT("R"),
    OFFSET_DOWN("D"),
    OFFSET_UP("U"),
    OFFSET_BACK("B"),
    OFFSET_FRONT("F"),
    OFFSET_CENTER_X("M"),
    OFFSET_CENTER_Y("N"),
    OFFSET_CENTER_Z("O"),
    
    // 旋转属性
    ROTATION_X("X"),
    ROTATION_Y("Y"),
    ROTATION_Z("Z"),
    ROTATION_ANGLE("A"),
    ROTATION_AXIS_X("I"),
    ROTATION_AXIS_Y("J"),
    ROTATION_AXIS_Z("K"),
    
    // 大小属性
    SIZE_W(""),
    SIZE_H("x"),
    
    // 纹理属性
    TEXTURE_X("u"),
    TEXTURE_Y("v"),
    TEXTURE_W("w"),
    TEXTURE_H("h"),
    TEXTURE_SPLIT_W("c"),
    TEXTURE_SPLIT_H("s"),
    TEXTURE_OPACITY("o"),
    TEXTURE_REPEAT("r"),
    TEXTURE_MIPMAP("m"),
    TEXTURE_LIGHTING("l"),
    TEXTURE_BLEND_SRC("b"),
    TEXTURE_BLEND_DST("d"),
    TEXTURE_LIGHT_X("f"),
    TEXTURE_LIGHT_Y("g"),
    
    // 保留字符
    _reserved_e("e"),
    _reserved_E("E");
    
    public final @Nonnull String id;
    
    PropSyntax(@Nonnull final String identifier) {
        this.id = identifier;
    }
    
    static {
        // 检查语法冲突
        final Map<String, PropSyntax> checkcache = Maps.newHashMap();
        final PropSyntax[] allprops = PropSyntax.values();
        for (final PropSyntax newprop : allprops) {
            final PropSyntax cacheprop = checkcache.get(newprop.id);
            if (cacheprop != null) {
                throw new IllegalStateException("conflicting sign syntax: [" + newprop.id + "](" + 
                    newprop.name() + "&" + cacheprop.name() + ")");
            }
            checkcache.put(newprop.id, newprop);
        }
    }
}
