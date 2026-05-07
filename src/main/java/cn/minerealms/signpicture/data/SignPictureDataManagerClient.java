package cn.minerealms.signpicture.data;

import cn.minerealms.signpicture.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端SignPicture数据管理器
 *
 * 职责：
 * - 管理 .minecraft/signpic/data/ 中的元数据缓存
 * - 从服务端接收元数据并缓存
 * - 提供元数据给渲染器
 *
 * 注意：
 * - 仅在客户端运行！
 * - 只管理元数据，不管理图片！
 * - 图片由ContentManager管理
 */
@OnlyIn(Dist.CLIENT)
public class SignPictureDataManagerClient {

    public static final SignPictureDataManagerClient INSTANCE = new SignPictureDataManagerClient();

    // 内存缓存（元数据）
    private final Map<String, SignPictureData> metadataCache = new ConcurrentHashMap<>();

    // 数据目录（.minecraft/signpic/data/）
    private File dataDir;

    private SignPictureDataManagerClient() {
    }

    /**
     * 初始化（客户端启动时调用）
     */
    public void init(@Nonnull File baseDir) {
        this.dataDir = new File(baseDir, "data");
        if (!this.dataDir.exists()) {
            this.dataDir.mkdirs();
        }

        Log.info("[Client] SignPictureDataManagerClient initialized at: " + this.dataDir.getAbsolutePath());
    }

    /**
     * 保存元数据到本地缓存
     * （从服务端同步时调用）
     */
    public void saveMetadata(@Nonnull String uuid, @Nonnull SignPictureData data) {
        try {
            // 保存到内存
            metadataCache.put(uuid, data);

            // 持久化到文件
            File file = getDataFile(uuid);

            // 确保父目录存在
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 序列化并保存
            CompoundTag nbt = data.toNBT();
            NbtIo.writeCompressed(nbt, file);

            Log.debug("[Client] Saved metadata: " + uuid);

        } catch (IOException e) {
            Log.error("[Client] Failed to save metadata: " + uuid, e);
        }
    }

    /**
     * 获取元数据
     * （渲染时调用）
     */
    @Nullable
    public SignPictureData getMetadata(@Nonnull String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }

        // 检查内存缓存
        SignPictureData data = metadataCache.get(uuid);
        if (data != null) {
            return data;
        }

        // 从文件加载
        data = loadMetadata(uuid);
        if (data != null) {
            metadataCache.put(uuid, data);
        }

        return data;
    }

    /**
     * 从文件加载元数据
     */
    @Nullable
    private SignPictureData loadMetadata(@Nonnull String uuid) {
        try {
            File file = getDataFile(uuid);
            if (!file.exists()) {
                return null;
            }

            CompoundTag nbt = NbtIo.readCompressed(file);
            SignPictureData data = SignPictureData.fromNBT(nbt);

            if (data != null) {
                Log.debug("[Client] Loaded metadata: " + uuid);
            } else {
                Log.error("[Client] Failed to deserialize metadata: " + uuid);
            }

            return data;

        } catch (IOException e) {
            Log.error("[Client] Failed to load metadata: " + uuid, e);
            return null;
        }
    }

    /**
     * 删除元数据（可选，通常保留缓存）
     */
    public void deleteMetadata(@Nonnull String uuid) {
        // 从内存移除
        metadataCache.remove(uuid);

        // 删除文件（可选）
        File file = getDataFile(uuid);
        if (file.exists()) {
            if (file.delete()) {
                Log.debug("[Client] Deleted metadata: " + uuid);
            }
        }
    }

    /**
     * 检查元数据是否存在
     */
    public boolean hasMetadata(@Nonnull String uuid) {
        return metadataCache.containsKey(uuid) || getDataFile(uuid).exists();
    }

    /**
     * 获取数据文件
     */
    @Nonnull
    private File getDataFile(@Nonnull String uuid) {
        // 验证UUID格式
        if (!uuid.matches("[a-z0-9]{6}")) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuid);
        }

        File file = new File(dataDir, uuid + ".dat");

        // 安全检查
        try {
            String canonicalDataDir = dataDir.getCanonicalPath();
            String canonicalFilePath = file.getCanonicalPath();
            if (!canonicalFilePath.startsWith(canonicalDataDir)) {
                throw new SecurityException("Path traversal detected: " + uuid);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to validate file path", e);
        }

        return file;
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        metadataCache.clear();
        Log.info("[Client] Cleared metadata cache");
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return metadataCache.size();
    }
}
