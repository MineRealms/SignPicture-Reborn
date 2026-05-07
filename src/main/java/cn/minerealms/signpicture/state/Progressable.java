package cn.minerealms.signpicture.state;

import javax.annotation.Nonnull;

/**
 * 具有状态的对象接口
 */
public interface Progressable {
    @Nonnull
    State getState();
}
