package cn.minerealms.signpicture.entry.content;

import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.entry.IAsyncProcessable;
import cn.minerealms.signpicture.entry.ICollectable;
import cn.minerealms.signpicture.entry.IDivisionProcessable;
import cn.minerealms.signpicture.entry.IInitable;
import cn.minerealms.signpicture.state.Progressable;
import cn.minerealms.signpicture.state.State;
import cn.minerealms.signpicture.state.StateType;
import cn.minerealms.signpicture.util.Downloader;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 内容类
 * 管理单个图片的下载、加载和状态
 */
public class Content implements Progressable, IInitable, IAsyncProcessable, IDivisionProcessable, ICollectable {
    private final @Nonnull ContentId id;
    private final @Nonnull State state;
    private final @Nonnull ContentLocation location;
    
    private BufferedImage image;
    private File cacheFile;
    private int retryCount = 0;
    
    public Content(@Nonnull ContentId id, @Nonnull ContentLocation location) {
        this.id = id;
        this.location = location;
        this.state = new State().setName(id.getID());
    }
    
    @Override
    public @Nonnull State getState() {
        return this.state;
    }
    
    @Override
    public void onInit() {
        this.state.setType(StateType.INIT);
        this.cacheFile = this.location.getCacheFile(this.id.getID());
        
        // 检查缓存
        if (this.cacheFile.exists()) {
            this.state.setType(StateType.DOWNLOADED);
        } else {
            this.state.setType(StateType.INITIALIZED);
        }
    }
    
    @Override
    public void onAsyncProcess() throws Exception {
        if (this.id.isResource()) {
            // 资源文件，直接加载
            loadFromResource();
        } else {
            // 网络URL，需要下载
            if (!this.cacheFile.exists()) {
                download();
            }
        }
    }
    
    @Override
    public boolean onDivisionProcess() throws Exception {
        if (this.image == null && this.cacheFile.exists()) {
            loadFromCache();
            return true;
        }
        return this.image != null;
    }
    
    @Override
    public void onCollect() {
        if (this.image != null) {
            this.image.flush();
            this.image = null;
        }
    }
    
    private void download() throws Exception {
        if (this.retryCount >= Config.COMMON.contentMaxRetry.get()) {
            throw new RetryCountOverException("Max retry count exceeded");
        }
        
        this.state.setType(StateType.DOWNLOADING);
        this.retryCount++;
        
        // TODO: 实际下载逻辑
        // 使用Downloader下载到cacheFile
    }
    
    private void loadFromCache() throws Exception {
        this.state.setType(StateType.LOADING);
        this.image = ImageIO.read(this.cacheFile);
        
        if (this.image != null) {
            this.state.setType(StateType.LOADED);
        } else {
            throw new Exception("Failed to load image from cache");
        }
    }
    
    private void loadFromResource() throws Exception {
        this.state.setType(StateType.LOADING);
        // TODO: 从ResourceLocation加载
        this.state.setType(StateType.LOADED);
    }
    
    public BufferedImage getImage() {
        return this.image;
    }
    
    public boolean isAvailable() {
        return this.image != null && this.state.getType() == StateType.LOADED;
    }
}
