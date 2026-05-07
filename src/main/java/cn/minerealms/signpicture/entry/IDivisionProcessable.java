package cn.minerealms.signpicture.entry;

/**
 * 分段处理接口
 * 可以被多次调用，每次处理一部分
 */
public interface IDivisionProcessable {
    /**
     * 执行一次分段处理
     * @return 是否已完成
     */
    boolean onDivisionProcess() throws Exception;
}
