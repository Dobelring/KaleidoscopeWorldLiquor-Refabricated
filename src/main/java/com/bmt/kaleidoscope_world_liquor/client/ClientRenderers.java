package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.client.render.block.BarCabinetBlockEntityRender;
import com.bmt.kaleidoscope_world_liquor.client.render.block.BarCellarCabinetBlockEntityRender;
import com.bmt.kaleidoscope_world_liquor.client.render.block.FreezerBlockEntityRender;
import com.bmt.kaleidoscope_world_liquor.event.MusicDiscEvents;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModEntities;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;

/**
 * 客户端渲染接线：BER 注册、椅子 NoopRenderer、唱片 tooltip、敌对发光 tick。
 */
public final class ClientRenderers {
    private ClientRenderers() {
    }

    public static void register() {
        // BER
        BlockEntityRenderers.register(ModBlockEntities.FREEZER_BE, FreezerBlockEntityRender::new);
        BlockEntityRenderers.register(ModBlockEntities.BAR_CABINET_BE, BarCabinetBlockEntityRender::new);
        BlockEntityRenderers.register(ModBlockEntities.BAR_CELLAR_CABINET_BE, BarCellarCabinetBlockEntityRender::new);
        // 椅子
        EntityRenderers.register(ModEntities.CHAIR, NoopRenderer::new);

        // 唱片 tooltip（"可放置"，暗灰斜体）
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (MusicDiscEvents.appendPlaceTooltip(stack)) {
                lines.add(Component.translatable("item.kaleidoscope_world_liquor.music_disc.tooltip")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        });

        // 冥视发光由 HostileDetectionHandler 负责（onInitializeClient 已注册；
        // 此处不再内联一份——重复注册会绕过 IGlowingEntity 的发光分离机制，每 tick 清掉原版发光来源）
    }
}
