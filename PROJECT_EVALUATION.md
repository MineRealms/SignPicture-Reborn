# SignPicture 项目独立评估报告

## 评估日期
2026-05-08

## 预期流程
1. 放下告示牌
2. 打开编辑框
3. 调整设置粘贴网址
4. 下载图片，预览图片
5. 完事粘贴到世界上渲染（按照给的设置比如rotate,offset,size等进行渲染）

---

## 实际实现检查

### ✅ 步骤1: 放下告示牌
**状态**: 正常工作
- 使用原版Minecraft告示牌放置机制
- 无需额外实现

### ✅ 步骤2: 打开编辑框
**状态**: 已实现
- **文件**: `src/main/java/cn/minerealms/signpicture/mixin/SignEditScreenMixin.java`
- **机制**: 通过Mixin拦截原版告示牌编辑界面，自动打开`GuiMainFull`
- **触发**: 玩家右键点击告示牌时自动触发
- **实现细节**:
  ```java
  @Inject(method = "init", at = @At("RETURN"))
  private void onInit(CallbackInfo ci) {
      mc.setScreen(new GuiMainFull(null, this.sign));
  }
  ```

### ✅ 步骤3: 调整设置粘贴网址
**状态**: 已实现
- **文件**: `src/main/java/cn/minerealms/signpicture/gui/GuiMainFull.java`
- **功能**:
  - URL输入框（最大1000字符）
  - 粘贴按钮（从剪贴板）
  - Size调整按钮 → 打开`GuiSize`
  - Rotation调整按钮 → 打开`GuiRotation`
  - Offset调整按钮 → 打开`GuiOffset`
- **属性存储**:
  ```java
  private float sizeWidth = 1.0f;
  private float sizeHeight = 1.0f;
  private float rotationX = 0.0f;
  private float rotationY = 0.0f;
  private float rotationZ = 0.0f;
  private float offsetX = 0.0f;
  private float offsetY = 0.0f;
  private float offsetZ = 0.0f;
  ```

### ✅ 步骤4: 下载图片，预览图片
**状态**: 已实现
- **下载机制**:
  - **文件**: `src/main/java/cn/minerealms/signpicture/util/Downloader.java`
  - 异步下载图片
  - 缓存到本地: `signpic/cache/[hash]`
- **预览机制**:
  - **文件**: `GuiMainFull.java:239-253`
  - 使用`EntryManager`获取图片内容
  - 使用`ImageRenderer.renderImage()`在GUI中显示预览
  - 预览区域: 280x150像素
- **日志证据**:
  ```
  [SignPicture-ContentLoad-0/INFO]: Loading from cache: ...cache\4f0c108485aa9261e5952b0ee31f607e
  [SignPicture-ContentLoad-0/INFO]: ImageIO.read result: 404x132
  ```

### ❌ 步骤5: 渲染到世界（使用属性）
**状态**: **部分实现，存在严重问题**

#### 问题5.1: 属性未被渲染器使用
**严重程度**: 🔴 高

**问题描述**:
- GUI中设置的`size`, `rotation`, `offset`属性被正确保存到URL中
- `GuiMainFull.buildFullUrl()`正确构建带属性的URL（例如: `http://example.com/image.png#size=2.0,1.5&rotation=0,90,0`）
- URL被正确写入告示牌的4行文本
- **但是**: `SignHandler.renderImageToSign()`完全忽略这些属性

**当前渲染代码**:
```java
// SignHandler.java:73-94
private void renderImageToSign(...) {
    poseStack.translate(0.5, 0.75, 0.0625);  // 固定位置
    float size = 0.4f;  // 固定大小，忽略用户设置
    
    // 没有应用rotation
    // 没有应用offset
    // 没有应用size
    
    consumer.vertex(matrix, -size, -size, 0)...
    consumer.vertex(matrix, size, -size, 0)...
    consumer.vertex(matrix, size, size, 0)...
    consumer.vertex(matrix, -size, size, 0)...
}
```

**缺失的实现**:
1. 从URL中解析属性（使用`AttrReaders`）
2. 应用offset到`poseStack.translate()`
3. 应用rotation到`poseStack.mulPose()`
4. 应用size到顶点坐标

#### 问题5.2: 渲染崩溃
**严重程度**: 🔴 高

**错误信息**:
```
java.lang.IllegalStateException: Not filled all elements of the vertex
	at cn.minerealms.signpicture.render.SignHandler.renderImageToSign(SignHandler.java:88)
```

**原因**: 
- 已在之前修复：顶点格式不完整，缺少`color`和`lightmap`属性
- 修复后的代码已添加`.color(255, 255, 255, 255).uv2(packedLight)`

**状态**: ✅ 已修复（但未测试）

#### 问题5.3: 使用错误的RenderType
**严重程度**: ⚠️ 中

**问题描述**:
```java
bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.text(
    net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/misc/placeholder.png")
));
```

**问题**:
1. 使用`RenderType.text()`渲染图片不合适
2. 引用不存在的纹理`textures/misc/placeholder.png`导致警告
3. 实际图片纹理未被绑定和使用

**应该使用**:
- 动态纹理系统（`DynamicTexture`）
- 或自定义RenderType绑定BufferedImage

#### 问题5.4: 图片纹理未上传到GPU
**严重程度**: 🔴 高

**问题描述**:
- `renderImageToSign()`接收`BufferedImage`参数
- 但从未将图片数据上传到GPU作为OpenGL纹理
- 当前代码只是绘制了一个固定大小的白色矩形

