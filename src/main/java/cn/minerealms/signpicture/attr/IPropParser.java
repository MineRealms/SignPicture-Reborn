package cn.minerealms.signpicture.attr;

import javax.annotation.Nonnull;

/**
 * 可解析的属性接口
 */
public interface IPropParser extends IPropComposable {
    boolean parse(@Nonnull String src, @Nonnull String key, @Nonnull String value);
}
