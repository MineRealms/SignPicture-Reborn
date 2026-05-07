# URL长度问题分析和解决方案

## 问题描述

### 当前限制
- **告示牌容量**：4行 × 15字符 = 60字符
- **实际URL长度**：
  - 基础URL：`https://img-s.msn.cn/tenant/amp/entityid/AA20nsoi.img` = 54字符 ✅
  - 带属性：`https://img-s.msn.cn/tenant/amp/entityid/AA20nsoi.img#w=3.0,h=3.0` = 66字符 ❌

### 问题
当URL超过60字符时，`splitUrl()`方法会截断后面的内容，导致：
1. 属性丢失（size/rotation/offset）
2. URL不完整（如果基础URL就很长）
3. 图片无法加载

---

## 解决方案对比

### 方案1：URL缩短服务 ⭐ 推荐
**优点**：
- 彻底解决长度问题
- 用户体验好
- 已有实现（Bitly集成）

**缺点**：
- 需要API key
- 依赖外部服务
- 网络延迟

**实现**：
```java
// 在GuiMainFull中已有实现
private void onShortenUrl() {
    BitlyShortener shortener = new BitlyShortener();
    shortener.shorten(this.currentUrl, apiKey).thenAccept(result -> {
        if (result.isSuccess()) {
            this.currentUrl = result.getShortUrl(); // 通常<30字符
        }
    });
}
```

**使用流程**：
1. 用户输入长URL
2. 点击"Shorten URL"按钮
3. 自动缩短为短链接（如：`https://bit.ly/3xYz`）
4. 短链接写入告示牌

---

### 方案2：本地映射表 ⭐⭐ 最佳
**优点**：
- 不依赖外部服务
- 无网络延迟
- 完全离线工作
- 支持任意长度URL

**缺点**：
- 需要维护映射文件
- 短码在不同客户端不通用

**实现思路**：
```java
// 1. 生成短码（6位随机字符）
String shortCode = generateShortCode(); // 例如："abc123"

// 2. 保存映射
// .minecraft/signpic/url_mappings.json
{
  "abc123": "https://very-long-url.com/path/to/image.png#w=3.0,h=3.0,r=90"
}

// 3. 告示牌只存储短码
告示牌文本：
行1: "!abc123"
行2-4: 空

// 4. 渲染时查找映射
String shortCode = extractShortCode(sign); // "abc123"
String fullUrl = UrlMappingManager.resolve(shortCode);
```

**优点详解**：
- 短码只需6-8字符
- 支持无限长的URL
- 映射文件可以备份/分享
- 服务器可以同步映射文件

---

### 方案3：增加告示牌行数 ❌ 不可行
**问题**：
- 原版告示牌固定4行
- 修改需要Mixin告示牌核心逻辑
- 兼容性问题
- 不推荐

---

### 方案4：使用NBT存储 ⭐⭐⭐ 技术最优
**优点**：
- 不受文本长度限制
- 可以存储任意数据
- 不影响告示牌显示
- 完全兼容原版

**缺点**：
- 需要自定义NBT结构
- 需要网络同步
- 实现复杂度高

**实现思路**：
```java
// 1. 在告示牌NBT中存储完整URL
CompoundTag nbt = sign.getPersistentData();
nbt.putString("SignPictureURL", fullUrl);
nbt.putFloat("SignPictureWidth", 3.0f);
nbt.putFloat("SignPictureHeight", 3.0f);
// ... 其他属性

// 2. 告示牌文本显示简短提示
行1: "[SignPicture]"
行2: "Image loaded"
行3-4: 空

// 3. 渲染时从NBT读取
String fullUrl = sign.getPersistentData().getString("SignPictureURL");
float width = sign.getPersistentData().getFloat("SignPictureWidth");
```

**优点详解**：
- 支持无限长URL
- 支持更多属性（不受文本限制）
- 告示牌文本可以显示其他信息
- 数据结构清晰

---

## 推荐实现方案

### 短期方案：URL缩短 + 警告提示
```java
// GuiMainFull.java
private void onDone() {
    String fullUrl = buildFullUrl();
    
    // 检查长度
    if (fullUrl.length() > 60) {
        // 显示警告对话框
        showWarning("URL too long (" + fullUrl.length() + "/60 chars). " +
                   "Please use 'Shorten URL' button or reduce attributes.");
        return;
    }
    
    writeUrlToSign();
    this.onClose();
}
```

### 中期方案：本地映射表
```java
// 新增类：UrlMappingManager.java
public class UrlMappingManager {
    private static final Map<String, String> mappings = new HashMap<>();
    private static final File mappingFile = new File(gameDir, "signpic/url_mappings.json");
    
    public static String shorten(String longUrl) {
        String shortCode = generateShortCode();
        mappings.put(shortCode, longUrl);
        saveMappings();
        return "!" + shortCode; // 前缀!表示短码
    }
    
    public static String resolve(String shortCode) {
        return mappings.get(shortCode);
    }
}

// 修改SignHandler.java
private String extractFullUrl(SignBlockEntity sign) {
    String text = extractText(sign);
    
    // 检查是否是短码
    if (text.startsWith("!")) {
        String shortCode = text.substring(1);
        return UrlMappingManager.resolve(shortCode);
    }
    
    return text;
}
```

