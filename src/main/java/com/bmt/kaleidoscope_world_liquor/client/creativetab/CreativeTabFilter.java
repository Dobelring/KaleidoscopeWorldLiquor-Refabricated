package com.bmt.kaleidoscope_world_liquor.client.creativetab;

import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModCreativeModeTabs;
import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.AbstractContainerScreenAccessor;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.CreativeModeInventoryScreenAccessor;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.ScreenAddWidgetAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.joml.Matrix4f;

/**
 * 创造栏分类筛选器（原 Forge {@code client/creativetab/CreativeTabFilter}，官方 1.1.11 内置）：
 * 选中本模组创造栏时，在窗口左缘显示两个按钮（酒水 / 装饰），点击后把标签页物品列表
 * 替换为 {@link ModCreativeModeTabs#LIQUOR_ITEMS} / {@link ModCreativeModeTabs#FURNITURE_ITEMS}
 * 之一——即"左侧两个按钮划分两类物品"。
 *
 * <p>Fabric 转换：Forge 的 ScreenEvent.Init/Render.Post → {@code ScreenEvents.AFTER_INIT}
 * （注入按钮）+ 每屏 {@code ScreenEvents.beforeRender}（标签页切换检测）；Forge 的
 * getGuiLeft/getGuiTop/getSelectedTab → {@link CreativeModeInventoryScreenAccessor}；
 * ItemPickerMenu#items 与 scrollTo 在 1.20.1 原版即 public，可直用。
 */
@Environment(EnvType.CLIENT)
public class CreativeTabFilter {
    private static final ResourceLocation VANILLA_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static final List<CreativeTabFilter.FilterButton> BUTTONS = new ArrayList<>();
    /** 已挂过 beforeRender 回调的屏幕实例（弱引用）：窗口 resize 会触发同实例重复 init，回调只挂一次 */
    private static final Set<Screen> REGISTERED_SCREENS = Collections.newSetFromMap(new WeakHashMap<>());
    private static CreativeModeTab lastTab;
    private static CreativeTabFilter.Category selectedCategory = CreativeTabFilter.Category.LIQUOR;

    public CreativeTabFilter() {
    }

