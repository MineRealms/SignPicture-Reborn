package cn.minerealms.signpicture.entry.content;

/**
 * 内容被阻止异常
 */
public class ContentBlockedException extends Exception {
    public ContentBlockedException() {
        super();
    }
    
    public ContentBlockedException(String message) {
        super(message);
    }
    
    public ContentBlockedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ContentBlockedException(Throwable cause) {
        super(cause);
    }
}
