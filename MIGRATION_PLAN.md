# SignPicture 1.12.2 → 1.20.1 迁移计划

## 项目概述

**源项目**: SignPicture 1.12.2 (多版本架构)
**目标**: SignPicture-Rebornified 1.20.1 (单版本，简化架构)
**新包名**: cn.minerealms.signpicture

## 架构简化策略

### 移除的复杂性
1. ❌ 多版本支持系统 (UniversalVersioner)
2. ❌ Compat兼容层 (直接使用1.20.1 API)
3. ❌ ASM CoreMod (使用Mixin替代)
4. ❌ 旧版FML代码 (使用新版Forge API)
5. ❌ SidedProxy模式 (使用DistExecutor)

### 保留的核心功能
1. ✅ 告示牌图片渲染
2. ✅ 聊天图片显示
3. ✅ 图片下载和缓存管理
4. ✅ GIF动画支持
5. ✅ GUI配置界面
6. ✅ 图片变换系统 (旋转/缩放/偏移)
7. ✅ 命令系统
8. ✅ 更新检查器

---

## 迁移阶段规划

### Phase 0: 项目初始化 ✅
- [x] 创建Git仓库
- [x] 配置.gitignore
- [x] 更新gradle配置为SignPicture-Rebornified
- [x] 创建迁移计划文档

**提交**: "Initial project setup for SignPicture-Rebornified 1.20.1"

---

### Phase 1: 基础架构和常量
**目标**: 建立基础包结构和常量定义

#### 任务清单
- [ ] 创建包结构: cn.minerealms.signpicture
- [ ] 迁移 Reference.java → ModConstants.java
  - 更新包名和mod信息
- [ ] 创建主Mod类 SignPicture.java
  - 使用 @Mod 注解 (新版Forge)
  - 移除 @SidedProxy，使用 DistExecutor
- [ ] 创建 Config.java (使用Forge Config API v2)
  - 迁移所有配置项
  - 使用 ForgeConfigSpec

#### API变更
- `@Mod` 注解变更: modid → value
- 移除 `@SidedProxy`，使用 `DistExecutor.unsafeRunWhenOn()`
- 配置系统: Configuration → ForgeConfigSpec

**验证**: 编译通过，mod可加载
**提交**: "Phase 1: Basic mod structure and configuration"

---

### Phase 2: 工具类和基础系统
**目标**: 迁移不依赖Minecraft API的工具类

#### 任务清单
- [ ] 迁移 util/ 包
  - FileUtility.java - 文件操作
  - Downloader.java - HTTP下载 (更新HTTP客户端)
  - ThreadUtils.java - 线程工具
  - ChatBuilder.java → 更新为Component API
- [ ] 迁移 lib/ 包
  - GifDecoder.java - GIF解码器
  - ComponentMover.java, ComponentResizer.java - GUI工具
- [ ] 迁移 Log.java 和 Debug.java
  - 使用 SLF4J Logger
- [ ] 迁移 Locations.java
  - 更新文件路径API

#### API变更
- `ITextComponent` → `Component` (net.minecraft.network.chat)
- `TextComponentString` → `Component.literal()`
- Logger: 使用 `LogUtils.getLogger()`

**验证**: 工具类单元测试通过
**提交**: "Phase 2: Utility classes and basic systems"

---

### Phase 3: 内容管理系统
**目标**: 迁移Entry/Content管理系统

#### 任务清单
- [ ] 迁移 entry/ 包
  - Entry.java, EntryId.java, EntryManager.java
  - Content.java, ContentId.java, ContentManager.java
  - ContentLocation.java
  - 所有接口 (ITickEntry, IInitable等)
- [ ] 迁移 state/ 包
  - State.java, StateType.java
  - Progress.java, Progressable.java
- [ ] 迁移 attr/ 包
  - Attrs.java - 属性定义
  - PropReader.java - 属性解析
  - AttrReaders.java, AttrWriters.java
  - OffsetData.java, AnimationData.java

#### API变更
- 资源位置: `ResourceLocation` 构造函数变更
- NBT系统: `NBTTagCompound` → `CompoundTag`

**验证**: 内容管理系统可以加载和缓存图片
**提交**: "Phase 3: Content management system"

---

### Phase 4: Mixin系统替代ASM
**目标**: 使用Mixin替代ASM CoreMod

#### 任务清单
- [ ] 配置Mixin (已在build.gradle中)
- [ ] 创建 signpicture.mixins.json
- [ ] 替换 TileEntityVisitor → SignBlockEntityMixin
  - 扩展告示牌渲染边界
  - 目标: `SignBlockEntity.getRenderBoundingBox()`
- [ ] 替换 GuiScreenBookVisitor → BookEditScreenMixin
  - 钩入书本GUI渲染
  - 目标: `BookEditScreen.render()`
- [ ] 替换 GuiNewChatVisitor → ChatComponentMixin
  - 启用聊天图片渲染
  - 目标: `ChatComponent.render()`
- [ ] 替换 GuiScreenVisitor → ScreenMixin
  - 钩入输入处理
  - 目标: `Screen.keyPressed()`, `Screen.mouseClicked()`

