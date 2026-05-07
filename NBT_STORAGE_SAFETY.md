# NBT存储安全性分析

## 问题：NBT存储会不会损坏存档？

### 简短回答
**不会损坏存档**，但需要正确实现。

---

## NBT存储机制

### Minecraft的NBT系统
```java
// 告示牌的NBT结构
SignBlockEntity {
    // 原版数据
    Text1: "第一行文本"
    Text2: "第二行文本"
    Text3: "第三行文本"
    Text4: "第四行文本"
    
    // 自定义数据（使用PersistentData）
    ForgeData: {
        SignPictureURL: "https://example.com/image.png"
        SignPictureSizeW: 3.0f
        SignPictureSizeH: 3.0f
    }
}
```

### 关键点
1. **原版数据不受影响**：自定义NBT存储在`ForgeData`或`PublicBukkitValues`中
2. **向后兼容**：原版Minecraft会忽略不认识的NBT标签
3. **Forge保护**：Forge会保留所有NBT数据，即使mod未加载

---

## 三种情况分析

### 情况1：装了SignPicture mod
```
存档加载 → SignPicture读取NBT → 渲染图片
结果：✅ 完全正常工作
```

### 情况2：卸载SignPicture mod
```
存档加载 → 原版/Forge忽略SignPicture的NBT → 告示牌显示文本
结果：✅ 存档正常，告示牌恢复为普通告示牌
数据：NBT数据仍然保存在存档中（不会丢失）
```

### 情况3：重新安装SignPicture mod
```
存档加载 → SignPicture读取之前保存的NBT → 恢复图片显示
结果：✅ 图片重新显示，数据完整
```

---

## 对比：文本存储 vs NBT存储

### 文本存储（当前实现）
```
告示牌文本：
行1: "https://exampl"
行2: "e.com/image.pn"
行3: "g#w=3.0,h=3.0"
行4: ""

卸载mod后：
- 告示牌显示乱码URL
- 用户看到奇怪的文本
- 体验差 ❌
```

### NBT存储（推荐实现）
```
告示牌文本：
行1: "[SignPicture]"
行2: "Image loaded"
行3: ""
行4: ""

NBT数据：
ForgeData: {
    SignPictureURL: "https://example.com/image.png"
    SignPictureSizeW: 3.0f
}

卸载mod后：
- 告示牌显示 "[SignPicture]" 和 "Image loaded"
- 用户知道这是SignPicture的告示牌
- 体验好 ✅
```

---

## 实际案例：其他mod的NBT使用

### 案例1：Applied Energistics 2
```java
// ME终端存储大量数据在NBT中
METerminal {
    ForgeData: {
        ae2:items: [...]  // 物品列表
        ae2:fluids: [...]  // 流体列表
    }
}

卸载AE2后：
- 方块变成普通方块
- NBT数据保留
- 重装AE2后数据恢复
```

### 案例2：Tinkers' Construct
```java
// 工具存储属性在NBT中
ItemStack {
    tag: {
        tic_modifiers: [...]
        tic_stats: {...}
    }
}

卸载Tinkers后：
- 工具变成普通物品
- NBT数据保留
- 重装后工具恢复
```

### 案例3：Botania
```java
// 花存储魔力在NBT中
FlowerBlockEntity {
    ForgeData: {
        botania:mana: 1000
    }
}

卸载Botania后：
- 花变成普通方块
- NBT数据保留
```

**结论**：这是Minecraft modding的标准做法，非常安全。

---

## 安全实现指南

### ✅ 正确的实现
```java
// 1. 使用PersistentData（Forge推荐）
CompoundTag nbt = sign.getPersistentData();
nbt.putString("SignPictureURL", url);

// 2. 使用命名空间前缀
nbt.putString("signpicture:url", url);  // 避免与其他mod冲突

// 3. 向后兼容
private String extractUrl(SignBlockEntity sign) {
    CompoundTag nbt = sign.getPersistentData();
    
    // 优先从NBT读取
    if (nbt.contains("signpicture:url")) {
        return nbt.getString("signpicture:url");
    }
    
    // 降级：从文本读取（兼容旧版本）
    return extractTextUrl(sign);
}

// 4. 网络同步
@Override
public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
    super.onDataPacket(net, pkt);
    // Forge会自动同步PersistentData
}
```

### ❌ 错误的实现
```java
// 1. 直接修改原版NBT结构
sign.getTileData().putString("Text1", url);  // ❌ 破坏原版数据

// 2. 不使用命名空间
nbt.putString("url", url);  // ❌ 可能与其他mod冲突

// 3. 不考虑向后兼容
// 只从NBT读取，不支持文本URL  // ❌ 旧存档无法使用
```

---

## 迁移策略

