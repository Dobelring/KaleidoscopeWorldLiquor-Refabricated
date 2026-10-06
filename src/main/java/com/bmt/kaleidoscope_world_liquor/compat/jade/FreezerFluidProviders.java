package com.bmt.kaleidoscope_world_liquor.compat.jade;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModFluids;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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

/**
 * 官方 1.1.11：冰柜牛奶流体的 Jade tooltip（自定义平铺奶贴图 + 覆盖默认流体条）。
 * <p>
 * 语义照官方 neo 版：
 * <ul>
 *   <li>坦克里是牛奶（或服务端数据里残留上次的奶）→ 先移除 Jade 自带的
 *       {@code UNIVERSAL_FLUID_STORAGE} 条目，再补一条用本模组奶贴图绘制的进度条；</li>
 *   <li>官方 neo 版<b>没有</b> ServerDataProvider——依赖 Jade 自带的流体同步，
 *       本分支同款（冰柜 BE 已注册 {@code FluidStorage.SIDED} 暴露 tank）。</li>
 * </ul>
 * <p>
 * 26.x Jade 差异：
 * <ul>
 *   <li>{@code IElementHelper} / {@code ProgressStyle.overlay(Element)} 已移除，
 *       自定义覆盖元素改挂到 {@link ProgressView.Part#of(float, Element)}；</li>
 *   <li>{@code Element} 抽象方法变为 {@code getNarration()} + {@code extractRenderState(GuiGraphicsExtractor, …)}；</li>
 *   <li>平铺奶贴图改走 {@code GuiGraphicsExtractor#blitSprite(RenderPipeline, TextureAtlasSprite, …)}——
 *       该重载内部用 {@code sprite.atlasLocation()} 绑定方块图集，跨图集安全。</li>
 * </ul>
 */
public final class FreezerFluidProviders {
    /** 官方从 FreezerRenderer.MILK_STILL_TEXTURE 取贴图；自有奶流体直接用本模组贴图 */
    private static final Identifier MILK_STILL_TEXTURE =
            Identifier.fromNamespaceAndPath("kaleidoscope_world_liquor", "block/milk_still");
    private static final Identifier PROGRESS_BASE = Identifier.fromNamespaceAndPath("jade", "progress_base");

    private FreezerFluidProviders() {
    }

    private static boolean isMilk(BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof FreezerBlockEntity be) {
            return be.tank.getFluidAmountMb() > 0 && be.tank.getFluid() == ModFluids.MILK_STILL;
        }
        return false;
    }

    /**
     * 服务端数据里还留着上一次的奶、而本地 tank 已空（Jade 数据包未刷新的窗口期）——
     * 同样要接管显示，否则会看到「空罐 + 奶条」的错乱叠加。
     */
    private static boolean isStaleMilkData(BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof FreezerBlockEntity be)) {
            return false;
        }
        if (!be.tank.getFluidVariant().isBlank()) {
            // tank 还有东西 → 走 isMilk 分支
            return false;
        }
        CompoundTag data = accessor.getServerData();
        // 26.x：CompoundTag 改 Optional/OrEmpty 风格（无 type 参数的 contains/getList/getCompound）
        if (data.getListOrEmpty("JadeFluidStorage").isEmpty()) {
            return false;
        }
        String milkId = BuiltInRegistries.FLUID.getKey(ModFluids.MILK_STILL).toString();
        for (Tag groupTag : data.getListOrEmpty("JadeFluidStorage")) {
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

    /** 平铺奶贴图的 16×16 覆盖元素（官方 CustomFluidElement 的 26.x 等价实现）。 */
    public static final class CustomFluidElement extends Element {
        public CustomFluidElement() {
            this.width = 16;
            this.height = 16;
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor guiGraphics, int x, int y, float partialTick) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().atlasManager
                    .get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, MILK_STILL_TEXTURE));
            if (sprite != null) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, this.width, this.height);
            }
        }
    }

    public enum TooltipProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public Identifier getUid() {
            return ModJadePlugin.FREEZER_FLUID_UID;
        }

        @Override
        public int getDefaultPriority() {
            return 1001;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            boolean milk = FreezerFluidProviders.isMilk(accessor);
            boolean stale = FreezerFluidProviders.isStaleMilkData(accessor);
            if (milk || stale) {
                tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);
            }
            if (!milk) {
                return;
            }
            if (!(accessor.getBlockEntity() instanceof FreezerBlockEntity be)) {
                return;
            }
            int amount = be.tank.getFluidAmountMb();
            int capacity = (int) (be.tank.getCapacityTransfer()
                    * (long) com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank.MB_PER_BUCKET
                    / FluidConstants.BUCKET);
            if (capacity <= 0) {
                return;
            }
            float ratio = Math.min(1.0F, (float) amount / capacity);
            Component name = Component.translatable("fluid.kaleidoscope_world_liquor.milk");
            Component text;
            if (accessor.showDetails()) {
                text = Component.translatable("jade.fluid",
                                name.copy().withStyle(ChatFormatting.WHITE),
                                Component.literal(amount + "mB / " + capacity + "mB"))
                        .withStyle(ChatFormatting.GRAY);
            } else {
                text = Component.translatable("jade.fluid",
                        name, Component.literal(amount + "mB"));
            }

            ProgressView view = new ProgressView(
                    ProgressView.Part.of(ratio, new CustomFluidElement()),
                    text,
                    JadeUI.progressStyle().fitContentX(false).fitContentY(false),
                    BoxStyle.sprite(PROGRESS_BASE, null, 0));
            tooltip.add(JadeUI.progress(view, 185, 15));
        }
    }
}
