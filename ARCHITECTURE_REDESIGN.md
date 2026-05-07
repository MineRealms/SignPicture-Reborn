# SignPicture 架构重构设计文档

## 核心设计理念

### 告示牌显示
```
行1: [SignPicture]
行2: #a3f9c2
行3: (空)
行4: (空)
```

### 数据存储
```
.minecraft/signpic/data/a3f9c2.dat (NBT文件)
{
    uuid: "a3f9c2"
    url: "https://example.com/very/long/url/to/image.png"
    sizeWidth: 3.0f
    sizeHeight: 3.0f
    rotationX: 0.0f
    rotationY: 90.0f
    rotationZ: 0.0f
    offsetX: 0.1f
    offsetY: 0.2f
    offsetZ: 0.0f
    cacheFile: "ac2e72eeade0acb2a5bfc538c3e6d4aa"
    createdTime: 1234567890
    lastModified: 1234567890
}
```

---

## 架构层次

```
┌─────────────────────────────────────────┐
│         用户界面层 (GUI)                 │
│  GuiMainFull, GuiSize, GuiRotation...   │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       数据管理层 (DataManager)           │
│  SignPictureDataManager                 │
│  - UUID生成                             │
│  - NBT序列化/反序列化                    │
│  - 数据CRUD操作                         │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       存储层 (Storage)                   │
│  - NBT文件读写                          │
│  - 缓存管理                             │
│  - 数据持久化                           │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       渲染层 (Renderer)                  │
│  SignHandler, TextureManager            │
│  - 纹理加载                             │
│  - 图片渲染                             │
└─────────────────────────────────────────┘
```

---

## 核心类设计

### 1. SignPictureData (数据模型)
```java
public class SignPictureData {
    // 唯一标识符（6位短UUID）
    private String uuid;
    
    // 图片信息
    private String url;
    private String cacheFile;  // MD5 hash
    
    // 渲染属性
    private float sizeWidth = 1.0f;
    private float sizeHeight = 1.0f;
    private float rotationX = 0.0f;
    private float rotationY = 0.0f;
    private float rotationZ = 0.0f;
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float offsetZ = 0.0f;
    
    // 元数据
    private long createdTime;
    private long lastModified;
    
    // NBT序列化
    public CompoundTag toNBT();
    public static SignPictureData fromNBT(CompoundTag nbt);
}
```

### 2. SignPictureDataManager (数据管理器)
```java
public class SignPictureDataManager {
    // 单例
    public static final SignPictureDataManager INSTANCE;
    
    // 内存缓存
    private Map<String, SignPictureData> cache;
    
    // 数据目录
    private File dataDir;
    
    // 核心方法
    public String create(String url);  // 创建新数据，返回UUID
    public SignPictureData get(String uuid);  // 获取数据
    public void update(String uuid, SignPictureData data);  // 更新数据
    public void delete(String uuid);  // 删除数据
    
    // UUID生成
    private String generateUUID();  // 生成6位短UUID
    
    // 持久化
    public void save(String uuid);  // 保存到文件
    public void load(String uuid);  // 从文件加载
    public void saveAll();  // 保存所有
    public void loadAll();  // 加载所有
}
```

### 3. SignPictureHelper (工具类)
```java
public class SignPictureHelper {
    // 告示牌操作
    public static void setSignPicture(SignBlockEntity sign, String uuid);
    public static String getSignPictureUUID(SignBlockEntity sign);
    public static boolean isSignPicture(SignBlockEntity sign);
    
    // 文本格式化
    public static String[] formatSignText(String uuid);
    public static String extractUUID(String[] lines);
}
```

---

## UUID设计

### 格式
- **长度**：6位字符
- **字符集**：`[a-z0-9]`（36种字符）
- **总数**：36^6 = 2,176,782,336 种组合
- **碰撞概率**：极低（生日悖论：1%碰撞需要约46,000个UUID）

### 生成算法
```java
private String generateUUID() {
    String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
    Random random = new SecureRandom();
    StringBuilder uuid = new StringBuilder(6);
    
    for (int i = 0; i < 6; i++) {
        uuid.append(chars.charAt(random.nextInt(chars.length())));
    }
    
    // 检查碰撞
    while (exists(uuid.toString())) {
        uuid.setLength(0);
        for (int i = 0; i < 6; i++) {
            uuid.append(chars.charAt(random.nextInt(chars.length())));
        }
    }
    
    return uuid.toString();
}
```

