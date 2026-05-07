package cn.minerealms.signpicture;

/**
 * 加载取消异常
 */
public class LoadCanceledException extends Exception {
    public LoadCanceledException() {
        super();
    }
    
    public LoadCanceledException(String message) {
        super(message);
    }
    
    public LoadCanceledException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public LoadCanceledException(Throwable cause) {
        super(cause);
    }
}
