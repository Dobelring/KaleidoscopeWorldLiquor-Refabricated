package com.bmt.kaleidoscope_world_liquor.compat.jade;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.api.view.ProgressView;

/**
 * Jade：冰柜牛奶流体 tooltip（官方 1.1.11 neo 版 FreezerFluidProviders 的 26.x 移植）。
 * <p>
 * 官方结构：奶在柜时移除 Jade 自带 UNIVERSAL_FLUID_STORAGE 行，改画一条
 * 「奶贴图平铺 + 语言键 fluid.kaleidoscope_world_liquor.milk」的进度条；另附
 * 过期数据兜底（旧 Jade 把流体行留在 serverData 里时一并移除——Jade 26 改走
 * StreamServerDataProvider 网络同步，该分支仅作结构对齐，多半为 no-op）。
 * 官方 neo 版没有 ServerDataProvider（依赖 Jade 自带流体同步），照官方结构。
 * <p>
 * 26.x 适配（Jade 26.2.8 API）：
 * <ul>
 *   <li>官方的 CustomFluidElement 手绘平铺 → {@code JadeUI.fluid(JadeFluidObject)}。
 *       本分支奶是注册流体（ModFluids.MILK_STILL），Jade 直接经流体渲染取
 *       block/milk_still 贴图；官方奶是假流体才需要手绘。</li>
 *   <li>{@code IElementHelper} → 静态 {@link JadeUI}；{@code helper.progress(…)} →
 *       {@code JadeUI.progress(new ProgressView(Part.of(ratio, overlay), text, style, box))}；
 *       {@code BoxStyle.getNestedBox()} → {@code BoxStyle.nestedBox()}。</li>
 *   <li>官方的 jade.fluid2 键 Jade 26 已移除 → 用 jade.fluid + jade.fluid.with_capacity 组合。</li>
 * </ul>
 */
public final class FreezerFluidProviders {
    private FreezerFluidProviders() {
    }

    private static boolean isMilk(BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof FreezerBlockEntity be) {
            return !be.tank.isResourceBlank() && be.tank.getFluid() == ModFluids.MILK_STILL;
        }
        return false;
    }

    /** 官方过期数据兜底：柜已空但旧 Jade 流体行数据仍指向奶时也移除自带行。 */
    private static boolean isStaleMilkData(BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof FreezerBlockEntity be)) {
            return false;
        }
        if (!be.tank.isResourceBlank()) {
            return false;
        }
        CompoundTag data = accessor.getServerData();
        if (data == null) {
            return false;
        }
        ListTag groups = data.getListOrEmpty("JadeFluidStorage");
        if (groups.isEmpty()) {
            return false;
        }
        String milkId = BuiltInRegistries.FLUID.getKey(ModFluids.MILK_STILL).toString();
        for (Tag groupTag : groups) {
            if (!(groupTag instanceof CompoundTag group)) {
                continue;
            }
            for (Tag viewTag : group.getListOrEmpty("Views")) {
                if (!(viewTag instanceof CompoundTag view)) {
                    continue;
                }
                CompoundTag fluid = view.getCompoundOrEmpty("fluid");
                if (fluid.getLongOr("amount", 0L) > 0L && milkId.equals(fluid.getStringOr("type", ""))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static final class TooltipProvider implements IBlockComponentProvider {
        public static final TooltipProvider INSTANCE = new TooltipProvider();

        private TooltipProvider() {
        }

        @Override
        public Identifier getUid() {
            return ModJadePlugin.FREEZER_FLUID_UID;
        }

        @Override
        public int getDefaultPriority() {
            // 晚于 Jade 自带 UNIVERSAL_FLUID_STORAGE（默认 1000）执行，便于移除其行
            return 1001;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            boolean milk = FreezerFluidProviders.isMilk(accessor);
            boolean stale = FreezerFluidProviders.isStaleMilkData(accessor);
            if (milk || stale) {
                tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);
            }

            if (milk && accessor.getBlockEntity() instanceof FreezerBlockEntity be) {
                int amount = be.tank.getFluidAmountMb();
                int capacity = CustomFluidTank.MB_PER_BUCKET;
                if (capacity > 0) {
                    float ratio = (float) amount / capacity;
                    // 该分支仅在 isMilk=true 时执行，直接用本模组奶流体的既有语言键
                    Component name = Component.translatable("fluid.kaleidoscope_world_liquor.milk");
                    Component text = accessor.showDetails()
                            ? Component.translatable("jade.fluid",
                                    Component.literal(name.getString()).withStyle(ChatFormatting.WHITE),
                                    Component.translatable("jade.fluid.with_capacity",
                                            amount + "mB", capacity + "mB"))
                            .withStyle(ChatFormatting.GRAY)
                            : Component.translatable("jade.fluid",
                                    name, Component.literal(amount + "mB"));
                    ProgressStyle style = JadeUI.progressStyle();
                    ProgressView view = new ProgressView(
                            ProgressView.Part.of(ratio, JadeUI.fluid(JadeFluidObject.of(ModFluids.MILK_STILL))),
                            text, style, BoxStyle.nestedBox());
                    tooltip.add(JadeUI.progress(view));
                }
            }
        }
    }
}
