package com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * kaleidoscope_twilight 缺失时，为本模组代注册的 twilight 食物补 tooltip。
 * <p>
 * 原 Forge 是 {@code @EventBusSubscriber(Bus.FORGE, value = Dist.CLIENT)}
 * + {@code ItemTooltipEvent}；Fabric 改为 {@code ItemTooltipCallback}
 * （客户端专用 API），本类整体 {@code @Environment(CLIENT)}，
 * 并在<b>客户端入口点</b>调用 {@link #register()} 注册
 * （服务端不能加载本类，与原版 Dist.CLIENT 守卫语义一致）。
 */
@Environment(EnvType.CLIENT)
public class TooltipEvents {
    public TooltipEvents() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, tooltipFlag, lines) -> {
            if (!FabricLoader.getInstance().isModLoaded("kaleidoscope_twilight")) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (KTItems.LIANGSHAN_ICE_CONE_ID.equals(itemId)) {
                    lines.add(
                        Component.translatable("item.kaleidoscope_twilight.liangshan_ice_cone.tooltip")
                            .withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
                    );
                }

                if (KTItems.KITA_STUFFED_CRISP_ID.equals(itemId)) {
                    lines.add(
                        Component.translatable("item.kaleidoscope_twilight.kita_stuffed_crisp.tooltip")
                            .withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
                    );
                }

                if (KTItems.POCHI_PUDDING_ID.equals(itemId)) {
                    lines.add(
                        Component.translatable("item.kaleidoscope_twilight.pochi_pudding.tooltip")
                            .withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
                    );
                }

                if (KTItems.MAGIC_CRISPY_CORNER_ID.equals(itemId)) {
                    lines.add(
                        Component.translatable("item.kaleidoscope_twilight.magic_crispy_corner.tooltip")
                            .withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
                    );
                }
            }
        });
    }
}
