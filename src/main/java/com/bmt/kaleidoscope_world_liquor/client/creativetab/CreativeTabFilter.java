package com.bmt.kaleidoscope_world_liquor.client.creativetab;

import com.bmt.kaleidoscope_world_liquor.init.ModCreativeModeTabs;
import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import com.bmt.kaleidoscope_world_liquor.util.PortHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 创造界面左侧的「酒水 / 装饰」过滤按钮（官方 1.1.9 同款表现）。
 * 点击后把 {@code ItemPickerMenu.items} 整体换成所选分类的快照并 scrollTo(0)。
 *
 * <p>26.1.2 适配：GUI 已 extract 化，按钮自绘走 {@code extractWidgetRenderState} +
 * {@code blitSprite(RenderPipeline, …)}；当前 tab 由 Fabric 的
 * {@link FabricCreativeModeInventoryScreen#getSelectedTab()} 读取（原版 selectedTab 是 private static，无需 accessor）。
 */
@Environment(EnvType.CLIENT)
public final class CreativeTabFilter {
    private static final Identifier SELECTED_FILTER_TAB = PortHelper.id("filter_tab_selected");
    private static final Identifier UNSELECTED_FILTER_TAB = PortHelper.id("filter_tab_unselected");
    private static final int BUTTON_WIDTH = 32;
    private static final int BUTTON_HEIGHT = 26;
    private static final List<FilterButton> BUTTONS = new ArrayList<>();
    private static CreativeModeTab lastTab;
    private static Category selectedCategory = Category.LIQUOR;

    private CreativeTabFilter() {
    }

    /** 由 CreativeModeInventoryScreenMixin 在 init() TAIL 调用（此时 leftPos/topPos 已就绪）。 */
    public static List<Button> createButtons(int leftPos, int topPos) {
        BUTTONS.clear();
        int x = leftPos - 28;
        BUTTONS.add(new FilterButton(x, topPos + 17, Category.LIQUOR));
        BUTTONS.add(new FilterButton(x, topPos + 44, Category.FURNITURE));
        return List.copyOf(BUTTONS);
    }

    /**
     * 由 CreativeModeInventoryScreenMixin 在 init() TAIL 调用（此时 leftPos/topPos 已就绪）。
     * 这里**无条件**按当前 tab 同步一次：创造界面每次打开都会重建 ItemPickerMenu，
     * 只靠"tab 变了才刷新"会在重开界面时留下未过滤的完整列表。
     */
    public static void onScreenInit(CreativeModeInventoryScreen screen) {
        CreativeModeTab tab = ((FabricCreativeModeInventoryScreen) screen).getSelectedTab();
        lastTab = tab;
        onSwitchTab(tab, screen);
    }

    /** 每帧同步（由 mixin 在 extractRenderState TAIL 调用）：只在 tab 切换时刷新按钮可见性与物品列表。 */
    public static void sync(CreativeModeInventoryScreen screen) {
        CreativeModeTab tab = ((FabricCreativeModeInventoryScreen) screen).getSelectedTab();
        if (lastTab != tab) {
            onSwitchTab(tab, screen);
            lastTab = tab;
        }
    }

    private static void onSwitchTab(CreativeModeTab tab, CreativeModeInventoryScreen screen) {
        boolean isOurTab = tab != null && tab == BuiltInRegistries.CREATIVE_MODE_TAB
                .getValue(ModCreativeModeTabs.KALEIDOSCOPE_WORLD_LIQUOR_TAB.identifier());
        for (FilterButton button : BUTTONS) {
            button.visible = isOurTab;
        }
        if (isOurTab) {
            refreshItems(screen);
        }
    }

    private static void select(Category category) {
        selectedCategory = category;
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen screen) {
            refreshItems(screen);
        }
    }

    private static void refreshItems(CreativeModeInventoryScreen screen) {
        NonNullList<ItemStack> items = screen.getMenu().items;
        items.clear();
        for (ItemStack stack : selectedCategory.items) {
            items.add(stack.copy());
        }
        screen.getMenu().scrollTo(0.0F);
    }

    private enum Category {
        LIQUOR(ModCreativeModeTabs.LIQUOR_ITEMS,
                () -> new ItemStack(ModItems.BOMBAY_SAPPHIRE_GIN),
                Component.translatable("gui.kaleidoscope_world_liquor.filter.liquor")),
        FURNITURE(ModCreativeModeTabs.FURNITURE_ITEMS,
                () -> new ItemStack(ModItems.BAR_STOOL_WHITE),
                Component.translatable("gui.kaleidoscope_world_liquor.filter.furniture"));

        private final NonNullList<ItemStack> items;
        private final Supplier<ItemStack> icon;
        private final Component title;

        Category(NonNullList<ItemStack> items, Supplier<ItemStack> icon, Component title) {
            this.items = items;
            this.icon = icon;
            this.title = title;
        }
    }

    private static final class FilterButton extends Button {
        private final Category category;

        private FilterButton(int x, int y, Category category) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, CommonComponents.EMPTY, button -> select(category), DEFAULT_NARRATION);
            this.category = category;
            this.setTooltip(Tooltip.create(category.title));
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
            extractor.blitSprite(RenderPipelines.GUI_TEXTURED,
                    selectedCategory == this.category ? SELECTED_FILTER_TAB : UNSELECTED_FILTER_TAB,
                    this.getX(), this.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
            extractor.item(this.category.icon.get(), this.getX() + 8, this.getY() + 5);
        }
    }
}
