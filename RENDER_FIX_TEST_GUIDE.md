# SignPicture 渲染系统修复 - 测试指南

## 修复内容

### 1. 完全重写SignHandler.java
- ✅ 使用Minecraft原生DynamicTexture系统
- ✅ 使用TextureManager管理纹理生命周期
- ✅ 使用RenderType.entityCutoutNoCull()完全兼容Minecraft渲染管线
- ✅ 正确解析URL中的属性（size, rotation, offset）
- ✅ 应用所有用户设置的变换
- ✅ 支持GIF动画（自动切换帧）
- ✅ 纹理缓存和自动清理

### 2. 纹理生命周期管理
- ✅ 纹理缓存：避免重复上传相同图片
- ✅ 自动清理：5分钟未使用的纹理自动释放
- ✅ 世界卸载清理：切换世界时清理所有纹理
- ✅ 资源重载支持：F3+T重载资源包时清理动态纹理

### 3. 渲染特性
- ✅ 完整的顶点格式：position + color + uv + overlay + lightmap + normal
- ✅ 正确的颜色格式转换：ARGB → ABGR
- ✅ 支持透明度
- ✅ 支持光照（packedLight）
- ✅ 支持覆盖层（packedOverlay）
- ✅ 正确的法线向量

---

## 测试步骤

### 测试1: 基础图片渲染
1. 启动游戏，进入世界
2. 放置一个告示牌
3. 右键点击告示牌，自动打开SignPicture GUI
4. 在URL输入框粘贴图片URL，例如：
   ```
   https://www.example.com/image.png
   ```
5. 等待预览加载
6. 点击"Done"按钮
7. **预期结果**：告示牌上应该显示图片

### 测试2: Size属性
1. 打开告示牌编辑
2. 输入URL
3. 点击"Size"按钮
4. 调整宽度和高度（例如：2.0 x 1.5）
5. 点击"Done"
6. **预期结果**：图片应该按照设置的大小渲染

### 测试3: Rotation属性
1. 打开告示牌编辑
2. 输入URL
3. 点击"Rotation"按钮
4. 调整旋转角度（例如：X=0, Y=45, Z=0）
5. 点击"Done"
6. **预期结果**：图片应该旋转45度

### 测试4: Offset属性
1. 打开告示牌编辑
2. 输入URL
3. 点击"Offset"按钮
4. 调整偏移（例如：X=0.2, Y=0.1, Z=0）
5. 点击"Done"
6. **预期结果**：图片应该偏移到指定位置

### 测试5: 组合属性
1. 打开告示牌编辑
2. 输入URL
3. 依次设置：
   - Size: 1.5 x 1.5
   - Rotation: Y=90
   - Offset: X=0.1, Y=0.1
4. 点击"Done"
5. **预期结果**：图片应该同时应用所有变换

### 测试6: GIF动画
1. 打开告示牌编辑
2. 输入GIF图片URL
3. 点击"Done"
4. **预期结果**：GIF应该自动播放动画

### 测试7: 纹理缓存
1. 放置多个告示牌，使用相同的URL
2. **预期结果**：
   - 第一个告示牌：下载并创建纹理
   - 后续告示牌：使用缓存的纹理（不重复下载）
3. 查看日志，应该只有一条"Created texture"消息

### 测试8: 纹理清理
1. 放置告示牌并显示图片
2. 等待5分钟不看该告示牌
3. **预期结果**：纹理应该被自动清理（查看日志）
4. 再次靠近告示牌
5. **预期结果**：纹理重新创建

### 测试9: 资源重载
1. 放置告示牌并显示图片
2. 按F3+T重载资源包
3. **预期结果**：
   - 日志显示"Resource reloading, clearing dynamic textures"
   - 图片重新加载并显示

### 测试10: 世界切换
1. 在世界A中放置告示牌并显示图片
2. 退出到主菜单
3. 进入世界B
4. **预期结果**：
   - 日志显示"World unloading, clearing all textures"
   - 世界A的纹理被清理

---

## 调试信息

### 查看日志
日志位置：`.minecraft/logs/latest.log`

关键日志消息：
```
[SignPicture] [DEBUG] Created texture: signpicture/dynamic/xxx (404x132)
[SignPicture] [DEBUG] Loading from cache: ...
[SignPicture] [DEBUG] ImageIO.read result: 404x132
[SignPicture] Released expired texture: signpicture/dynamic/xxx
[SignPicture] World unloading, clearing all textures
[SignPicture] Resource reloading, clearing dynamic textures
```

### 启用DEBUG日志
在配置文件中设置：
```toml
[common]
    debug = true
```

---

## 已知问题和限制

### 1. 缓存路径问题（未修复）
- 问题：URL中的特殊字符可能导致文件系统错误
- 状态：已在CURRENT_ISSUES.md中记录
- 解决方案：使用MD5 hash作为文件名（待实现）

### 2. 告示牌文本长度限制
- 限制：每行最多15字符，共4行（60字符）
- 影响：长URL需要使用URL缩短服务
- 解决方案：使用"Shorten URL"按钮

### 3. 渲染距离
- 默认：256方块
- 可在SignPictureRenderer.getViewDistance()中调整

---

## 性能考虑

### 纹理内存使用
- 每个纹理占用：width × height × 4字节（RGBA）
- 例如：512x512图片 = 1MB
- 缓存限制：自动清理5分钟未使用的纹理

### 渲染性能
- 使用Minecraft原生RenderType，性能与原版实体渲染相同
- 支持批处理渲染
- 支持视锥剔除

### 下载性能
- 异步下载，不阻塞主线程
- 支持HTTP代理
- 缓存到本地，避免重复下载

---

## 技术细节

### 顶点格式
```java
consumer.vertex(matrix, x, y, z)           // 位置
        .color(255, 255, 255, 255)         // 颜色（白色=不着色）
        .uv(u, v)                          // 纹理坐标
        .overlayCoords(packedOverlay)      // 覆盖层（受伤红色等）
        .uv2(packedLight)                  // 光照
        .normal(0, 0, 1)                   // 法线向量
        .endVertex();
```

### 变换顺序
1. 基础位置：translate(0.5, 0.5, 0.501)
2. 用户偏移：translate(offset.x, offset.y, offset.z)
3. 用户旋转：mulPose(Y) → mulPose(X) → mulPose(Z)
4. 用户大小：顶点坐标 × size

### RenderType选择
- `entityCutoutNoCull`：支持透明度，不剔除背面，适合告示牌渲染
- 其他选项：
  - `entityTranslucent`：半透明，需要排序
  - `entityCutout`：不透明，剔除背面

---

## 下一步优化

### 短期
1. 修复缓存路径hash问题
2. 添加纹理压缩支持
3. 优化GIF动画性能

### 长期
1. 支持视频播放
2. 支持3D模型
3. 支持交互式内容
4. ModernUI集成

---

## 回滚方案

如果新版本有问题，可以回滚到旧版本：
```bash
git checkout <previous-commit>
./gradlew build
```

或者使用备份的jar文件。
