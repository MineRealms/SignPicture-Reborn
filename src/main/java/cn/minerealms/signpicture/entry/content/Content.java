package cn.minerealms.signpicture.entry.content;

import cn.minerealms.signpicture.Config;
import cn.minerealms.signpicture.Log;
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

        String url = this.id.getURI();
        Log.debug("Downloading: " + url);

        // 使用同步下载
        try {
            java.net.URL urlObj = new java.net.URL(url);
            Log.debug("URL: " + url);

            // 设置代理 - 优先使用用户代理127.0.0.1:7890
            java.net.Proxy proxy = java.net.Proxy.NO_PROXY;

            // 硬编码常用代理端口，也可能用户没开代理所以用直接连接
            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");

            // 如果JVM没有设置代理，尝试常用代理地址
            if (proxyHost == null || proxyHost.isEmpty()) {
                // 尝试直连
                proxy = java.net.Proxy.NO_PROXY;
                Log.debug("Using direct connection (no proxy)");
            } else {
                try {
                    int port = Integer.parseInt(proxyPort);
                    proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(proxyHost, port));
                    Log.debug("Using proxy: " + proxyHost + ":" + proxyPort);
                } catch (Exception e) {
                    proxy = java.net.Proxy.NO_PROXY;
                }
            }

            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection(proxy);
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "image/png,image/jpeg,image/gif,image/webp,*/*");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new java.io.IOException("HTTP " + responseCode);
            }

            long contentLength = conn.getContentLengthLong();
            int maxSize = Config.COMMON.contentMaxByte.get();
            if (maxSize > 0 && contentLength > maxSize) {
                throw new java.io.IOException("File too large: " + contentLength);
            }

            // 确保父目录存在
            File parentDir = cacheFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new java.io.IOException("Failed to create cache directory: " + parentDir);
                }
            }

            // 下载并保存
            try (InputStream stream = conn.getInputStream();
                 FileOutputStream fos = new FileOutputStream(cacheFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            Log.debug("Download completed: " + cacheFile.getAbsolutePath());
            this.state.setType(StateType.DOWNLOADED);

        } catch (Exception e) {
            Log.debug("Download failed: " + url + " - " + e.getMessage());
            this.state.setErrorMessage(e);

            if (this.retryCount < Config.COMMON.contentMaxRetry.get()) {
                download();
            } else {
                throw e;
            }
        }
    }

    private void loadFromCache() throws Exception {
        this.state.setType(StateType.LOADING);

        Log.debug("Loading from cache: " + cacheFile.getAbsolutePath() + ", exists=" + cacheFile.exists() + ", size=" + cacheFile.length());

        // 检查是否是GIF
        String fileName = this.cacheFile.getName().toLowerCase();
        if (fileName.endsWith(".gif") || isGifFile(this.cacheFile)) {
            // 加载GIF
            try (FileInputStream fis = new FileInputStream(this.cacheFile)) {
                this.gifImage = GifImage.read(fis);
                this.isGif = true;
                this.state.setType(StateType.LOADED);
                Log.debug("Loaded GIF, frames: " + (this.gifImage != null ? this.gifImage.getFrameCount() : 0));
            }
        } else {
            // 加载普通图片
            this.image = ImageIO.read(this.cacheFile);

            Log.debug("ImageIO.read result: " + (this.image != null ? image.getWidth() + "x" + image.getHeight() : "null"));

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

    /**
     * 是否是动画（GIF）
     */
    public boolean isAnimated() {
        return this.isGif && this.gifImage != null;
    }

    /**
     * 获取动画帧数
     */
    public int getFrameCount() {
        if (this.gifImage != null) {
            return this.gifImage.getFrameCount();
        }
        return 1;
    }

    /**
     * 获取指定帧的图片
     */
    public BufferedImage getFrame(int frameIndex) {
        if (this.gifImage != null) {
            return this.gifImage.getFrame(frameIndex);
        }
        return this.image;
    }

    /**
     * 获取ContentId
     */
    public ContentId getId() {
        return this.id;
    }
}
