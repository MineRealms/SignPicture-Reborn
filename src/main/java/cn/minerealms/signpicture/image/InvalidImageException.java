package cn.minerealms.signpicture.image;

/**
 * 无效图片异常
 */
public class InvalidImageException extends Exception {
    public InvalidImageException() {
        super();
    }
    
    public InvalidImageException(String message) {
        super(message);
    }
    
    public InvalidImageException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidImageException(Throwable cause) {
        super(cause);
    }
}
