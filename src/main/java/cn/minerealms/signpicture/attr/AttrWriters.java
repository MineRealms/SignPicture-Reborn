package cn.minerealms.signpicture.attr;

import cn.minerealms.signpicture.attr.prop.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * 属性写入器
 * 将属性对象转换为文本格式
 */
public class AttrWriters {

    private final List<String> attributes = new ArrayList<>();

    public AttrWriters() {
    }

    /**
     * 添加大小属性
     */
    public AttrWriters addSize(@Nonnull SizeData size) {
        if (size.getWidth() != 1.0f) {
            attributes.add("w=" + size.getWidth());
        }
        if (size.getHeight() != 1.0f) {
            attributes.add("h=" + size.getHeight());
        }
        return this;
    }

    /**
     * 添加旋转属性
     */
    public AttrWriters addRotation(@Nonnull RotationData rotation) {
        if (rotation.getX() != 0.0f) {
            attributes.add("rx=" + rotation.getX());
        }
        if (rotation.getY() != 0.0f) {
            attributes.add("ry=" + rotation.getY());
        }
        if (rotation.getZ() != 0.0f) {
            attributes.add("rz=" + rotation.getZ());
        }
        return this;
    }

    /**
     * 添加偏移属性
     */
    public AttrWriters addOffset(@Nonnull OffsetData offset) {
        if (offset.getX() != 0.0f) {
            attributes.add("ox=" + offset.getX());
        }
        if (offset.getY() != 0.0f) {
            attributes.add("oy=" + offset.getY());
        }
        if (offset.getZ() != 0.0f) {
            attributes.add("oz=" + offset.getZ());
        }
        return this;
    }

    /**
     * 添加动画属性
     */
    public AttrWriters addAnimation(@Nonnull AnimationData animation) {
        if (!"linear".equals(animation.getEasing())) {
            attributes.add("easing=" + animation.getEasing());
        }
        if (!"none".equals(animation.getRedstone())) {
            attributes.add("redstone=" + animation.getRedstone());
        }
        return this;
    }

    /**
     * 添加纹理属性
     */
    public AttrWriters addTexture(@Nonnull TextureData texture) {
        if (texture.getU() != 0.0f) {
            attributes.add("u=" + texture.getU());
        }
        if (texture.getV() != 0.0f) {
            attributes.add("v=" + texture.getV());
        }
        if (texture.getW() != 1.0f) {
            attributes.add("w=" + texture.getW());
        }
        if (texture.getH() != 1.0f) {
            attributes.add("h=" + texture.getH());
        }
        if (texture.getOpacity() != 1.0f) {
            attributes.add("opacity=" + texture.getOpacity());
        }
        if (texture.isRepeat()) {
            attributes.add("repeat=true");
        }
        if (!texture.isMipmap()) {
            attributes.add("mipmap=false");
        }
        if (!texture.isLighting()) {
            attributes.add("lighting=false");
        }
        return this;
    }

    /**
     * 添加自定义属性
     */
    public AttrWriters addCustom(@Nonnull String key, @Nonnull String value) {
        attributes.add(key + "=" + value);
        return this;
    }

    /**
     * 构建属性字符串
     */
    @Nonnull
    public String build() {
        return String.join(",", attributes);
    }

    /**
     * 构建属性字符串（带分隔符）
     */
    @Nonnull
    public String build(@Nonnull String separator) {
        return String.join(separator, attributes);
    }

    /**
     * 清空属性
     */
    public void clear() {
        attributes.clear();
    }

    /**
     * 获取属性数量
     */
    public int size() {
        return attributes.size();
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    @Override
    public String toString() {
        return build();
    }

    // 静态工厂方法
    public static AttrWriters create() {
        return new AttrWriters();
    }

    /**
     * 从AttrReaders创建
     */
    public static AttrWriters from(@Nonnull AttrReaders readers) {
        return new AttrWriters()
                .addSize(readers.getSizeData())
                .addRotation(readers.getRotationData())
                .addOffset(readers.getOffsetData())
                .addAnimation(readers.getAnimationData())
                .addTexture(readers.getTextureData());
    }
}
