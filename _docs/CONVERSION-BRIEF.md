# liquor 1.20.1 Forge→Fabric 转换简报（共享约定）

> 本文件是 1.1.11 Forge 官方源码 → Fabric 1.20.1 工程的**共享转换约定**。所有转换批次必须遵守。
> 目标工程：`D:\ZCode_workspace\KaleidoscopeWorldLiquor-Refabricated\1.20.1\KaleidoscopeWorldLiquor-Refabricated-1.20.1-fabric`（下文称 **P**）

## 0. 路径
- 权威源码（1.1.11 官方 Forge，已 SRG→Mojmap）：`D:\ZCode_workspace\KaleidoscopeWorldLiquor-Refabricated\_refsources\liquor-1111-mojmap\com\bmt\kaleidoscope_world_liquor\`（下文称 **S**）
- 目标源码：`P\src\java` 下同包路径 `com\bmt\kaleidoscope_world_liquor\...`
- 完整转换规则（**必读**）：`D:\dsh_workSpace\Kaleidoscope_Chinese_Food_fabric\1.20.1\Kaleidoscope-ChineseFood-1.20.1-Fabric\_docs\CONVERSION-RULES-1.20.1.md`（下文称 **规则**）
- 同版本 Fabric 参考工程（所有 Fabric API 写法以它们为准，**唯一合法参考**）：
  - `D:\dsh_workSpace\Kaleidoscope_Chinese_Food_fabric\1.20.1\Kaleidoscope-ChineseFood-1.20.1-Fabric\src\main\java`（国味，下称 CF）
  - `D:\dsh_workSpace\Kaleidoscope_Chinese_Food_fabric\1.20.1\KaleidoscopeCookery-Refabricated-1.20.1-fabric\src\main\java`（厨房，下称 CK）
  - `D:\ZCode_workspace\KaleidoscopeWorldLiquor-Refabricated\1.20.1\KaleidoscopeTavern-Refabricated-1.20.1-fabric\src\main\java`（酒馆前置，下称 TV）
- 官方 1.1.11 资源/数据：`D:\ZCode_workspace\KaleidoscopeWorldLiquor-Refabricated\_refsources\liquor-1111-extract\`（已整体拷入 P 的 resources，**除非本简报要求，不要改资源**）

## 1. 硬纪律
1. **绝不参考其他 MC 版本**（1.21.1/1.21.11/26.x 的 liquor 代码一律不看）。同 MC 版本只看 S / CF / CK / TV + Fabric 官方文档。
2. 保持原类名/包名/字段名/方法名/常量名不变；只改加载器相关类型与调用。不顺手重构。
3. 保留原注释（中英文都留），不翻译不删除。
4. 代码风格：**4 空格缩进**（S 是 3 空格反编译产物，转换时统一改 4 空格）。
5. 转换后的文件不得残留 `net.minecraftforge.*` / `net.neoforged.*` import，**例外**：`net.minecraftforge.common.ForgeConfigSpec`、`net.minecraftforge.fml.config.ModConfig`（Forge Config API Port 提供）。完成后自检：`grep -rn "net.minecraftforge\|net.neoforged" <你的文件>` 只应命中这两个。
6. `ResourceLocation.fromNamespaceAndPath(a,b)` / `parse(s)` → `new ResourceLocation(a,b)` / `new ResourceLocation(s)`；`tryBuild`/`tryParse` 保留。

## 2. 注册（DeferredRegister → eager 字段）
所有 init 类改成 **静态字段 + `registerXxx()` 方法内 `Registry.register`**（照 CF 的 `init/ModBlocks.java`、`init/ModItems.java` 范式）：
- `public static final RegistryObject<Block> X = BLOCKS.register("x", () -> ...)` → `public static final Block X = ...;`（**创建在静态字段**），`registerBlocks()` 里 `Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(MODID, "x"), X);`
- **字段引用处删掉 `.get()`**：`ModBlocks.X.get()` → `ModBlocks.X`；`(Block)ModBlocks.X.get()` → `ModBlocks.X`。全文件 grep `.get()` 逐个核对，别漏。
- `ForgeRegistries.X` → `BuiltInRegistries.X`（对照规则 §1 列表）。
- **tavern Fabric 的 `DrinkBlock.Builder.build()` 直接返回 `Block`**（javap 核实过）：官方 Forge 写法 `.build().get()` → `.build()`；`DrinkBlock.create()`、`CocktailBlock::new` 保留。
- ModBlocks↔ModItems 互相引用用 lambda/延迟（规则 §1 顺序纪律）；主类注册顺序由主类作者统一规定，init 类只需保证**静态字段创建用到的其他类字段在本类静态初始化时已可用**（跨类引用尽量推迟到 registerXxx() 方法体或方法内 lambda）。
- 各 init 类的注册方法命名（主类会按此调用，**必须叫这些名字**）：
  `ModSounds.registerSounds()` / `ModEffects.registerEffects()` / `ModFluids.registerFluids()` / `ModFluidTypes.registerFluidTypes()` / `ModBlocks.registerBlocks()` / `ModItems.registerItems()` / `ModBlockEntities.registerBlockEntities()` / `ModRecipes.registerRecipes()` / `ModCreativeModeTabs.registerCreativeModeTabs()` / `ModPaintings.registerPaintings()` / `ModEnchantments.registerEnchantments()` / `SMCItems.registerSMCItems()` / `KTItems.registerKTItems()`

## 3. 事件（Forge → Fabric 1.20.1）
`@EventBusSubscriber`/`@SubscribeEvent`/`MinecraftForge.EVENT_BUS.register` 全部去掉，改成显式 `register()` 静态方法（在主类/客户端类里调用）。映射（**写之前先到 CF/CK 源码里 grep 同类写法核对**；fabric-api 0.92.12 jar 在 gradle 缓存，可 unzip/javap 验证类与方法签名）：
- `ItemTooltipEvent` → `ItemTooltipCallback`（CF event 类有例）
- `BuildCreativeModeTabContentsEvent` → `ItemGroupEvents.modifyEntriesEvent(ResourceKey)`（CF/CreativeTabEvents 有例；往**别人的**（tavern）创造栏塞项用同一 API，`FabricItemGroupEntries` 提供 add*/accept；**挂画要在 tavern 装饰栏 MASTER_MARISA_PAINTING 之后**，用 entries 的 addAfter 能力，CF 的 CreativeTabEventHandler 或 TV 里找先例）
- `PlayerInteractEvent.RightClickBlock` → `UseBlockCallback`
- `PlayerInteractEvent.RightClickItem` → `UseItemCallback`
- `PlayerTickEvent` → `ServerTickEvents.END_WORLD_TICK`（每世界）或 `END_TICK`；客户端 tick → `ClientTickEvents`
- `LivingAttackEvent` / `LivingHurtEvent` / `LivingDeathEvent` / `LivingDropsEvent` → 优先 `net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents`（ALLOW_DEATH/AFTER_DEATH 等，先 javap 确认 0.92.12 有哪些）；**事件粒度不够时用 mixin 注入原版 `LivingEntity.hurt` / `dropAllDeathLoot`（照 CK 的 `mixin/LivingEntityMixin` 手法，@Inject/Redirect/ModifyReturnValue + mixinextras）**。改掉落物（淘金热复制、斩首补头）用 mixin 进 `dropAllDeathLoot` 或 `ServerLivingEntityEvents.AFTER_DEATH` 自行 spawn，**选型前先反编译确认 1.20.1 `LivingEntity.dropAllDeathLoot` 实际逻辑**（loom 缓存 jar，见规则 §9 及 CF 排障手法）。
- `BreakEvent` → `PlayerBlockBreakEvents.BEFORE`（CF 有 `PlayerBlockBreakEvents` 用例则照抄；否则 `UseBlockCallback`/mixin）
- `ArrowNockEvent` 等冷门事件 → 到 CF/CK 里 grep 是否有先例，没有就 mixin 原版方法，**不许静默丢行为**；确实做不了的写 `// TODO(fabric):` 并在报告列出。
- `RenderTickEvent`（客户端帧）→ CF/CK 里 grep `WorldRenderEvents`/`HudRenderCallback`/`ClientTickEvents` 先例；渲染线框类逻辑可改挂 `WorldRenderEvents.AFTER`（javap 确认）。**做不了的列 TODO，不许删行为**。
- 配置事件 `ModConfigEvent.Loading/Reloading` → ForgeConfigAPIPort 的 `fuzs.forgeconfigapiport.api.event.config.ModConfigEvent`（若 CF/CK 有先例照抄）或直接省掉空实现。
- Forge 网络 `SimpleChannel` → `FabricPacket`+`PacketType`（规则 §5，CK `network/` 有例）。

