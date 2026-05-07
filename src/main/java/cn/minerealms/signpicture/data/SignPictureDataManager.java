package cn.minerealms.signpicture.data;

import cn.minerealms.signpicture.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * SignPicture数据管理器
 * 负责UUID生成、数据CRUD、持久化
 * 线程安全的单例实现
 */
public class SignPictureDataManager {

    public static final SignPictureDataManager INSTANCE = new SignPictureDataManager();

    // 内存缓存
    private final Map<String, SignPictureData> cache = new ConcurrentHashMap<>();

    // 读写锁
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // 数据目录
    private File dataDir;

    // UUID字符集
    private static final String UUID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int UUID_LENGTH = 6;
    private static final Random random = new SecureRandom();

    private SignPictureDataManager() {
    }

    /**
     * 初始化数据管理器
     */
    public void init(@Nonnull File baseDir) {
        this.dataDir = new File(baseDir, "data");
        if (!this.dataDir.exists()) {
            this.dataDir.mkdirs();
        }
        Log.info("SignPictureDataManager initialized at: " + this.dataDir.getAbsolutePath());
    }

    /**
     * 创建新的SignPicture数据
     * @param url 图片URL
     * @return UUID
     */
    @Nonnull
    public String create(@Nonnull String url) {
        lock.writeLock().lock();
        try {
            // 生成唯一UUID
            String uuid = generateUUID();

            // 创建数据对象
            SignPictureData data = new SignPictureData(uuid, url);

            // 保存到缓存
            cache.put(uuid, data);

            // 持久化到文件
            save(uuid);

            Log.debug("Created SignPicture: " + uuid + " for URL: " + url);
            return uuid;

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取SignPicture数据
     * @param uuid UUID
     * @return 数据对象，如果不存在返回null
     */
    @Nullable
    public SignPictureData get(@Nonnull String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }

        lock.readLock().lock();
        try {
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

        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 更新SignPicture数据
     * @param uuid UUID
     * @param data 新数据
     */
    public void update(@Nonnull String uuid, @Nonnull SignPictureData data) {
        lock.writeLock().lock();
        try {
            data.touch();
            cache.put(uuid, data);
            save(uuid);
            Log.debug("Updated SignPicture: " + uuid);

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 删除SignPicture数据
     * @param uuid UUID
     */
    public void delete(@Nonnull String uuid) {
        lock.writeLock().lock();
        try {
            // 从缓存移除
            cache.remove(uuid);

            // 删除文件
            File file = getDataFile(uuid);
            if (file.exists()) {
                if (file.delete()) {
                    Log.debug("Deleted SignPicture: " + uuid);
                } else {
                    Log.error("Failed to delete file: " + file.getAbsolutePath());
                }
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 检查UUID是否存在
     */
    public boolean exists(@Nonnull String uuid) {
        lock.readLock().lock();
        try {
            return cache.containsKey(uuid) || getDataFile(uuid).exists();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 生成唯一的UUID
     */
    @Nonnull
    private String generateUUID() {
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
     * 保存数据到文件
     */
    private void save(@Nonnull String uuid) {
        try {
            SignPictureData data = cache.get(uuid);
            if (data == null) {
                Log.error("Cannot save null data for UUID: " + uuid);
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

            Log.debug("Saved SignPicture data: " + file.getAbsolutePath());

        } catch (IOException e) {
            Log.error("Failed to save SignPicture data: " + uuid, e);
        }
    }

    /**
     * 从文件加载数据
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
                Log.debug("Loaded SignPicture data: " + file.getAbsolutePath());
            } else {
                Log.error("Failed to deserialize SignPicture data: " + uuid);
            }

            return data;

        } catch (IOException e) {
            Log.error("Failed to load SignPicture data: " + uuid, e);
            return null;
        }
    }

    /**
     * 获取数据文件
     */
    @Nonnull
    private File getDataFile(@Nonnull String uuid) {
        // 验证UUID格式（防止路径遍历攻击）
        if (!uuid.matches("[a-z0-9]{" + UUID_LENGTH + "}")) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuid);
        }

        File file = new File(dataDir, uuid + ".dat");

        // 安全检查：确保文件在数据目录内
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
     * 保存所有缓存的数据
     */
    public void saveAll() {
        lock.readLock().lock();
        try {
            int count = 0;
            for (String uuid : cache.keySet()) {
                save(uuid);
                count++;
            }
            Log.info("Saved " + count + " SignPicture data files");

        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 加载所有数据文件
     */
    public void loadAll() {
        lock.writeLock().lock();
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

            Log.info("Loaded " + count + " SignPicture data files");

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 清理所有缓存
     */
    public void clearCache() {
        lock.writeLock().lock();
        try {
            cache.clear();
            Log.info("Cleared SignPicture data cache");
        } finally {
            lock.writeLock().unlock();
        }
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
        lock.readLock().lock();
        try {
            return cache.keySet().toArray(new String[0]);
        } finally {
            lock.readLock().unlock();
        }
    }
}
