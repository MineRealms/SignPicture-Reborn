package cn.minerealms.signpicture.attr.prop;

import cn.minerealms.signpicture.attr.IPropComposable;
import cn.minerealms.signpicture.attr.IPropInterpolatable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 大小数据
 * 简化版本，移除bnnwidget依赖
 */
public class SizeData implements IPropInterpolatable<SizeData>, IPropComposable {
    public static final float Default = 1f;
    public static final float Unknown = Float.NaN;
    
    public static final @Nonnull SizeData DefaultSize = new SizeData(Default, Default);
    public static final @Nonnull SizeData UnknownSize = new SizeData(Unknown, Unknown);
    
    private final float width;
    private final float height;
    
    public SizeData(final float width, final float height) {
        this.width = width;
        this.height = height;
    }
    
    public float getWidth() {
        return this.width;
    }
    
    public float getHeight() {
        return this.height;
    }
    
    public boolean validWidth() {
        return !Float.isNaN(getWidth());
    }
    
    public boolean validHeight() {
        return !Float.isNaN(getHeight());
    }
    
    public float max() {
        return Math.max(getWidth(), getHeight());
    }
    
    public float min() {
        return Math.min(getWidth(), getHeight());
    }
    
    public @Nonnull SizeData scale(final float scale) {
        return new SizeData(getWidth() * scale, getHeight() * scale);
    }
    
    @Override
    public @Nonnull SizeData per() {
        return this;
    }
    
    @Override
    public @Nonnull SizeData per(final float per, final @Nullable SizeData before) {
        if (before != null) {
            float w = getWidth() * per + before.getWidth() * (1f - per);
            float h = getHeight() * per + before.getHeight() * (1f - per);
            return new SizeData(w, h);
        }
        return this;
    }
    
    @Override
    public @Nonnull String compose() {
        StringBuilder sb = new StringBuilder();
        if (validWidth()) {
            sb.append(PropSyntax.SIZE_W.id).append(getWidth());
        }
        if (validHeight()) {
            sb.append(PropSyntax.SIZE_H.id).append(getHeight());
        }
        return sb.toString();
    }
    
    public static @Nonnull SizeData create(final float width, final float height) {
        return new SizeData(width, height);
    }
}
