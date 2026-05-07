package cn.minerealms.signpicture.entry.content;

/**
 * 重试次数超限异常
 */
public class RetryCountOverException extends Exception {
    public RetryCountOverException() {
        super();
    }
    
    public RetryCountOverException(String message) {
        super(message);
    }
    
    public RetryCountOverException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RetryCountOverException(Throwable cause) {
        super(cause);
    }
}
