package cn.minerealms.signpicture.entry.content;

/**
 * 内容容量超限异常
 */
public class ContentCapacityOverException extends Exception {
    public ContentCapacityOverException() {
        super();
    }
    
    public ContentCapacityOverException(String message) {
        super(message);
    }
    
    public ContentCapacityOverException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ContentCapacityOverException(Throwable cause) {
        super(cause);
    }
}