## 4. 环境/守卫
- `Dist`→`EnvType`、`@OnlyIn`→`@Environment`、`FMLEnvironment`→`FabricLoader.getEnvironmentType()`、`ModList.isLoaded`→`FabricLoader.isModLoaded`（规则 §7）。
- `DistExecutor.unsafeRunWhenOn` → 直接调（类上加 `@Environment(EnvType.CLIENT)`）。
- 软依赖守卫语义**原样保留**（官方已有 `isLoaded` 判断的位置）。

## 5. 客户端
- `client/ClientSetup.java`（Forge `FMLClientSetupEvent`/`Register*Event`）内容 → 新类 `com.bmt.kaleidoscope_world_liquor.client.KaleidoscopeWorldLiquorClient implements ClientModInitializer`（`@Environment(CLIENT)`），方法名 `onInitializeClient()`。方块实体渲染器 `BlockEntityRenderers.register(...)`（1.20.1 public）；流体渲染 `FluidRenderHandlerRegistry.INSTANCE.register(...)` + `SimpleFluidRenderHandler`（CK 的 fluid 类有例）；**渲染层（cutout/translucent）由资产阶段统一在客户端类登记（见 §7）**，客户端类先留好 `registerRenderLayers()` 骨架。
- 1.20.1 菜单类是 `MenuScreens`（1.20.2+ 才叫 HandledScreens）；本模组无菜单则不管。

