# SignPicture 当前问题总结

## 问题1: 文件缓存路径问题 ❌ 未解决

### 错误信息
```
java.io.IOException: Failed to create cache directory: 
G:\MinecraftGames\GregTech Oh my shit\.minecraft\versions\GregTech Odyssey\signpic\cache\_https_www.mcmod.cn\pages\class\images
```

### 根本原因
- `ContentLocation.java` 使用URL字符串直接作为文件名/路径
- URL中的特殊字符（`$`, `/`, `\`, `:` 等）导致跨平台兼容性问题
- 当前尝试替换特殊字符的方案不可靠

### 正确解决方案
**使用MD5/SHA-256 hash作为文件名**
- 将URL hash成固定长度的十六进制字符串
- 确保全平台兼容（Windows/Linux/macOS）
- 避免文件名过长问题
- 避免特殊字符问题

### 需要修改的文件
- `src/main/java/cn/minerealms/signpicture/entry/content/ContentLocation.java`
  - `getCacheFile()` 方法：使用 `MessageDigest.getInstance("MD5")` 计算hash
  - `getMetaFile()` 方法：同样使用hash

### 示例代码
```java
private String toHash(String url) {
    try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(url.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    } catch (Exception e) {
        // fallback
        return url.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

public File getCacheFile(String id) {
    return new File(cacheDir, toHash(id));
}
```

---

## 问题2: 日志输出混乱 ⚠️

### 错误信息
```
[SignPicture-Download-0/ERROR]: [SignPicture] Download failed: https://7] [SignPicture-Download-0/ERROR]: [SignPicture] Download failed: https://www.mcmod.cn/pages/class/images/none.jpgjava.io.IOException: Failed to create cache directory...
```

### 原因
- 错误日志被截断或拼接
- 可能是多线程日志输出冲突
- URL被错误解析

---

## 问题3: UpdateSignPacket 反射问题 ✅ 已修复

### 原问题
```
java.lang.NoSuchFieldException: frontText
```

### 解决方案
改用 `SignBlockEntity.updateText()` 公共API，不再使用反射

---

## 已完成的TODO项目 ✅

1. ✅ 实现HTTP上传（Imgur）
2. ✅ 实现URL缩短（Bitly）
3. ✅ 统一日志系统（DEBUG开关）
4. ✅ GIF动画支持
5. ✅ 垃圾回收机制
6. ✅ 命令系统

---

## 构建和测试

### 编译
```bash
./gradlew build
```

### 复制到游戏
```bash
cp build/libs/SignPicture-Rebornified-1.0.0.jar "G:/MinecraftGames/GregTech Oh my shit/.minecraft/versions/GregTech Odyssey/mods/"
```

### 测试环境
- Minecraft 1.20.1
- Forge 47.4.20
- Java 21

---

## 优先级

1. **立即修复**: ContentLocation.java 使用hash命名
2. **次要**: 调查日志混乱问题
3. **可选**: ModernUI迁移（依赖已添加但未实现）
