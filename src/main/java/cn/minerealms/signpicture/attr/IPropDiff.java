package cn.minerealms.signpicture.attr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 可计算差异的属性接口
 */
public interface IPropDiff<Diffed, Base> {
    @Nonnull
    Diffed diff(@Nullable Base base);
}