---

## 文件结构

```
.minecraft/
└── signpic/
    ├── data/           # NBT数据文件
    │   ├── a3f9c2.dat
    │   ├── b7k2m9.dat
    │   └── ...
    ├── cache/          # 图片缓存
    │   ├── ac2e72eeade0acb2a5bfc538c3e6d4aa
    │   └── ...
    └── index.dat       # 索引文件（可选）
```

---

## 工作流程

### 创建SignPicture
```
1. 用户放置告示牌
2. 打开GUI，输入URL和属性
3. 点击Done
   ├─ DataManager.create(url) → 生成UUID "a3f9c2"
   ├─ 创建SignPictureData对象
   ├─ 保存到 data/a3f9c2.dat
   └─ 更新告示牌文本：
       行1: [SignPicture]
       行2: #a3f9c2
4. 发送网络包到服务器
5. 服务器更新告示牌
```

### 渲染SignPicture
```
1. SignHandler.render(sign)
2. 检查告示牌文本
   ├─ 行1 == "[SignPicture]" ?
   └─ 提取UUID: "a3f9c2"
3. DataManager.get("a3f9c2")
   ├─ 内存缓存命中？返回
   └─ 否则从 data/a3f9c2.dat 加载
4. 获取URL和属性
5. 下载/加载图片
6. 应用属性渲染
```

### 编辑SignPicture
```
1. 用户右键告示牌
2. 提取UUID: "a3f9c2"
3. DataManager.get("a3f9c2")
4. 加载数据到GUI
5. 用户修改属性
6. 点击Done
   ├─ DataManager.update("a3f9c2", newData)
   └─ 保存到 data/a3f9c2.dat
7. 告示牌文本不变（UUID不变）
```

### 删除SignPicture
```
1. 用户破坏告示牌
2. 提取UUID: "a3f9c2"
3. DataManager.delete("a3f9c2")
   ├─ 删除 data/a3f9c2.dat
   └─ 从内存缓存移除
```

---

## 网络同步

### 数据包设计
```java
// 创建/更新SignPicture
public class SignPictureUpdatePacket {
    private BlockPos pos;
    private String uuid;
    private SignPictureData data;  // 完整数据
}

// 删除SignPicture
public class SignPictureDeletePacket {
    private String uuid;
}

// 请求数据（客户端 → 服务器）
public class SignPictureRequestPacket {
    private String uuid;
}

// 响应数据（服务器 → 客户端）
public class SignPictureResponsePacket {
    private String uuid;
    private SignPictureData data;
}
```

### 同步流程
```
客户端创建SignPicture:
1. 生成UUID和数据
2. 保存到本地 data/uuid.dat
3. 发送UpdatePacket到服务器
4. 服务器保存到 world/data/signpic/uuid.dat
5. 服务器广播给其他玩家

客户端加载SignPicture:
1. 渲染时发现UUID
2. 检查本地是否有 data/uuid.dat
3. 如果没有，发送RequestPacket到服务器
4. 服务器响应ResponsePacket
5. 客户端保存到本地并渲染
```

---

## 向后兼容

### 兼容旧版本（文本URL）
```java
public static String getSignPictureUUID(SignBlockEntity sign) {
    String line1 = sign.getFrontText().getMessage(0, false).getString();
    
    // 新格式：[SignPicture]
    if (line1.equals("[SignPicture]")) {
        String line2 = sign.getFrontText().getMessage(1, false).getString();
        return line2.replace("#", "").trim();
    }
    
    // 旧格式：直接是URL
    if (line1.startsWith("http://") || line1.startsWith("https://")) {
        // 迁移到新格式
        String fullUrl = extractOldFormatUrl(sign);
        String uuid = DataManager.INSTANCE.create(fullUrl);
        updateSignToNewFormat(sign, uuid);
        return uuid;
    }
    
    return null;
}
```

---

## 安全性考虑

### 1. UUID碰撞检测
```java
private String generateUUID() {
    String uuid;
    int attempts = 0;
    do {
        uuid = randomUUID();
        attempts++;
        if (attempts > 100) {
            throw new RuntimeException("Failed to generate unique UUID");
        }
    } while (exists(uuid));
    return uuid;
}
```

