# 优化后的架构设计

## 核心原则

**服务端只存储元数据，客户端自己下载图片**

---

## 数据流（优化版）

### 创建SignPicture

```
[客户端] 用户在GUI输入URL和属性
    ↓
[客户端] 发送CreateSignPicturePacket到服务端
    {
        pos: BlockPos
        url: "https://example.com/image.png"
        sizeWidth: 3.0f
        sizeHeight: 3.0f
        rotationX: 0.0f
        rotationY: 90.0f
        rotationZ: 0.0f
        offsetX: 0.1f
        offsetY: 0.2f
        offsetZ: 0.0f
    }
    ↓
[服务端] 接收数据包
    ↓
[服务端] 验证数据（URL格式、属性范围）
    ↓
[服务端] 生成UUID: "a3f9c2"
    ↓
[服务端] 创建SignPictureData（只包含元数据）
    {
        uuid: "a3f9c2"
        url: "https://example.com/image.png"
        sizeWidth: 3.0f
        sizeHeight: 3.0f
        ...
    }
    ↓
[服务端] 保存到 world/data/signpic/a3f9c2.dat
    ↓
[服务端] 更新告示牌文本：
    行1: [SignPicture]
    行2: #a3f9c2
    ↓
[服务端] 广播SyncSignPicturePacket给所有客户端
    {
        uuid: "a3f9c2"
        url: "https://example.com/image.png"
        sizeWidth: 3.0f
        ...
    }
    ↓
[所有客户端] 接收同步包
    ↓
[所有客户端] 保存元数据到本地 .minecraft/signpic/data/a3f9c2.dat
    ↓
[所有客户端] **各自下载图片**到 .minecraft/signpic/cache/[md5]
    ↓
[所有客户端] 渲染
```

### 加载SignPicture（进入世界）

```
[客户端] 渲染告示牌，发现 [SignPicture] #a3f9c2
    ↓
[客户端] 检查本地元数据 .minecraft/signpic/data/a3f9c2.dat
    ↓
    ├─ 存在 → 读取URL和属性
    │           ↓
    │       检查图片缓存 .minecraft/signpic/cache/[md5]
    │           ↓
    │           ├─ 存在 → 直接渲染
    │           └─ 不存在 → 下载图片 → 渲染
    │
    └─ 不存在 → 发送RequestSignPicturePacket到服务端
                    ↓
                [服务端] 从 world/data/signpic/a3f9c2.dat 读取
                    ↓
                [服务端] 发送ResponseSignPicturePacket
                    {
                        uuid: "a3f9c2"
                        url: "https://example.com/image.png"
                        ...
                    }
                    ↓
                [客户端] 保存元数据到本地
                    ↓
                [客户端] 下载图片
                    ↓
                [客户端] 渲染
```

---

## 文件结构（优化版）

### 服务端（存档）
```
world/
└── data/
    └── signpic/
        ├── a3f9c2.dat  # 只包含元数据（URL+属性）
        │   {
        │       uuid: "a3f9c2"
        │       url: "https://example.com/image.png"
        │       sizeWidth: 3.0f
        │       ...
        │   }
        ├── b7k2m9.dat
        └── ...
```

### 客户端（游戏目录）
```
.minecraft/
└── signpic/
    ├── data/           # 元数据缓存（从服务端同步）
    │   ├── a3f9c2.dat  # 元数据（URL+属性）
    │   ├── b7k2m9.dat
    │   └── ...
    │
    └── cache/          # 图片缓存（客户端自己下载）
        ├── ac2e72eeade0acb2a5bfc538c3e6d4aa  # 图片文件（URL的MD5）
        ├── f3b8d9c1a2e4f5a6b7c8d9e0f1a2b3c4
        └── ...
```

---

## 优势

### 1. 服务端轻量
- ✅ 只存储元数据（几KB）
- ✅ 不存储图片（可能几MB）
- ✅ 不处理图片下载
- ✅ 带宽占用小

### 2. 客户端独立
- ✅ 各自下载图片
- ✅ 本地缓存管理
- ✅ 不重复下载
- ✅ 离线可用（如果有缓存）

### 3. 多人游戏友好
- ✅ 服务端不需要下载所有图片
- ✅ 客户端只下载自己看到的图片
- ✅ 减少服务端压力

---

## 关键代码优化

### SignPictureData（元数据）
```java
public class SignPictureData {
    // 元数据
    private String uuid;
    private String url;  // 只存URL，不存图片！
    
    // 渲染属性
    private float sizeWidth;
    private float sizeHeight;
    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float offsetX;
    private float offsetY;
    private float offsetZ;
    
    // 时间戳
    private long createdTime;
    private long lastModified;
    
    // ❌ 不包含图片数据
    // ❌ 不包含缓存文件路径
}
```

