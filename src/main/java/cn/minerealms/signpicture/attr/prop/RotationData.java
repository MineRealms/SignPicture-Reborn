package cn.minerealms.signpicture.attr.prop;

import cn.minerealms.signpicture.attr.IPropComposable;
import cn.minerealms.signpicture.attr.IPropInterpolatable;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 旋转数据
 * 使用四元数表示旋转
 */
public class RotationData implements IPropInterpolatable<RotationData>, IPropComposable {
    public static final @Nonnull RotationData DefaultRotation = new RotationData(0, 0, 0);
    
    private final float x;
    private final float y;
    private final float z;
    
    public RotationData(final float x, final float y, final float z) {
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
    public @Nonnull RotationData per() {
        return this;
    }
    
    @Override
    public @Nonnull RotationData per(final float per, final @Nullable RotationData before) {
        if (before != null) {
            // 简单线性插值，实际应该用四元数插值
            float nx = getX() * per + before.getX() * (1f - per);
            float ny = getY() * per + before.getY() * (1f - per);
            float nz = getZ() * per + before.getZ() * (1f - per);
            return new RotationData(nx, ny, nz);
        }
        return this;
    }
    
    @Override
    public @Nonnull String compose() {
        StringBuilder sb = new StringBuilder();
        if (getX() != 0) sb.append("X").append(getX());
        if (getY() != 0) sb.append("Y").append(getY());
        if (getZ() != 0) sb.append("Z").append(getZ());
        return sb.toString();
    }
    
    public static @Nonnull RotationData create(final float x, final float y, final float z) {
        return new RotationData(x, y, z);
    }
}