### 长期方案：NBT存储
```java
// 新增类：SignPictureData.java
public class SignPictureData {
    private String url;
    private float sizeWidth = 1.0f;
    private float sizeHeight = 1.0f;
    private float rotationX = 0.0f;
    private float rotationY = 0.0f;
    private float rotationZ = 0.0f;
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float offsetZ = 0.0f;
    
    public void saveToNBT(CompoundTag nbt) {
        nbt.putString("SignPictureURL", url);
        nbt.putFloat("SignPictureSizeW", sizeWidth);
        nbt.putFloat("SignPictureSizeH", sizeHeight);
        // ... 其他属性
    }
    
    public static SignPictureData loadFromNBT(CompoundTag nbt) {
        SignPictureData data = new SignPictureData();
        data.url = nbt.getString("SignPictureURL");
        data.sizeWidth = nbt.getFloat("SignPictureSizeW");
        // ... 其他属性
        return data;
    }
}

// 修改SignHandler.java
private SignPictureData extractData(SignBlockEntity sign) {
    CompoundTag nbt = sign.getPersistentData();
    
    // 优先从NBT读取
    if (nbt.contains("SignPictureURL")) {
        return SignPictureData.loadFromNBT(nbt);
    }
    
    // 降级：从文本读取（兼容旧版本）
    String text = extractTextUrl(sign);
    return SignPictureData.fromUrl(text);
}
```

---

## 当前状态

### 已实现
- ✅ URL缩短服务集成（Bitly）
- ✅ 长度限制：60字符
- ✅ 超长URL会被截断

### 未实现
- ❌ 长度检查和警告
- ❌ 本地映射表
- ❌ NBT存储

---

## 立即可用的解决方法

### 方法1：使用URL缩短服务
1. 在GUI中输入长URL
2. 点击"Shorten URL"按钮
3. 配置Bitly API key（在config中）
4. 等待缩短完成
5. 点击Done

### 方法2：手动缩短URL
1. 使用在线服务（bit.ly, tinyurl.com等）
2. 手动缩短URL
3. 粘贴短链接到SignPicture GUI

### 方法3：减少属性
1. 只使用必要的属性
2. 使用较小的数值（如：`w=2,h=2`而不是`w=2.0,h=2.0`）
3. 避免同时使用size/rotation/offset

---

## 测试案例

### 案例1：正常URL（54字符）
```
URL: https://img-s.msn.cn/tenant/amp/entityid/AA20nsoi.img
长度: 54字符
结果: ✅ 正常工作
```

### 案例2：带属性URL（66字符）
```
URL: https://img-s.msn.cn/tenant/amp/entityid/AA20nsoi.img#w=3.0,h=3.0
长度: 66字符
结果: ❌ 被截断为60字符
实际存储: https://img-s.msn.cn/tenant/amp/entityid/AA20nsoi.img#w=3
属性丢失: h=3.0被截断
```

### 案例3：超长URL（100字符）
```
URL: https://example.com/very/long/path/to/image/file/with/many/subdirectories/image.png#w=2.0,h=2.0,rx=45
长度: 100字符
结果: ❌ 被截断为60字符
实际存储: https://example.com/very/long/path/to/image/file/with/man
图片无法加载: URL不完整
```

### 案例4：使用短链接（20字符）
```
URL: https://bit.ly/3xYz
长度: 20字符
结果: ✅ 正常工作
可用空间: 还剩40字符可以添加属性
```

---

## 建议

### 立即实施
1. 添加长度检查和警告提示
2. 在GUI中显示当前URL长度
3. 超过60字符时禁用Done按钮

### 短期实施（1-2周）
1. 实现本地映射表系统
2. 自动检测超长URL并提示使用短码
3. 添加映射管理界面

### 长期实施（1-2月）
1. 迁移到NBT存储
2. 保持向后兼容（支持文本URL）
3. 添加更多属性支持

---

## 代码示例：添加长度检查

```java
// GuiMainFull.java
private void onDone() {
    String fullUrl = buildFullUrl();
    
    // 检查长度
    if (fullUrl.length() > 60) {
        // 计算超出的字符数
        int overflow = fullUrl.length() - 60;
        
        // 显示错误消息
        Log.notice("URL too long: " + fullUrl.length() + "/60 chars (+" + overflow + ")");
        Log.notice("Please use 'Shorten URL' button or reduce attributes");
        
        // 可选：自动打开URL缩短对话框
        // this.mc.setScreen(new GuiUrlShorten(this, fullUrl));
        
        return; // 不保存
    }
    
    onApply();
    this.onClose();
}

// 在render()中显示长度指示器
@Override
public void render(...) {
    super.render(...);
    
    String fullUrl = buildFullUrl();
    int length = fullUrl.length();
    int color = length > 60 ? 0xFFFF0000 : // 红色：超长
                length > 50 ? 0xFFFFAA00 : // 橙色：接近上限
                0xFF00FF00;                 // 绿色：正常
    
    guiGraphics.drawString(this.font, 
        "Length: " + length + "/60", 
        this.guiLeft + 10, this.guiTop + this.ySize - 50, 
        color, false);
}
```

---

## 总结

**当前问题**：URL超过60字符会被截断

**影响**：
- 属性丢失
- 长URL无法使用
- 用户体验差

**解决方案优先级**：
1. 🔴 立即：添加长度检查和警告
2. 🟡 短期：实现本地映射表
3. 🟢 长期：迁移到NBT存储

**临时解决方法**：
- 使用URL缩短服务
- 手动缩短URL
- 减少属性使用
