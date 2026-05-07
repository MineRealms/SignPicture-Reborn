# 客户端/服务端逻辑分离设计

## 核心原则

**Minecraft是客户端-服务端架构，必须严格区分逻辑！**

---

## 逻辑分布

### 服务端（Server Side）
**职责**：数据权威、持久化、同步

```
服务端负责：
✅ 告示牌NBT数据的权威存储
✅ SignPictureData的持久化（world/data/signpic/）
✅ 数据验证和安全检查
✅ 网络包处理（接收客户端请求）
✅ 数据同步（广播给所有客户端）
❌ 不负责渲染
❌ 不负责纹理管理
❌ 不负责GUI
```

### 客户端（Client Side）
**职责**：渲染、GUI、本地缓存

```
客户端负责：
✅ GUI显示和交互
✅ 图片下载和本地缓存
✅ 纹理上传到GPU
✅ 渲染到世界
✅ 本地数据缓存（.minecraft/signpic/）
❌ 不是数据权威
❌ 不负责持久化到存档
❌ 不负责数据同步
```

---

## 数据流

### 创建SignPicture流程

```
[客户端] 用户放置告示牌
    ↓
[客户端] 打开GUI，输入URL和属性
    ↓
[客户端] 生成UUID（临时）
    ↓
[客户端] 创建SignPictureData对象
    ↓
[客户端] 发送CreateSignPicturePacket到服务端
    {
        pos: BlockPos
        url: String
        sizeWidth: float
        sizeHeight: float
        ...
    }
    ↓
[服务端] 接收数据包
    ↓
[服务端] 验证数据（URL合法性、属性范围等）
    ↓
[服务端] 生成权威UUID
    ↓
[服务端] 创建SignPictureData
    ↓
[服务端] 保存到 world/data/signpic/uuid.dat
    ↓
[服务端] 更新告示牌文本：
    行1: [SignPicture]
    行2: #uuid
    ↓
[服务端] 广播SyncSignPicturePacket给所有客户端
    {
        uuid: String
        data: SignPictureData
    }
    ↓
[客户端] 接收同步包
    ↓
[客户端] 保存到本地缓存 .minecraft/signpic/data/uuid.dat
    ↓
[客户端] 下载图片（如果需要）
    ↓
[客户端] 渲染
```

### 加载SignPicture流程（进入世界）

```
[客户端] 进入世界，渲染告示牌
    ↓
[客户端] SignHandlerV2.render() 检测到 [SignPicture]
    ↓
[客户端] 提取UUID
    ↓
[客户端] 检查本地缓存 .minecraft/signpic/data/uuid.dat
    ↓
    ├─ 存在 → 加载数据 → 渲染
    │
    └─ 不存在 → 发送RequestSignPicturePacket到服务端
                {
                    uuid: String
                }
                ↓
            [服务端] 接收请求
                ↓
            [服务端] 从 world/data/signpic/uuid.dat 加载
                ↓
            [服务端] 发送ResponseSignPicturePacket给请求的客户端
                {
                    uuid: String
                    data: SignPictureData
                }
                ↓
            [客户端] 接收响应
                ↓
            [客户端] 保存到本地缓存
                ↓
            [客户端] 下载图片
                ↓
            [客户端] 渲染
```

### 编辑SignPicture流程

```
[客户端] 用户右键告示牌
    ↓
[客户端] 提取UUID
    ↓
[客户端] 从本地缓存加载数据
    ↓
[客户端] 打开GUI，显示当前属性
    ↓
[客户端] 用户修改属性
    ↓
[客户端] 发送UpdateSignPicturePacket到服务端
    {
        uuid: String
        data: SignPictureData（新数据）
    }
    ↓
[服务端] 接收更新包
    ↓
[服务端] 验证数据
    ↓
[服务端] 更新 world/data/signpic/uuid.dat
    ↓
[服务端] 广播SyncSignPicturePacket给所有客户端
    ↓
[客户端] 接收同步包
    ↓
[客户端] 更新本地缓存
    ↓
[客户端] 重新渲染
```

### 删除SignPicture流程

```
[客户端] 用户破坏告示牌
    ↓
[客户端] 提取UUID
    ↓
[客户端] 发送DeleteSignPicturePacket到服务端
    {
        uuid: String
    }
    ↓
[服务端] 接收删除包
    ↓
[服务端] 删除 world/data/signpic/uuid.dat
    ↓
[服务端] 广播DeleteSignPicturePacket给所有客户端
    ↓
[客户端] 接收删除包
    ↓
[客户端] 从本地缓存删除（可选，保留也可以）
    ↓
[客户端] 清理纹理缓存
```

