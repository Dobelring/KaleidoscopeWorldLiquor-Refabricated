package com.bmt.kaleidoscope_world_liquor.mixins.accessor;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * addRenderableWidget 声明在 Screen（非 CreativeModeInventoryScreen 自身），
 * 故 invoker 目标类为 Screen（同理：@Invoker 也只查目标类自身声明）。
 */
@Mixin({Screen.class})
public interface ScreenAddWidgetAccessor {
    @Invoker("addRenderableWidget")
    <T extends GuiEventListener & Renderable & NarratableEntry> T kwl$addRenderableWidget(T widget);
}