#### API变更
- 1.12: `TileEntity` → 1.20: `BlockEntity`
- 1.12: `GuiScreen` → 1.20: `Screen`
- 1.12: `GuiNewChat` → 1.20: `ChatComponent`
- 1.12: `GuiScreenBook` → 1.20: `BookEditScreen`

**验证**: Mixin应用成功，不崩溃
**提交**: "Phase 4: Mixin system replacing ASM transformers"

---

### Phase 5: 渲染系统
**目标**: 迁移告示牌和图片渲染

#### 任务清单
- [ ] 迁移 SignHandler.java
  - 更新为新版渲染API
  - 使用 `PoseStack` 替代 `GlStateManager`
- [ ] 迁移 SignEntity.java
  - 更新实体包装器
- [ ] 迁移 PreviewTileEntitySign.java
  - 更新预览渲染
- [ ] 创建自定义告示牌渲染器
  - 替代 CompatTileEntitySignRenderer
  - 实现 `BlockEntityRenderer<SignBlockEntity>`
- [ ] 迁移物品渲染
  - 移除 CompatItemSignRenderer (1.20使用不同系统)
  - 使用 BakedModel 或 ItemDecorator

#### API变更
- `GlStateManager` → `RenderSystem` + `PoseStack`
- `TileEntitySpecialRenderer` → `BlockEntityRenderer`
- `IBakedModel` → 新版模型系统
- 纹理绑定: `bindTexture()` → `RenderSystem.setShaderTexture()`
- 矩阵操作: 直接GL调用 → `PoseStack.mulPose()`

**验证**: 告示牌可以显示图片
**提交**: "Phase 5: Rendering system with modern APIs"

---

### Phase 6: GUI系统
**目标**: 迁移所有GUI界面

#### 任务清单
- [ ] 迁移基础GUI类
  - GuiMain.java → MainScreen.java
  - GuiSettings.java → SettingsScreen.java
  - GuiImage.java → ImageScreen.java
- [ ] 迁移编辑器GUI
  - GuiSize.java, GuiRotation.java, GuiOffset.java
  - GuiSignOption.java
- [ ] 迁移工具GUI
  - GuiTask.java, GuiVariable.java
  - GuiIngameScreenShot.java, GuiWindowScreenShot.java
- [ ] 迁移组件
  - SignPicLabel.java
  - OverlayFrame.java
- [ ] 更新GUI渲染调用
  - 使用 `GuiGraphics` (1.20新增)

#### API变更
- `GuiScreen` → `Screen`
- `drawString()` → `GuiGraphics.drawString()`
- `drawTexturedModalRect()` → `GuiGraphics.blit()`
- 按钮系统: `GuiButton` → `Button` (新构造方式)
- 文本框: `GuiTextField` → `EditBox`

**验证**: 所有GUI可以打开和交互
**提交**: "Phase 6: GUI system with modern Screen API"

---

### Phase 7: 事件处理系统
**目标**: 迁移事件处理器

#### 任务清单
- [ ] 创建 ClientEventHandler.java
  - 替代 CompatEvents 和 CoreHandler
  - 使用 `@SubscribeEvent` 注解
- [ ] 迁移 KeyHandler.java
  - 更新按键绑定API
  - 使用 `KeyMapping`
- [ ] 迁移 AnvilHandler.java
  - 更新铁砧GUI钩子
- [ ] 注册事件总线
  - 使用 `MinecraftForge.EVENT_BUS.register()`
  - 使用 `FMLJavaModLoadingContext.get().getModEventBus()`

#### API变更
- `KeyBinding` → `KeyMapping`
- 事件包: `net.minecraftforge.fml.common.eventhandler` → `net.minecraftforge.eventapi`
- 客户端事件: 注册到 `MinecraftForge.EVENT_BUS`
- Mod事件: 注册到 Mod Event Bus

**验证**: 按键绑定工作，事件触发正常
**提交**: "Phase 7: Event handling system"

---

### Phase 8: 命令系统
**目标**: 迁移命令到Brigadier

#### 任务清单
- [ ] 重写 RootCommand.java
  - 使用 Brigadier API
  - `Commands.literal()`, `Commands.argument()`
- [ ] 迁移子命令
  - CommandImage.java
  - CommandVersion.java
- [ ] 注册命令
  - 使用 `RegisterCommandsEvent`

#### API变更
- 完全重写: 1.12命令系统 → Brigadier
- `ICommand` → `CommandNode`
- `CommandBase` → `Commands` 工具类
- 参数解析: 自定义 → `ArgumentType`

**验证**: /signpic 命令可用
**提交**: "Phase 8: Brigadier command system"

---

### Phase 9: 网络系统
**目标**: 迁移网络包

#### 任务清单
- [ ] 创建网络通道
  - 使用 `SimpleChannel`
- [ ] 迁移数据包
  - 告示牌更新包
  - 配置同步包
- [ ] 实现包处理器

#### API变更
- `SimpleNetworkWrapper` → `SimpleChannel` (API相似)
- `IMessage` → 自定义包类
- `IMessageHandler` → `BiConsumer<MSG, Supplier<NetworkEvent.Context>>`

