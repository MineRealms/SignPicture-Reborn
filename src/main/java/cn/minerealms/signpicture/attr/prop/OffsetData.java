package cn.minerealms.signpicture.attr.prop;

import cn.minerealms.signpicture.attr.IPropComposable;
import cn.minerealms.signpicture.attr.IPropInterpolatable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 偏移数据
 * 3D位置偏移
 */
public class OffsetData implements IPropInterpolatable<OffsetData>, IPropComposable {
    public static final @Nonnull OffsetData DefaultOffset = new OffsetData(0, 0, 0);
    
    private final float x;
    private final float y;
    private final float z;
    
    public OffsetData(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public float getX() {
        return this.x;
    }
    
    public float getY() {
        return this.y;
    }
    
    public float getZ() {
        return this.z;
    }
    
    @Override
    public @Nonnull OffsetData per() {
        return this;
    }
    
    @Override
    public @Nonnull OffsetData per(final float per, final @Nullable OffsetData before) {
        if (before != null) {
            float nx = getX() * per + before.getX() * (1f - per);
            float ny = getY() * per + before.getY() * (1f - per);
            float nz = getZ() * per + before.getZ() * (1f - per);
            return new OffsetData(nx, ny, nz);
        }
        return this;
    }
    
    @Override
    public @Nonnull String compose() {
        StringBuilder sb = new StringBuilder();
        if (getX() != 0) sb.append("M").append(getX());
        if (getY() != 0) sb.append("N").append(getY());
        if (getZ() != 0) sb.append("O").append(getZ());
        return sb.toString();
    }
    
    public static @Nonnull OffsetData create(final float x, final float y, final float z) {
        return new OffsetData(x, y, z);
    }
}
