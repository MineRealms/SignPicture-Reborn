package cn.minerealms.signpicture.entry.content;

import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.entry.IAsyncProcessable;
import cn.minerealms.signpicture.entry.ICollectable;
import cn.minerealms.signpicture.entry.IDivisionProcessable;
import cn.minerealms.signpicture.entry.IInitable;
import cn.minerealms.signpicture.image.GifImage;
import cn.minerealms.signpicture.state.Progressable;
import cn.minerealms.signpicture.state.State;
import cn.minerealms.signpicture.state.StateType;
import cn.minerealms.signpicture.util.Downloader;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
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
    private GifImage gifImage;
    private File cacheFile;
    private int retryCount = 0;
    private boolean isGif = false;
    
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
        if (this.image == null && this.gifImage == null && this.cacheFile.exists()) {
            loadFromCache();
            return true;
        }
        return this.image != null || this.gifImage != null;
    }
    
    @Override
    public void onCollect() {
        if (this.image != null) {
            this.image.flush();
            this.image = null;
        }
        this.gifImage = null;
    }
    
    private void download() throws Exception {
        if (this.retryCount >= Config.COMMON.contentMaxRetry.get()) {
            throw new RetryCountOverException("Max retry count exceeded");
        }
        
        this.state.setType(StateType.DOWNLOADING);
        this.retryCount++;
        
        // 使用Downloader下载
        String url = this.id.getURI();
        Downloader downloader = new Downloader(
            Config.COMMON.communicateThreads.get(),
            Config.COMMON.communicateDLTimedout.get()
        );
        
        final boolean[] success = {false};
        downloader.download(url, new Downloader.DownloadCallback() {
            @Override
            public void onSuccess(InputStream stream, long contentLength) throws java.io.IOException {
                // 检查大小限制
                int maxSize = Config.COMMON.contentMaxByte.get();
                if (maxSize > 0 && contentLength > maxSize) {
                    throw new java.io.IOException("File too large: " + contentLength);
                }

                // 保存到缓存
                try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = stream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }

                state.setType(StateType.DOWNLOADED);
                success[0] = true;
            }

            @Override
            public void onError(Exception e) {
                state.setErrorMessage(e);
            }
        });
        
        // 等待下载完成
        Thread.sleep(100);
        if (!success[0] && this.retryCount < Config.COMMON.contentMaxRetry.get()) {
            download(); // 重试
        }
    }
    
    private void loadFromCache() throws Exception {
        this.state.setType(StateType.LOADING);
        
        // 检查是否是GIF
        String fileName = this.cacheFile.getName().toLowerCase();
        if (fileName.endsWith(".gif") || isGifFile(this.cacheFile)) {
            // 加载GIF
            try (FileInputStream fis = new FileInputStream(this.cacheFile)) {
                this.gifImage = GifImage.read(fis);
                this.isGif = true;
                this.state.setType(StateType.LOADED);
            }
        } else {
            // 加载普通图片
            this.image = ImageIO.read(this.cacheFile);
            
            if (this.image != null) {
                this.state.setType(StateType.LOADED);
            } else {
                throw new Exception("Failed to load image from cache");
            }
        }
    }
    
    private boolean isGifFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[3];
            if (fis.read(header) == 3) {
                return header[0] == 'G' && header[1] == 'I' && header[2] == 'F';
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }
    
    private void loadFromResource() throws Exception {
        this.state.setType(StateType.LOADING);
        // TODO: 从ResourceLocation加载
        this.state.setType(StateType.LOADED);
    }
    
    public BufferedImage getImage() {
        return this.image;
    }
    
    public GifImage getGifImage() {
        return this.gifImage;
    }
    
    public boolean isGif() {
        return this.isGif;
    }
    
    public boolean isAvailable() {
        return (this.image != null || this.gifImage != null) && this.state.getType() == StateType.LOADED;
    }
}