**缺失的实现**:
```java
// 需要添加:
1. 将BufferedImage转换为NativeImage
2. 创建DynamicTexture
3. 注册到TextureManager
4. 使用正确的ResourceLocation绑定纹理
5. 在RenderType中使用该纹理
```

---

## 关键问题总结

### 🔴 严重问题（阻止功能工作）

1. **渲染器不使用属性**
   - 位置: `SignHandler.java:73-94`
   - 影响: 用户设置的size/rotation/offset完全无效
   - 修复: 需要解析URL属性并应用到渲染

2. **图片纹理未上传**
   - 位置: `SignHandler.java:73-94`
   - 影响: 实际上不显示图片，只显示白色矩形
   - 修复: 实现BufferedImage → GPU纹理的转换

3. **使用错误的RenderType**
   - 位置: `SignHandler.java:80-82`
   - 影响: 渲染管线不正确
   - 修复: 使用适合纹理渲染的RenderType

### ⚠️ 次要问题

4. **缓存路径使用URL字符串**
   - 位置: `ContentLocation.java`
   - 影响: 特殊字符导致文件系统错误
   - 状态: 已在CURRENT_ISSUES.md中记录
   - 修复: 使用MD5 hash作为文件名

---

## 工作流程验证

### 当前实际流程:
1. ✅ 放下告示牌
2. ✅ 自动打开GuiMainFull
3. ✅ 输入URL，调整size/rotation/offset
4. ✅ 图片被下载并缓存
5. ✅ 预览在GUI中显示
6. ✅ 点击Done，URL+属性写入告示牌
7. ❌ **渲染器读取URL但忽略属性**
8. ❌ **渲染器不显示实际图片**

### 预期流程:
1. ✅ 放下告示牌
2. ✅ 自动打开GuiMainFull
3. ✅ 输入URL，调整size/rotation/offset
4. ✅ 图片被下载并缓存
5. ✅ 预览在GUI中显示
6. ✅ 点击Done，URL+属性写入告示牌
7. ❌ **渲染器应该解析属性**
8. ❌ **渲染器应该显示实际图片并应用变换**

---

## 需要修复的文件

### 1. SignHandler.java (最关键)
**需要重写 `renderImageToSign()` 方法**:

```java
private void renderImageToSign(
    @Nonnull PoseStack poseStack,
    @Nonnull MultiBufferSource bufferSource,
    @Nonnull BufferedImage image,
    @Nonnull String fullUrl,  // 添加完整URL参数
    int packedLight
) {
    // 1. 解析属性
    AttrReaders attrs = new AttrReaders(fullUrl);
    SizeData size = attrs.getSizeData();
    RotationData rotation = attrs.getRotationData();
    OffsetData offset = attrs.getOffsetData();
    
    // 2. 应用变换
    poseStack.pushPose();
    
    // 基础位置
    poseStack.translate(0.5, 0.75, 0.0625);
    
    // 应用offset
    poseStack.translate(offset.x, offset.y, offset.z);
    
    // 应用rotation
    poseStack.mulPose(Axis.XP.rotationDegrees(rotation.x));
    poseStack.mulPose(Axis.YP.rotationDegrees(rotation.y));
    poseStack.mulPose(Axis.ZP.rotationDegrees(rotation.z));
    
    // 3. 上传纹理到GPU
    ResourceLocation texLocation = uploadImageTexture(image);
    
    // 4. 使用正确的RenderType
    VertexConsumer consumer = bufferSource.getBuffer(
        RenderType.entityCutout(texLocation)
    );
    
    // 5. 应用size并绘制
    Matrix4f matrix = poseStack.last().pose();
    float w = size.getWidth() * 0.4f;
    float h = size.getHeight() * 0.4f;
    
    consumer.vertex(matrix, -w, -h, 0).color(255, 255, 255, 255)
           .uv(0, 0).uv2(packedLight).endVertex();
    consumer.vertex(matrix, w, -h, 0).color(255, 255, 255, 255)
           .uv(1, 0).uv2(packedLight).endVertex();
    consumer.vertex(matrix, w, h, 0).color(255, 255, 255, 255)
           .uv(1, 1).uv2(packedLight).endVertex();
    consumer.vertex(matrix, -w, h, 0).color(255, 255, 255, 255)
           .uv(0, 1).uv2(packedLight).endVertex();
    
    poseStack.popPose();
}

// 新增方法
private ResourceLocation uploadImageTexture(BufferedImage image) {
    // 实现BufferedImage → DynamicTexture → ResourceLocation
    // 需要缓存纹理避免每帧重新上传
}
```

### 2. SignHandler.java - render()方法
**需要传递完整URL**:
```java
public void render(...) {
    String fullUrl = extractImageUrl(sign);  // 包含属性的完整URL
    // ...
    renderImageToSign(poseStack, bufferSource, image, fullUrl, packedLight);
}
```

---

## 结论

### 实现完成度: 70%

**已完成**:
- ✅ GUI系统（100%）
- ✅ 属性编辑（100%）
- ✅ 图片下载（100%）
- ✅ 图片预览（100%）
- ✅ 网络同步（100%）
- ✅ 属性序列化（100%）

**未完成**:
- ❌ 渲染器属性应用（0%）
- ❌ 图片纹理上传（0%）
- ❌ 正确的RenderType（0%）

### 核心问题
**渲染系统与属性系统完全脱节**。GUI和属性系统工作正常，但渲染器完全忽略它们。

### 优先级
1. 🔴 **立即修复**: 实现图片纹理上传到GPU
2. 🔴 **立即修复**: 渲染器解析并应用属性
3. ⚠️ **次要**: 修复缓存路径hash问题
4. ⚠️ **次要**: 优化RenderType选择