**验证**: 客户端-服务器通信正常
**提交**: "Phase 9: Network packet system"

---

### Phase 10: 资源文件
**目标**: 迁移所有资源文件

#### 任务清单
- [ ] 迁移语言文件
  - 更新格式 (如需要)
  - 路径: assets/signpicture/lang/
- [ ] 迁移纹理
  - 路径: assets/signpicture/textures/
- [ ] 迁移音效 (如有)
  - 更新 sounds.json 格式
- [ ] 更新 pack.mcmeta
- [ ] 创建 mods.toml (已完成)

#### API变更
- 语言文件格式可能有细微变化
- 资源包格式: pack_format 9 (1.20.1)

**验证**: 语言文件加载，纹理显示
**提交**: "Phase 10: Resource files migration"

---

### Phase 11: 依赖库更新
**目标**: 更新外部依赖

#### 任务清单
- [ ] 检查 bnnwidget 库
  - 寻找1.20.1版本或替代方案
  - 如无，考虑移除或自行实现
- [ ] 更新HTTP客户端
  - 可能需要更新到新版Apache HttpClient
- [ ] 检查其他依赖兼容性

**验证**: 所有依赖解析成功
**提交**: "Phase 11: Update external dependencies"

---

### Phase 12: 测试和优化
**目标**: 全面测试和性能优化

#### 任务清单
- [ ] 功能测试
  - 告示牌图片显示
  - 聊天图片显示
  - GUI交互
  - 命令执行
  - 配置保存/加载
- [ ] 性能测试
  - 图片加载性能
  - 渲染性能
  - 内存使用
- [ ] 兼容性测试
  - 单人游戏
  - 多人游戏
  - 服务器端
- [ ] Bug修复
- [ ] 代码清理和优化

**验证**: 所有功能正常工作
**提交**: "Phase 12: Testing and optimization"

---

## 主要API变更总结

### 包名变更
| 1.12.2 | 1.20.1 |
|--------|--------|
| net.minecraft.client.gui.GuiScreen | net.minecraft.client.gui.screens.Screen |
| net.minecraft.tileentity.TileEntity | net.minecraft.world.level.block.entity.BlockEntity |
| net.minecraft.util.text.ITextComponent | net.minecraft.network.chat.Component |
| net.minecraft.nbt.NBTTagCompound | net.minecraft.nbt.CompoundTag |
| net.minecraft.client.renderer.GlStateManager | com.mojang.blaze3d.systems.RenderSystem |

### 渲染系统
- **矩阵栈**: 引入 `PoseStack`，所有渲染方法需要传递
- **着色器**: 使用 `RenderSystem.setShader()` 设置着色器
- **纹理**: `RenderSystem.setShaderTexture(int, ResourceLocation)`
- **GUI渲染**: 新增 `GuiGraphics` 包装类

### 文本系统
- **创建文本**: `Component.literal()`, `Component.translatable()`
- **样式**: `Style` 系统保持，但API略有变化
- **MutableComponent**: 可变文本组件

### 配置系统
- **ForgeConfigSpec**: 使用Builder模式
- **配置类型**: CLIENT, COMMON, SERVER
- **配置事件**: `ModConfigEvent.Loading`, `ModConfigEvent.Reloading`

### 事件系统
- **注解**: `@SubscribeEvent` 保持
- **事件总线**: 区分Mod总线和Forge总线
- **取消**: `event.setCanceled(true)` 保持

### 资源系统
- **ResourceLocation**: 构造函数变为 `new ResourceLocation(namespace, path)`
- **资源管理器**: API变化较大

---

## 风险和挑战

### 高风险项
1. **ASM → Mixin 转换**: 需要仔细测试每个注入点
2. **渲染系统**: API变化最大，需要完全重写
3. **bnnwidget依赖**: 可能没有1.20版本

### 中风险项
1. **GUI系统**: API变化较多，但相对直接
2. **命令系统**: Brigadier学习曲线
3. **网络系统**: API相似但有细节变化

### 低风险项
1. **工具类**: 大部分可以直接迁移
2. **配置系统**: 新API更好用
3. **资源文件**: 格式变化小

---

## 开发规范

### 代码风格
- 使用Java 17特性
- 遵循Forge 1.20.1最佳实践
- 保持代码简洁，移除不必要的抽象

### 提交规范
- 每个Phase完成后提交
- 提交信息格式: "Phase X: 简短描述"
- 确保每次提交可编译

### 测试策略
- 每个Phase完成后编译验证
- 关键Phase需要运行游戏测试
- 最终Phase进行全面测试

---

## 时间估算

- Phase 0: ✅ 完成
- Phase 1-3: 基础系统 (2-3小时)
- Phase 4: Mixin系统 (1-2小时)
- Phase 5: 渲染系统 (3-4小时) ⚠️ 最复杂
- Phase 6: GUI系统 (2-3小时)
- Phase 7-9: 事件/命令/网络 (2-3小时)
- Phase 10-11: 资源和依赖 (1-2小时)
- Phase 12: 测试优化 (2-3小时)

**总计**: 约15-23小时

---

## 下一步行动

开始 **Phase 1: 基础架构和常量**
