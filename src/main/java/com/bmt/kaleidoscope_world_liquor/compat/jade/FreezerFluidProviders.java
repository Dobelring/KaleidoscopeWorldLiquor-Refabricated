package com.bmt.kaleidoscope_world_liquor.compat.jade;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.api.view.ProgressView;
import org.jetbrains.annotations.NotNull;

/**
 * 官方 1.1.11：冰柜牛奶流体的 Jade 提示（neo 版没有 ServerDataProvider，依赖 Jade 自带流体同步）。
 * <p>
 * 26.3 的 Jade UI 已换成 {@code extractRenderState(GuiGraphicsExtractor, …)} 抽取模型，
 * 没了 1.21.1 的 {@code IElementHelper}/{@code ProgressStyle#overlay}——这里按新 API 等价实现：
 * 用 {@link JadeUI#progress(ProgressView)} + 自绘 16×16 平铺奶贴图作为进度条覆盖层。
 */
public final class FreezerFluidProviders {
    /** 官方从 FreezerRenderer.MILK_STILL_TEXTURE 取贴图；自有奶流体直接用本模组贴图。 */
    private static final Identifier MILK_STILL_TEXTURE =
            Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "block/milk_still");

    private FreezerFluidProviders() {
    }

    private static boolean isMilk(BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof FreezerBlockEntity be) {
            return !be.tank.isResourceBlank() && be.tank.getFluid() == ModFluids.MILK_STILL;
        }
        return false;
    }

    /**
     * 官方 1.1.11：tank 已空但客户端还缓存着「服务端曾经下发过奶数据」时，
     * 也要把 Jade 自带的流体行摘掉（否则会显示过期的奶量）。
     * <p>26.3 的 Jade 不再用 "JadeFluidStorage" NBT 路径，退化为在服务端数据里找奶流体 id。
     */
    private static boolean isStaleMilkData(BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof FreezerBlockEntity be)) {
            return false;
        }
        if (!be.tank.isResourceBlank()) {
            return false;
        }
        CompoundTag data = accessor.getServerData();
        if (data == null || data.isEmpty()) {
            return false;
        }
        String milkId = BuiltInRegistries.FLUID.getKey(ModFluids.MILK_STILL).toString();
        return data.toString().contains(milkId);
    }

    private static int capacityMb(FreezerBlockEntity be) {
        long capacity = be.tank.getCapacityTransfer();
        long mb = capacity * (long) CustomFluidTank.MB_PER_BUCKET / FluidConstants.BUCKET;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, mb));
    }

    /**
     * 进度条覆盖层：16×16 平铺奶贴图（官方 CustomFluidElement 的 26.3 等价物）。
     */
    public static final class CustomFluidElement extends Element {
        private static final int SIZE = 16;

        public CustomFluidElement() {
            this.width = SIZE;
            this.height = SIZE;
        }

        @Override
        public void extractRenderState(@NotNull GuiGraphicsExtractor extractor, int x, int y, float partialTick) {
            extractor.blitSprite(RenderPipelines.GUI_TEXTURED, MILK_STILL_TEXTURE, x, y, SIZE, SIZE);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.empty();
        }
    }

    /** 冰柜奶量提示组件（官方同名内部枚举的 26.3 等价物）。 */
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
            return 1001;
        }

        @Override
        public void appendTooltip(@NotNull ITooltip tooltip, @NotNull BlockAccessor accessor, @NotNull IPluginConfig config) {
            boolean milk = isMilk(accessor);
            boolean stale = isStaleMilkData(accessor);
            if (milk || stale) {
                // 自家奶行已有名字/数量时摘掉 Jade 自带流体行，避免重复（官方同款）
                tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);
            }
            if (!milk || !(accessor.getBlockEntity() instanceof FreezerBlockEntity be)) {
                return;
            }

            int amountMb = be.tank.getFluidAmountMb();
            int capacityMb = capacityMb(be);
            if (capacityMb <= 0) {
                return;
            }
            float ratio = Math.min(1.0F, (float) amountMb / capacityMb);

            // 该分支仅在 isMilk=true 时执行，直接用本模组奶流体的既有语言键
            Component name = Component.translatable("fluid.kaleidoscope_world_liquor.milk");
            Component amountText = Component.literal(amountMb + "mB").withStyle(ChatFormatting.WHITE);
            Component text;
            if (accessor.showDetails()) {
                Component nameAmount = Component.empty()
                        .append(name.copy().withStyle(ChatFormatting.WHITE))
                        .append(" ")
                        .append(amountText);
                text = Component.translatable("jade.fluid.with_capacity",
                                nameAmount, Component.literal(capacityMb + "mB"))
                        .withStyle(ChatFormatting.GRAY);
            } else {
                text = Component.translatable("jade.fluid", name, amountText);
            }

            ProgressView view = new ProgressView(
                    ProgressView.Part.of(ratio, new CustomFluidElement()),
                    text,
                    JadeUI.progressStyle(),
                    BoxStyle.nestedBox());
            tooltip.add(JadeUI.progress(view));
        }
    }
}
