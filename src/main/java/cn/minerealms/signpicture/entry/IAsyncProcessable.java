package cn.minerealms.signpicture.entry;

/**
 * 异步处理接口
 * 一次性调用，立即执行处理
 */
public interface IAsyncProcessable {
    void onAsyncProcess() throws Exception;
}