### 2. 文件访问控制
```java
private File getDataFile(String uuid) {
    // 验证UUID格式
    if (!uuid.matches("[a-z0-9]{6}")) {
        throw new IllegalArgumentException("Invalid UUID format");
    }
    
    // 防止路径遍历攻击
    File file = new File(dataDir, uuid + ".dat");
    if (!file.getCanonicalPath().startsWith(dataDir.getCanonicalPath())) {
        throw new SecurityException("Path traversal detected");
    }
    
    return file;
}
```

### 3. 数据验证
```java
public static SignPictureData fromNBT(CompoundTag nbt) {
    SignPictureData data = new SignPictureData();
    
    // 验证必需字段
    if (!nbt.contains("uuid") || !nbt.contains("url")) {
        throw new IllegalArgumentException("Missing required fields");
    }
    
    // 验证数据范围
    data.sizeWidth = Math.max(0.01f, Math.min(10.0f, nbt.getFloat("sizeWidth")));
    data.sizeHeight = Math.max(0.01f, Math.min(10.0f, nbt.getFloat("sizeHeight")));
    
    return data;
}
```

### 4. 并发安全
```java
public class SignPictureDataManager {
    private final ConcurrentHashMap<String, SignPictureData> cache;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public SignPictureData get(String uuid) {
        lock.readLock().lock();
        try {
            return cache.get(uuid);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void update(String uuid, SignPictureData data) {
        lock.writeLock().lock();
        try {
            cache.put(uuid, data);
            save(uuid);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

---

## 性能优化

### 1. 延迟加载
```java
// 只在需要时加载数据
public SignPictureData get(String uuid) {
    SignPictureData data = cache.get(uuid);
    if (data == null) {
        data = load(uuid);
        if (data != null) {
            cache.put(uuid, data);
        }
    }
    return data;
}
```

### 2. 批量保存
```java
// 定期批量保存，而不是每次修改都保存
private final Set<String> dirtyUUIDs = new HashSet<>();

public void markDirty(String uuid) {
    dirtyUUIDs.add(uuid);
}

public void saveAllDirty() {
    for (String uuid : dirtyUUIDs) {
        save(uuid);
    }
    dirtyUUIDs.clear();
}
```

### 3. 索引文件（可选）
```java
// index.dat 存储所有UUID列表，加快启动速度
{
    uuids: ["a3f9c2", "b7k2m9", ...]
    count: 2
}
```

---

## 测试计划

### 单元测试
- UUID生成和碰撞检测
- NBT序列化/反序列化
- 数据验证
- 文件操作

### 集成测试
- 创建SignPicture
- 编辑SignPicture
- 删除SignPicture
- 网络同步

### 兼容性测试
- 旧版本数据迁移
- 卸载mod后重装
- 多人游戏同步

---

## 迁移计划

### 阶段1：实现新架构（1周）
- SignPictureData
- SignPictureDataManager
- SignPictureHelper
- 网络包

### 阶段2：集成到现有系统（3天）
- 修改GUI使用新API
- 修改SignHandler使用新API
- 向后兼容旧格式

### 阶段3：测试和优化（3天）
- 单元测试
- 集成测试
- 性能优化

### 阶段4：文档和发布（1天）
- 用户文档
- 开发者文档
- 发布说明

---

## 优势总结

### vs 文本存储
- ✅ 无长度限制
- ✅ 支持更多属性
- ✅ 告示牌文本清晰

### vs 直接NBT存储
- ✅ 数据独立于告示牌
- ✅ 易于备份和分享
- ✅ 支持数据迁移

### vs URL缩短
- ✅ 完全离线工作
- ✅ 不依赖外部服务
- ✅ 数据完全可控

---

## 风险和缓解

### 风险1：UUID碰撞
**缓解**：碰撞检测 + 36^6种组合

### 风险2：数据文件损坏
**缓解**：NBT格式自带校验 + 备份机制

### 风险3：网络同步失败
**缓解**：重试机制 + 请求-响应模式

### 风险4：性能问题
**缓解**：内存缓存 + 延迟加载 + 批量保存

---

## 总结

这是一个**优雅、安全、可扩展**的架构设计：

- 🎯 **简洁**：告示牌只显示UUID
- 🔒 **安全**：完整的验证和并发控制
- 📦 **独立**：数据独立于告示牌
- 🚀 **高效**：缓存和延迟加载
- 🔄 **兼容**：支持旧版本迁移
- 🌐 **同步**：完整的网络同步机制

准备开始实现！