### 阶段1：双模式支持（推荐）
```java
// 写入：同时写入NBT和文本
private void saveData(SignBlockEntity sign, String url, ...) {
    // 1. 写入NBT（新方式）
    CompoundTag nbt = sign.getPersistentData();
    nbt.putString("signpicture:url", url);
    nbt.putFloat("signpicture:size_w", sizeW);
    nbt.putFloat("signpicture:size_h", sizeH);
    
    // 2. 写入文本（旧方式，兼容）
    String[] lines = splitUrl(url, 4);
    updateSignText(sign, lines);
}

// 读取：优先NBT，降级到文本
private String loadUrl(SignBlockEntity sign) {
    CompoundTag nbt = sign.getPersistentData();
    
    // 优先从NBT读取
    if (nbt.contains("signpicture:url")) {
        return nbt.getString("signpicture:url");
    }
    
    // 降级：从文本读取
    return extractTextUrl(sign);
}
```

**优点**：
- 完全向后兼容
- 新旧版本都能工作
- 平滑过渡

### 阶段2：纯NBT模式（未来）
```java
// 只使用NBT，文本显示提示信息
private void saveData(SignBlockEntity sign, String url, ...) {
    // 1. 写入NBT
    CompoundTag nbt = sign.getPersistentData();
    nbt.putString("signpicture:url", url);
    // ... 其他属性
    
    // 2. 文本显示提示
    updateSignText(sign, new String[]{
        "[SignPicture]",
        "Image loaded",
        "",
        ""
    });
}
```

**优点**：
- 支持无限长URL
- 告示牌文本可以显示其他信息
- 数据结构清晰

---

## 风险评估

### 低风险 ✅
- **NBT数据丢失**：不会，Forge会保留
- **存档损坏**：不会，只是添加额外数据
- **与其他mod冲突**：不会，使用命名空间
- **原版兼容性**：完全兼容，原版会忽略

### 中风险 ⚠️
- **网络同步问题**：需要正确实现同步
- **数据迁移**：需要支持旧版本数据

### 高风险 ❌
- **直接修改原版NBT结构**：会破坏数据
- **不使用命名空间**：可能冲突

---

## 测试计划

### 测试1：基础功能
```
1. 安装SignPicture
2. 放置告示牌，设置图片
3. 保存并退出
4. 重新加载存档
5. 验证：图片正常显示 ✅
```

### 测试2：卸载mod
```
1. 安装SignPicture
2. 放置告示牌，设置图片
3. 保存并退出
4. 卸载SignPicture mod
5. 重新加载存档
6. 验证：
   - 存档正常加载 ✅
   - 告示牌显示文本（不是图片）✅
   - 没有错误日志 ✅
```

### 测试3：重新安装mod
```
1. 继续测试2的存档
2. 重新安装SignPicture mod
3. 重新加载存档
4. 验证：
   - 图片重新显示 ✅
   - 所有属性恢复 ✅
   - 数据完整 ✅
```

### 测试4：多人游戏同步
```
1. 服务器安装SignPicture
2. 玩家A放置告示牌，设置图片
3. 玩家B加入服务器
4. 验证：
   - 玩家B看到图片 ✅
   - 属性正确同步 ✅
```

### 测试5：版本升级
```
1. 使用旧版本（文本存储）创建告示牌
2. 升级到新版本（NBT存储）
3. 验证：
   - 旧告示牌仍然工作 ✅
   - 新告示牌使用NBT ✅
   - 数据迁移正确 ✅
```

---

## 结论

### NBT存储是否安全？
**是的，完全安全**，前提是正确实现。

### 推荐实现方案
1. **短期**：双模式支持（NBT + 文本）
2. **长期**：纯NBT模式

### 关键要点
- ✅ 使用`PersistentData`
- ✅ 使用命名空间前缀
- ✅ 向后兼容
- ✅ 正确的网络同步
- ❌ 不修改原版NBT结构

### 对比其他方案
| 方案 | 长度限制 | 兼容性 | 安全性 | 实现难度 |
|------|---------|--------|--------|---------|
| 文本存储 | 60字符 | ✅ 完美 | ✅ 安全 | ⭐ 简单 |
| URL缩短 | 无限制 | ✅ 完美 | ✅ 安全 | ⭐⭐ 中等 |
| 本地映射 | 无限制 | ⚠️ 需同步 | ✅ 安全 | ⭐⭐ 中等 |
| NBT存储 | 无限制 | ✅ 完美 | ✅ 安全 | ⭐⭐⭐ 复杂 |

### 最终建议
**使用NBT存储 + 向后兼容**，这是最佳长期方案。

---

## 参考资料

### Forge文档
- [BlockEntity NBT](https://docs.minecraftforge.net/en/latest/blockentities/bes/)
- [Data Serialization](https://docs.minecraftforge.net/en/latest/datastorage/saveddata/)

### 成功案例
- Applied Energistics 2
- Tinkers' Construct
- Botania
- Mekanism
- Thermal Expansion

所有这些大型mod都使用NBT存储，从未出现存档损坏问题。
