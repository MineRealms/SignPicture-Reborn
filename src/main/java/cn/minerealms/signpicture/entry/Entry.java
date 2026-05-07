package cn.minerealms.signpicture.entry;

import cn.minerealms.signpicture.Log;
import cn.minerealms.signpicture.entry.content.Content;
import cn.minerealms.signpicture.entry.content.ContentId;
import cn.minerealms.signpicture.state.Progressable;
import cn.minerealms.signpicture.state.State;

import javax.annotation.Nonnull;

/**
 * Entry类
 * 连接告示牌文本和图片内容
 */
public class Entry implements Progressable, ITickEntry {
    private final @Nonnull EntryId id;
    private final @Nonnull Content content;
    private final @Nonnull State state;

    private int tickCount = 0;
    private long lastAccessTime = System.currentTimeMillis();
    private int currentFrame = 0;

    public Entry(@Nonnull EntryId id, @Nonnull ContentId contentId) {
        this.id = id;
        this.content = contentId.content();
        this.state = content.getState();
    }

    @Override
    public @Nonnull State getState() {
        return this.state;
    }

    @Override
    public void onTick() {
        this.tickCount++;

        // 更新动画帧（如果是GIF）
        if (content.isAnimated()) {
            int frameCount = content.getFrameCount();
            if (frameCount > 1) {
                // 每10 tick切换一帧（约0.5秒）
                if (tickCount % 10 == 0) {
                    currentFrame = (currentFrame + 1) % frameCount;
                    Log.debug("Entry " + id + " frame: " + currentFrame + "/" + frameCount);
                }
            }
        }
    }

    public @Nonnull Content getContent() {
        // 更新最后访问时间
        this.lastAccessTime = System.currentTimeMillis();
        return this.content;
    }

    public @Nonnull EntryId getId() {
        return this.id;
    }

    public int getTickCount() {
        return this.tickCount;
    }

    public long getLastAccessTime() {
        return this.lastAccessTime;
    }

    public int getCurrentFrame() {
        return this.currentFrame;
    }

    /**
     * 检查Entry是否应该被GC
     * @param maxIdleTime 最大空闲时间（毫秒）
     * @return true如果应该被回收
     */
    public boolean shouldCollect(long maxIdleTime) {
        long idleTime = System.currentTimeMillis() - lastAccessTime;
        return idleTime > maxIdleTime;
    }
}
