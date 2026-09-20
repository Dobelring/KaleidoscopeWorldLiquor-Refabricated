package com.bmt.kaleidoscope_world_liquor.client.creativetab;

import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModCreativeModeTabs;
import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * 创造栏左侧的「酒水 / 装饰」过滤按钮：把合并后的单一 tab 内容按分类切换。
 * 对应官方 1.1.9 的 client/creativetab/CreativeTabFilter，但按钮挂载改由
 * CreativeModeInventoryScreenMixin 在 init() 末尾驱动（Fabric 没有 ScreenEvent.Init.Post，
 * 且原版 Screen#addRenderableWidget 是 protected）。
 */
@Environment(EnvType.CLIENT)
public final class CreativeTabFilter {
   private static final ResourceLocation SELECTED_FILTER_TAB = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "filter_tab_selected");
   private static final ResourceLocation UNSELECTED_FILTER_TAB = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "filter_tab_unselected");
   private static final List<FilterButton> BUTTONS = new ArrayList<>();
   private static CreativeModeTab lastTab;
   private static Category selectedCategory = Category.LIQUOR;

   private CreativeTabFilter() {
   }

   /**
    * 建好两个过滤按钮并返回，由 CreativeModeInventoryScreenMixin 在 init() 末尾挂到屏幕上
    * （原版 Screen#addRenderableWidget 是 protected，只有 mixin 侧能调）。
    */
   public static List<Button> createButtons(int guiLeft, int guiTop) {
      BUTTONS.clear();
      int x = guiLeft - 28;
      BUTTONS.add(new FilterButton(x, guiTop + 17, Category.LIQUOR));
      BUTTONS.add(new FilterButton(x, guiTop + 44, Category.FURNITURE));
      return new ArrayList<>(BUTTONS);
   }

   /** 按钮挂载完成后调用：同步可见性并填充当前分类内容。 */
   public static void onScreenInit(CreativeModeInventoryScreen screen) {
      lastTab = null;
      onSwitchTab(getSelectedTab(screen), screen);
   }

   public static void onScreenRender(CreativeModeInventoryScreen screen) {
      CreativeModeTab tab = getSelectedTab(screen);
      if (lastTab != tab) {
         onSwitchTab(tab, screen);
         lastTab = tab;
      }
   }

   private static CreativeModeTab getSelectedTab(CreativeModeInventoryScreen screen) {
      return ((FabricCreativeInventoryScreen)screen).getSelectedItemGroup();
   }

   private static void onSwitchTab(CreativeModeTab tab, CreativeModeInventoryScreen screen) {
      boolean isOurTab = tab == ModCreativeModeTabs.KALEIDOSCOPE_WORLD_LIQUOR_TAB;
      BUTTONS.forEach(button -> button.visible = isOurTab);
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
      LIQUOR(
         ModCreativeModeTabs.LIQUOR_ITEMS,
         () -> new ItemStack((ItemLike)ModItems.BOMBAY_SAPPHIRE_GIN),
         Component.translatable("gui.kaleidoscope_world_liquor.filter.liquor")
      ),
      FURNITURE(
         ModCreativeModeTabs.FURNITURE_ITEMS,
         () -> new ItemStack((ItemLike)ModBlocks.BAR_STOOL_WHITE),
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
      private final Category category;

      protected FilterButton(int x, int y, Category category) {
         super(x, y, 32, 26, CommonComponents.EMPTY, button -> CreativeTabFilter.select(category), DEFAULT_NARRATION);
         this.category = category;
         this.setTooltip(Tooltip.create(category.title));
      }

      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         graphics.blitSprite(
            selectedCategory == this.category ? SELECTED_FILTER_TAB : UNSELECTED_FILTER_TAB,
            this.getX(),
            this.getY(),
            32,
            26
         );
         graphics.renderItem(this.category.icon.get(), this.getX() + 8, this.getY() + 5);
      }
   }
}