---

## 文件结构

### 服务端（存档目录）
```
world/
└── data/
    └── signpic/
        ├── a3f9c2.dat  # 权威数据
        ├── b7k2m9.dat
        └── ...
```

### 客户端（游戏目录）
```
.minecraft/
└── signpic/
    ├── data/           # 数据缓存（从服务端同步）
    │   ├── a3f9c2.dat
    │   ├── b7k2m9.dat
    │   └── ...
    └── cache/          # 图片缓存
        ├── ac2e72eeade0acb2a5bfc538c3e6d4aa
        └── ...
```

---

## 类的Side标注

### 服务端类
```java
// SignPictureDataManager（服务端版本）
@OnlyIn(Dist.DEDICATED_SERVER)  // 仅服务端
public class SignPictureDataManagerServer {
    // 管理 world/data/signpic/ 中的数据
    // 处理网络包
    // 数据验证
}
```

### 客户端类
```java
// SignPictureDataManager（客户端版本）
@OnlyIn(Dist.CLIENT)  // 仅客户端
public class SignPictureDataManagerClient {
    // 管理 .minecraft/signpic/data/ 中的缓存
    // 发送请求包
    // 接收同步包
}

// SignHandlerV2
@OnlyIn(Dist.CLIENT)  // 仅客户端
public class SignHandlerV2 {
    // 渲染逻辑
    // 纹理管理
}

// GUI相关
@OnlyIn(Dist.CLIENT)  // 仅客户端
public class GuiMainFull {
    // GUI逻辑
}
```

### 共享类
```java
// SignPictureData（数据模型）
// 无Side标注，客户端和服务端都使用
public class SignPictureData {
    // 纯数据类
    // NBT序列化/反序列化
}

// SignPictureHelper（工具类）
// 无Side标注，客户端和服务端都使用
public class SignPictureHelper {
    // 告示牌文本操作
    // UUID提取
}
```

---

## 网络包设计

### 1. CreateSignPicturePacket（客户端 → 服务端）
```java
public class CreateSignPicturePacket {
    private BlockPos pos;
    private String url;
    private float sizeWidth;
    private float sizeHeight;
    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float offsetX;
    private float offsetY;
    private float offsetZ;
    
    // 服务端处理
    public static void handle(CreateSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            
            // 1. 验证数据
            if (!validateData(packet)) {
                return;
            }
            
            // 2. 生成UUID
            String uuid = SignPictureDataManagerServer.INSTANCE.generateUUID();
            
            // 3. 创建数据
            SignPictureData data = new SignPictureData(uuid, packet.url);
            data.setSize(packet.sizeWidth, packet.sizeHeight);
            data.setRotation(packet.rotationX, packet.rotationY, packet.rotationZ);
            data.setOffset(packet.offsetX, packet.offsetY, packet.offsetZ);
            
            // 4. 保存到服务端
            SignPictureDataManagerServer.INSTANCE.save(uuid, data);
            
            // 5. 更新告示牌
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof SignBlockEntity sign) {
                SignPictureHelper.setSignPicture(sign, uuid);
                sign.setChanged();
                player.level().sendBlockUpdated(packet.pos, sign.getBlockState(), sign.getBlockState(), 3);
            }
            
            // 6. 广播给所有客户端
            SyncSignPicturePacket syncPacket = new SyncSignPicturePacket(uuid, data);
            NetworkHandler.INSTANCE.sendToAllClients(syncPacket);
        });
        ctx.get().setPacketHandled(true);
    }
}
```

### 2. SyncSignPicturePacket（服务端 → 客户端）
```java
public class SyncSignPicturePacket {
    private String uuid;
    private SignPictureData data;
    
    // 客户端处理
    public static void handle(SyncSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 保存到客户端本地缓存
            SignPictureDataManagerClient.INSTANCE.saveToCache(packet.uuid, packet.data);
        });
        ctx.get().setPacketHandled(true);
    }
}
```

### 3. RequestSignPicturePacket（客户端 → 服务端）
```java
public class RequestSignPicturePacket {
    private String uuid;
    
    // 服务端处理
    public static void handle(RequestSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            
            // 从服务端加载数据
            SignPictureData data = SignPictureDataManagerServer.INSTANCE.load(packet.uuid);
            if (data != null) {
                // 发送给请求的客户端
                ResponseSignPicturePacket response = new ResponseSignPicturePacket(packet.uuid, data);
                NetworkHandler.INSTANCE.sendToClient(response, player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
```

