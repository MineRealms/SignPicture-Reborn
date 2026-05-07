package cn.minerealms.signpicture.attr;

import javax.annotation.Nonnull;

/**
 * 可组合为字符串的属性接口
 */
public interface IPropComposable {
    @Nonnull
    String compose();
}
