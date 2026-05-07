package cn.minerealms.signpicture.image;

import cn.minerealms.signpicture.lib.GifDecoder;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * GIF图片包装器
 * 支持动画帧管理
 */
public class GifImage {
    private final List<BufferedImage> frames = new ArrayList<>();
    private final List<Integer> delays = new ArrayList<>();
    private final int width;
    private final int height;
    
    public GifImage(GifDecoder.GifImage gifImage) {
        this.width = gifImage.getWidth();
        this.height = gifImage.getHeight();
        
        int frameCount = gifImage.getFrameCount();
        for (int i = 0; i < frameCount; i++) {
            frames.add(gifImage.getFrame(i));
            delays.add(gifImage.getDelay(i));
        }
    }
    
    public int getFrameCount() {
        return frames.size();
    }
    
    public BufferedImage getFrame(int index) {
        if (index < 0 || index >= frames.size()) {
            return frames.get(0);
        }
        return frames.get(index);
    }
    
    public int getDelay(int index) {
        if (index < 0 || index >= delays.size()) {
            return 100;
        }
        return delays.get(index);
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public boolean isAnimated() {
        return frames.size() > 1;
    }
    
    /**
     * 根据时间获取当前帧
     */
    public int getCurrentFrame(long tickCount) {
        if (!isAnimated()) {
            return 0;
        }
        
        long totalTime = 0;
        for (int delay : delays) {
            totalTime += delay;
        }
        
        if (totalTime == 0) {
            return 0;
        }
        
        long currentTime = (tickCount * 50) % totalTime; // 50ms per tick
        long accumulatedTime = 0;
        
        for (int i = 0; i < delays.size(); i++) {
            accumulatedTime += delays.get(i);
            if (currentTime < accumulatedTime) {
                return i;
            }
        }
        
        return 0;
    }
    
    public static GifImage read(@Nonnull InputStream stream) throws Exception {
        GifDecoder.GifImage gifImage = GifDecoder.read(stream);
        return new GifImage(gifImage);
    }
}
