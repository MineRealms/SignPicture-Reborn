package cn.minerealms.signpicture.state;

import javax.annotation.Nonnull;

/**
 * 状态类型枚举
 * 定义内容加载的各个阶段
 */
public enum StateType {
    INIT("signpic.state.init"),
    INITIALIZED("signpic.state.initialized"),
    DOWNLOADING("signpic.state.downloading"),
    DOWNLOADED("signpic.state.downloaded"),
    LOADING("signpic.state.loading"),
    LOADED("signpic.state.loaded"),
    AVAILABLE("signpic.state.available"),
    ERROR("signpic.state.error"),
    ;
    
    public final @Nonnull String translationKey;
    
    StateType(final @Nonnull String translationKey) {
        this.translationKey = translationKey;
    }
}
