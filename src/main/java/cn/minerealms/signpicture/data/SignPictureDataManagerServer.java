package cn.minerealms.signpicture.data;

import cn.minerealms.signpicture.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端SignPicture数据管理器
 *
 * 职责：
 * - 管理 world/data/signpic/ 中的权威数据
 * - UUID生成
 * - 数据验证
 * - 持久化到存档
 *
 * 注意：仅在服务端运行！
 */
public class SignPictureDataManagerServer {

    public static final SignPictureDataManagerServer INSTANCE = new SignPictureDataManagerServer();

    // 内存缓存
    private final Map<String, SignPictureData> cache = new ConcurrentHashMap<>();

    // 数据目录（world/data/signpic/）
    private File dataDir;

    // UUID生成
    private static final String UUID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int UUID_LENGTH = 6;
    private static final Random random = new SecureRandom();

    private SignPictureDataManagerServer() {
    }

    /**
     * 初始化（服务端启动时调用）
     */
    public void init(@Nonnull MinecraftServer server) {
        try {
            // 获取存档目录
            File worldDir = server.getWorldPath(LevelResource.ROOT).toFile();
            File dataRoot = new File(worldDir, "data");
            this.dataDir = new File(dataRoot, "signpic");

            if (!this.dataDir.exists()) {
                this.dataDir.mkdirs();
            }

            Log.info("[Server] SignPictureDataManager initialized at: " + this.dataDir.getAbsolutePath());

            // 加载所有数据
            loadAll();

        } catch (Exception e) {
            Log.error("[Server] Failed to initialize SignPictureDataManager", e);
        }
    }

    /**
     * 生成唯一的UUID
     */
    @Nonnull
    public String generateUUID() {
        StringBuilder uuid = new StringBuilder(UUID_LENGTH);
        int attempts = 0;
        int maxAttempts = 100;

        do {
            uuid.setLength(0);
            for (int i = 0; i < UUID_LENGTH; i++) {
                uuid.append(UUID_CHARS.charAt(random.nextInt(UUID_CHARS.length())));
            }

            attempts++;
            if (attempts > maxAttempts) {
                throw new RuntimeException("Failed to generate unique UUID after " + maxAttempts + " attempts");
            }

        } while (exists(uuid.toString()));

        return uuid.toString();
    }

    /**
     * 创建新数据
     */
    @Nonnull
    public SignPictureData create(@Nonnull String uuid, @Nonnull SignPictureData data) {
        // 验证数据
        if (!validateData(data)) {
            throw new IllegalArgumentException("Invalid SignPictureData");
        }

        // 设置UUID
        data.setUuid(uuid);

        // 保存到缓存
        cache.put(uuid, data);

        // 持久化
        save(uuid);

        Log.info("[Server] Created SignPicture: " + uuid);
        return data;
    }

    /**
     * 获取数据
     */
    @Nullable
    public SignPictureData get(@Nonnull String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }

        // 检查缓存
        SignPictureData data = cache.get(uuid);
        if (data != null) {
            return data;
        }

        // 从文件加载
        data = load(uuid);
        if (data != null) {
            cache.put(uuid, data);
        }

        return data;
    }

    /**
     * 更新数据
     */
    public void update(@Nonnull String uuid, @Nonnull SignPictureData data) {
        // 验证数据
        if (!validateData(data)) {
            throw new IllegalArgumentException("Invalid SignPictureData");
        }

        data.touch();
        cache.put(uuid, data);
        save(uuid);

        Log.info("[Server] Updated SignPicture: " + uuid);
    }

    /**
     * 删除数据
     */
    public void delete(@Nonnull String uuid) {
        // 从缓存移除
        cache.remove(uuid);

        // 删除文件
        File file = getDataFile(uuid);
        if (file.exists()) {
            if (file.delete()) {
                Log.info("[Server] Deleted SignPicture: " + uuid);
            } else {
                Log.error("[Server] Failed to delete file: " + file.getAbsolutePath());
            }
        }
    }

    /**
     * 检查UUID是否存在
     */
    public boolean exists(@Nonnull String uuid) {
        return cache.containsKey(uuid) || getDataFile(uuid).exists();
    }

    /**
     * 验证数据
     */
    private boolean validateData(@Nonnull SignPictureData data) {
        if (data == null) {
            return false;
        }

        // 验证URL
        String url = data.getUrl();
        if (url == null || url.isEmpty()) {
            return false;
        }

        // 验证URL格式
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }

        // 验证属性范围
        if (data.getSizeWidth() < 0.01f || data.getSizeWidth() > 10.0f) {
            return false;
        }
        if (data.getSizeHeight() < 0.01f || data.getSizeHeight() > 10.0f) {
            return false;
        }

        return true;
    }

    /**
     * 保存到文件
     */
    private void save(@Nonnull String uuid) {
        try {
            SignPictureData data = cache.get(uuid);
            if (data == null) {
                Log.error("[Server] Cannot save null data for UUID: " + uuid);
                return;
            }

            File file = getDataFile(uuid);

            // 确保父目录存在
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 序列化并保存
            CompoundTag nbt = data.toNBT();
            NbtIo.writeCompressed(nbt, file);

            Log.debug("[Server] Saved data: " + file.getAbsolutePath());

        } catch (IOException e) {
            Log.error("[Server] Failed to save data: " + uuid, e);
        }
    }

    /**
     * 从文件加载
     */
    @Nullable
    private SignPictureData load(@Nonnull String uuid) {
        try {
            File file = getDataFile(uuid);
            if (!file.exists()) {
                return null;
            }

            CompoundTag nbt = NbtIo.readCompressed(file);
            SignPictureData data = SignPictureData.fromNBT(nbt);

            if (data != null) {
                Log.debug("[Server] Loaded data: " + file.getAbsolutePath());
            } else {
                Log.error("[Server] Failed to deserialize data: " + uuid);
            }

            return data;

        } catch (IOException e) {
            Log.error("[Server] Failed to load data: " + uuid, e);
            return null;
        }
    }

    /**
     * 获取数据文件
     */
    @Nonnull
    private File getDataFile(@Nonnull String uuid) {
        // 验证UUID格式
        if (!uuid.matches("[a-z0-9]{" + UUID_LENGTH + "}")) {
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
     * 加载所有数据
     */
    private void loadAll() {
        try {
            if (!dataDir.exists()) {
                return;
            }

            File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".dat"));
            if (files == null) {
                return;
            }

            int count = 0;
            for (File file : files) {
                String uuid = file.getName().replace(".dat", "");
                SignPictureData data = load(uuid);
                if (data != null) {
                    cache.put(uuid, data);
                    count++;
                }
            }

            Log.info("[Server] Loaded " + count + " SignPicture data files");

        } catch (Exception e) {
            Log.error("[Server] Failed to load all data", e);
        }
    }

    /**
     * 保存所有数据
     */
    public void saveAll() {
        try {
            int count = 0;
            for (String uuid : cache.keySet()) {
                save(uuid);
                count++;
            }
            Log.info("[Server] Saved " + count + " SignPicture data files");

        } catch (Exception e) {
            Log.error("[Server] Failed to save all data", e);
        }
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        cache.clear();
        Log.info("[Server] Cleared cache");
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * 获取所有UUID
     */
    @Nonnull
    public String[] getAllUUIDs() {
        return cache.keySet().toArray(new String[0]);
    }
}
