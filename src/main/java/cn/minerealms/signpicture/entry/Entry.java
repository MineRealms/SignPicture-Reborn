package cn.minerealms.signpicture.entry;

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
        // TODO: 更新动画帧
    }
    
    public @Nonnull Content getContent() {
        return this.content;
    }
    
    public @Nonnull EntryId getId() {
        return this.id;
    }
    
    public int getTickCount() {
        return this.tickCount;
    }
}