### 4. ResponseSignPicturePacket（服务端 → 客户端）
```java
public class ResponseSignPicturePacket {
    private String uuid;
    private SignPictureData data;
    
    // 客户端处理
    public static void handle(ResponseSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 保存到客户端本地缓存
            SignPictureDataManagerClient.INSTANCE.saveToCache(packet.uuid, packet.data);
        });
        ctx.get().setPacketHandled(true);
    }
}
```

### 5. UpdateSignPicturePacket（客户端 → 服务端）
```java
public class UpdateSignPicturePacket {
    private String uuid;
    private SignPictureData data;
    
    // 服务端处理
    public static void handle(UpdateSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            
            // 验证数据
            if (!validateData(packet.data)) {
                return;
            }
            
            // 更新服务端数据
            SignPictureDataManagerServer.INSTANCE.update(packet.uuid, packet.data);
            
            // 广播给所有客户端
            SyncSignPicturePacket syncPacket = new SyncSignPicturePacket(packet.uuid, packet.data);
            NetworkHandler.INSTANCE.sendToAllClients(syncPacket);
        });
        ctx.get().setPacketHandled(true);
    }
}
```

### 6. DeleteSignPicturePacket（客户端 → 服务端 → 客户端）
```java
public class DeleteSignPicturePacket {
    private String uuid;
    
    // 服务端处理
    public static void handleServer(DeleteSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 删除服务端数据
            SignPictureDataManagerServer.INSTANCE.delete(packet.uuid);
            
            // 广播给所有客户端
            NetworkHandler.INSTANCE.sendToAllClients(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    
    // 客户端处理
    public static void handleClient(DeleteSignPicturePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 从客户端缓存删除（可选）
            SignPictureDataManagerClient.INSTANCE.deleteFromCache(packet.uuid);
            
            // 清理纹理
            SignHandlerV2.INSTANCE.releaseTexture(packet.uuid);
        });
        ctx.get().setPacketHandled(true);
    }
}
```

---

## 关键点总结

### ✅ 正确的做法

1. **服务端是数据权威**
   - 所有数据修改必须经过服务端
   - 服务端验证所有数据
   - 服务端负责持久化到存档

2. **客户端只负责渲染**
   - 客户端从服务端请求数据
   - 客户端缓存数据（加速）
   - 客户端不是数据权威

3. **网络同步**
   - 所有数据修改都要同步
   - 使用专门的网络包
   - 广播给所有客户端

4. **Side标注**
   - 使用@OnlyIn标注
   - 避免客户端代码在服务端运行
   - 避免服务端代码在客户端运行

### ❌ 错误的做法

1. **客户端直接修改数据**
   ```java
   // ❌ 错误：客户端直接保存
   SignPictureDataManager.INSTANCE.save(uuid, data);
   ```

2. **服务端执行渲染逻辑**
   ```java
   // ❌ 错误：服务端没有渲染
   SignHandler.render(...);
   ```

3. **混合客户端和服务端逻辑**
   ```java
   // ❌ 错误：没有区分Side
   public class SignPictureDataManager {
       public void save() {
           // 这是服务端还是客户端？
       }
   }
   ```

4. **不同步数据**
   ```java
   // ❌ 错误：只在服务端修改，不广播
   data.setSize(2.0f, 2.0f);
   // 其他客户端看不到变化！
   ```

---

## 实现检查清单

- [ ] SignPictureData：纯数据类，无Side依赖
- [ ] SignPictureHelper：工具类，无Side依赖
- [ ] SignPictureDataManagerServer：服务端数据管理
- [ ] SignPictureDataManagerClient：客户端缓存管理
- [ ] SignHandlerV2：客户端渲染，标注@OnlyIn(Dist.CLIENT)
- [ ] GUI类：客户端，标注@OnlyIn(Dist.CLIENT)
- [ ] 网络包：6个包，正确的处理逻辑
- [ ] 网络注册：正确注册所有包
- [ ] 测试：单人游戏
- [ ] 测试：多人游戏同步

---

## 下一步

需要重新实现：
1. 拆分DataManager为Server和Client版本
2. 实现所有网络包
3. 正确标注@OnlyIn
4. 测试客户端-服务端同步

这是一个**完全重构**，但是**必须的**，否则多人游戏会出问题！
