package cn.minerealms.signpicture.attr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 可插值的属性接口
 * 用于动画帧之间的平滑过渡
 */
public interface IPropInterpolatable<InterFrame> {
    @Nonnull
    InterFrame per();
    
    @Nonnull
    InterFrame per(float per, @Nullable InterFrame before);
}