    /** 由 KaleidoscopeWorldLiquorClient 调用（客户端事件，服务端不可注册）。 */
    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof CreativeModeInventoryScreen creativeScreen)) {
                return;
            }

            // 原版 init 会 clearWidgets，按钮每次 AFTER_INIT 都要重加
            BUTTONS.clear();
            AbstractContainerScreenAccessor container = (AbstractContainerScreenAccessor) creativeScreen;
            int buttonX = container.kwl$getLeftPos() - 28;
            int top = container.kwl$getTopPos();
            BUTTONS.add(new CreativeTabFilter.FilterButton(buttonX, top + 17, CreativeTabFilter.Category.LIQUOR));
            BUTTONS.add(new CreativeTabFilter.FilterButton(buttonX, top + 44, CreativeTabFilter.Category.FURNITURE));
            ScreenAddWidgetAccessor widgets = (ScreenAddWidgetAccessor) creativeScreen;
            BUTTONS.forEach(widgets::kwl$addRenderableWidget);

            // 原 Forge ScreenEvent.Render.Post：每帧检测标签页切换（选中本模组页时按钮显隐+刷新列表）
            // 同一屏幕实例重复 init（resize）时 Fabric 的 beforeRender 事件列表会累加，这里按实例去重
            if (REGISTERED_SCREENS.add(creativeScreen)) {
                ScreenEvents.beforeRender(screen).register((s, graphics, mouseX, mouseY, delta) -> {
                    CreativeModeTab tab = CreativeModeInventoryScreenAccessor.kwl$getSelectedTab();
                    if (lastTab != tab) {
                        onSwitchTab(tab, creativeScreen);
                        lastTab = tab;
                    }
                });
            }
            onSwitchTab(CreativeModeInventoryScreenAccessor.kwl$getSelectedTab(), creativeScreen);
        });
    }

    private static void onSwitchTab(CreativeModeTab tab, CreativeModeInventoryScreen screen) {
        boolean isOurTab = tab == ModCreativeModeTabs.KALEIDOSCOPE_WORLD_LIQUOR_TAB;
        BUTTONS.forEach(button -> button.visible = isOurTab);
        if (isOurTab) {
            refreshItems(screen);
        }
    }

    private static void select(CreativeTabFilter.Category category) {
        selectedCategory = category;
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen screen) {
            refreshItems(screen);
        }
    }

    private static void refreshItems(CreativeModeInventoryScreen screen) {
        NonNullList<ItemStack> items = ((ItemPickerMenu) screen.getMenu()).items;
        items.clear();

        for (ItemStack stack : selectedCategory.items) {
            items.add(stack.copy());
        }

        ((ItemPickerMenu) screen.getMenu()).scrollTo(0.0F);
    }

    private enum Category {
        LIQUOR(
            ModCreativeModeTabs.LIQUOR_ITEMS,
            () -> new ItemStack((ItemLike) ModItems.BOMBAY_SAPPHIRE_GIN),
            Component.translatable("gui.kaleidoscope_world_liquor.filter.liquor")
        ),
        FURNITURE(
            ModCreativeModeTabs.FURNITURE_ITEMS,
            () -> new ItemStack((ItemLike) ModBlocks.BAR_STOOL_WHITE),
            Component.translatable("gui.kaleidoscope_world_liquor.filter.furniture")
        );

        private final NonNullList<ItemStack> items;
        private final Supplier<ItemStack> icon;
        private final Component title;

        Category(NonNullList<ItemStack> items, Supplier<ItemStack> icon, Component title) {
            this.items = items;
            this.icon = icon;
            this.title = title;
        }
    }

    private static class FilterButton extends Button {
        private final CreativeTabFilter.Category category;

        protected FilterButton(int x, int y, CreativeTabFilter.Category category) {
            super(x, y, 32, 26, CommonComponents.EMPTY, button -> CreativeTabFilter.select(category), DEFAULT_NARRATION);
            this.category = category;
            this.setTooltip(Tooltip.create(category.title));
        }

        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            boolean active = CreativeTabFilter.selectedCategory == this.category;
            int textureX = 26;
            int textureY = active ? 32 : 0;
            int textureWidth = active ? 32 : 28;
            int textureHeight = 26;
            RenderSystem.setShaderTexture(0, CreativeTabFilter.VANILLA_TABS);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            this.drawRotatedTexture(
                graphics.pose().last().pose(), this.getX(), this.getY(), textureX, textureY, textureWidth, textureHeight
            );
            graphics.renderItem(this.category.icon.get(), this.getX() + 8, this.getY() + 5);
        }

        private void drawRotatedTexture(Matrix4f matrix4f, int x, int y, int textureX, int textureY, int textureWidth, int textureHeight) {
            float scaleX = 0.00390625F;
            float scaleY = 0.00390625F;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(matrix4f, x, y + textureHeight, 0.0F).uv((textureX + textureHeight) * scaleX, textureY * scaleY).endVertex();
            builder.vertex(matrix4f, x + textureWidth, y + textureHeight, 0.0F)
                .uv((textureX + textureHeight) * scaleX, ((float) textureY + textureWidth) * scaleY).endVertex();
            builder.vertex(matrix4f, x + textureWidth, y, 0.0F).uv(textureX * scaleX, (textureY + textureWidth) * scaleY).endVertex();
            builder.vertex(matrix4f, x, y, 0.0F).uv(textureX * scaleX, textureY * scaleY).endVertex();
            BufferUploader.drawWithShader(builder.end());
        }

        protected ClientTooltipPositioner createTooltipPositioner() {
            return DefaultTooltipPositioner.INSTANCE;
        }
    }
}