### 服务端DataManager
```java
public class SignPictureDataManagerServer {
    // ✅ 只管理元数据
    public SignPictureData create(String uuid, SignPictureData data) {
        // 验证URL格式
        // 保存元数据到 world/data/signpic/uuid.dat
        // ❌ 不下载图片
    }
    
    // ❌ 没有图片相关方法
}
```

### 客户端DataManager
```java
public class SignPictureDataManagerClient {
    // ✅ 管理元数据缓存
    public void saveMetadata(String uuid, SignPictureData data) {
        // 保存到 .minecraft/signpic/data/uuid.dat
    }
    
    public SignPictureData getMetadata(String uuid) {
        // 从 .minecraft/signpic/data/uuid.dat 读取
    }
    
    // ❌ 不管理图片缓存（由ContentManager负责）
}
```

### 客户端渲染流程
```java
public class SignHandlerV2 {
    public void render(SignBlockEntity sign, ...) {
        // 1. 提取UUID
        String uuid = SignPictureHelper.getUUID(sign);
        
        // 2. 获取元数据
        SignPictureData data = SignPictureDataManagerClient.INSTANCE.getMetadata(uuid);
        if (data == null) {
            // 请求服务端
            requestFromServer(uuid);
            return;
        }
        
        // 3. 获取URL
        String url = data.getUrl();
        
        // 4. 使用现有的ContentManager下载图片
        ContentId contentId = ContentId.from(url);
        Content content = ContentManager.instance.get(contentId);
        
        // 5. 获取图片
        BufferedImage image = content.getImage();
        if (image == null) {
            return; // 还在下载中
        }
        
        // 6. 渲染（应用data中的属性）
        renderImage(image, data);
    }
}
```

---

## 网络包（优化版）

### 1. CreateSignPicturePacket（客户端 → 服务端）
```java
// 只发送元数据
{
    pos: BlockPos
    url: String
    sizeWidth: float
    sizeHeight: float
    rotationX: float
    rotationY: float
    rotationZ: float
    offsetX: float
    offsetY: float
    offsetZ: float
}
```

### 2. SyncSignPicturePacket（服务端 → 客户端）
```java
// 只同步元数据
{
    uuid: String
    url: String
    sizeWidth: float
    sizeHeight: float
    rotationX: float
    rotationY: float
    rotationZ: float
    offsetX: float
    offsetY: float
    offsetZ: float
}

// 客户端处理
public static void handle(SyncSignPicturePacket packet, ...) {
    // 1. 保存元数据
    SignPictureData data = new SignPictureData(packet.uuid, packet.url);
    data.setSize(packet.sizeWidth, packet.sizeHeight);
    data.setRotation(packet.rotationX, packet.rotationY, packet.rotationZ);
    data.setOffset(packet.offsetX, packet.offsetY, packet.offsetZ);
    
    SignPictureDataManagerClient.INSTANCE.saveMetadata(packet.uuid, data);
    
    // 2. 触发图片下载（使用现有的ContentManager）
    ContentId contentId = ContentId.from(packet.url);
    ContentManager.instance.get(contentId); // 自动开始下载
}
```

---

## 复用现有系统

### ContentManager（已有）
```java
// ✅ 已经实现了图片下载和缓存
// ✅ 已经管理 .minecraft/signpic/cache/
// ✅ 已经支持异步下载
// ✅ 已经支持GIF动画

// 直接复用！
ContentId contentId = ContentId.from(url);
Content content = ContentManager.instance.get(contentId);
BufferedImage image = content.getImage();
```

### EntryManager（已有）
```java
// ✅ 已经实现了Entry管理
// ✅ 已经支持GIF动画帧切换
// ✅ 已经支持垃圾回收

// 直接复用！
EntryId entryId = EntryId.from(sign.getBlockPos().toString());
ContentId contentId = ContentId.from(url);
Entry entry = EntryManager.instance.get(entryId, contentId);
```

---

## 总结

### 服务端职责
1. 存储元数据（UUID + URL + 属性）
2. 数据验证
3. 同步给客户端
4. ❌ **不下载图片**
5. ❌ **不管理图片缓存**

### 客户端职责
1. 接收元数据
2. 缓存元数据到本地
3. **自己下载图片**（使用现有ContentManager）
4. **管理图片缓存**（使用现有ContentManager）
5. 渲染

### 优势
- ✅ 服务端轻量（只存元数据）
- ✅ 客户端独立（各自下载）
- ✅ 复用现有系统（ContentManager/EntryManager）
- ✅ 多人游戏友好
- ✅ 离线可用（有缓存时）

这样设计更合理！