## 6. 特殊裁定（本模组专属，直接执行）
1. **丢弃 Forge 专属创造栏搜索器**：`client/creativetab/CreativeTabFilter.java`、`mixins/CreativeModeTabRegistryMixin.java`、`mixins/accessor/CreativeModeInventoryScreenAccessor.java` **不转换**（Forge 界面专属，Fabric 无对应物）。其"往创造栏塞内容"的职责已由 `event/CreativeTabEvents.java` → ItemGroupEvents 承担。在报告里记录即可。
2. **`compat/kaleidoscope_contraption/KaleidoscopeContraptionCompat`**：无 Fabric 版 → 保留类、守卫 `FabricLoader.isModLoaded("kaleidoscope_contraption")`，方法体按 CF 同类文件处理（CF 里有这个类的 Fabric 版，照它的做法）。
3. **doll 集成（关键）**：官方语义 = `doll 已装 && nether 未装` 才注册 doll_0..5（liquor ns）。保留两文件结构（门控类 + Impl），**Impl 类只能在守卫分支内被引用**（防 NoClassDefFoundError）。CF 的 `integration/KaleidoscopeDollIntegration{,Impl}.java` 是同结构的 Fabric 已验证实现——**逐段对照它的写法**（tab 塞入、SPECIAL_TOOLTIPS、注册时机），再把官方 liquor 的差异（AUTHOR_DOLL_DEFINITIONS 六个 doll、ModCreativeTabs.AUTHOR_DOLL_TAB）套进去。注意 doll Fabric jar 的 `ModCreativeTabs`/`ModRegisterEvent` 签名以 `P\libs\kaleidoscopedoll-1.20.1-fabric-1.0.9.jar` javap 为准。
4. **Create 联动**：官方 `compat/create/*` + `mixins/create/*` + `CreateMixinConfigPlugin` 全部转换；编译依赖 create-fabric（Modrinth 坐标）与 Ponder artifact。包名若与 Forge 相同（`com.simibubi.create.*`、`net.createmod.ponder.*`、`net.createmod.catnip.*`）直接保留；**以实际依赖 jar javap 为准**。网络层 SimpleChannel→FabricPacket（§3）。`CreateCompat.register()` 由主类在 `isModLoaded("create")` 守卫下调用。
5. **`init/smc/*`、`init/kaleidoscope_twilight/*` 代注册**：官方语义照搬（SMCItems 无条件注册、KTItems 仅当 twilight 未装、SMCIntegrationEvents 仅当 smc 已装）。这些类 import 的是本模组/原版类（无 smc/twilight 类依赖）——若有外部 import 先 javap 确认再处理。
6. **`fluids/MilkFluidType.java`**：Forge FluidType 无对应物 → 按规则 §8：流体渲染改 `FluidRenderHandlerRegistry`（客户端），属性语义用 `FluidState` 判定；**MilkFluidType 本体可删**，但其承载的行为（渲染色/发光？）要在别处等价实现并报告。
7. **Mixin**：结构与注入点保留；mixin json **不写 refmap**；`compatibilityLevel=JAVA_17`；MixinExtras 直接用（loader 0.19.5 内置，`com.llamalad7.mixinextras.*` import 不变）；Forge 专属 mixin 目标若在 Fabric 版原版类中不存在，用 javap 对 1.20.1 原版 jar 核对（同 MC 版本，方法名应该一致）。
8. **`config/ModConfigs`**：`ForgeConfigSpec` 全保留；主类注册 `ForgeConfigRegistry.INSTANCE.register(MODID, Type.COMMON, ModConfigs.SPEC)`（规则 §4）。

## 7. 资产/数据（资源阶段统一做，不在 Java 批次范围）
- 86 个模型带 `render_type`（1.20.1 原版忽略该字段）→ 客户端 `BlockRenderLayerMap.INSTANCE.putBlock` 按方块登记 cutout/translucent/cutout_mipped（**清单由资源阶段脚本从 models JSON 提取**）。
- 配方 JSON 中 Forge/非原版字段（`group`/`category`/`show_notification` 等）按 1.1.11 官方已清理后的现状为准；发现原版 1.20.1 仍不认的字段再修。
- 语言键、tag、战利品表、advancement 原样保留（官方 1.1.11 已是 1.20.1 格式）。

## 8. 自检与报告
每个批次完成后：
1. `grep -rn "net.minecraftforge\|net.neoforged" <本批文件>` → 只许剩 ForgeConfigSpec/fml.config.ModConfig。
2. `grep -rn "\.get()" <本批文件>` → 人工确认没有 RegistryObject 残留。
3. `grep -rn "fromNamespaceAndPath\|ResourceLocation.parse" <本批文件>` → 应为 0。
4. 报告：转换清单、**非机械判断**（每处事件换型/丢弃/守卫的决定与理由）、TODO/无法转换清单。
