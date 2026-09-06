package com.bmt.kaleidoscope_world_liquor.client;

import com.bmt.kaleidoscope_world_liquor.client.render.block.BarCabinetBlockEntityRender;
import com.bmt.kaleidoscope_world_liquor.client.render.block.BarCellarCabinetBlockEntityRender;
import com.bmt.kaleidoscope_world_liquor.client.render.block.FreezerBlockEntityRender;
import com.bmt.kaleidoscope_world_liquor.event.MusicDiscEvents;
import com.bmt.kaleidoscope_world_liquor.init.ModBlockEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModEntities;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
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

        // 冥视：客户端 tick 扫描附近敌对生物发光（1.20.1 RenderTick 同款）
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null || client.isPaused()) {
                return;
            }
            boolean hasEffect = client.player.hasEffect(ModEffects.HOSTILE_DETECTION);
            double rangeSq = 1024.0;
            for (Monster mob : client.level.getEntitiesOfClass(Monster.class,
                    client.player.getBoundingBox().inflate(37.0), mob -> mob.isAlive())) {
                boolean shouldGlow = hasEffect && mob instanceof Enemy && client.player.distanceToSqr(mob) <= rangeSq;
                mob.setGlowingTag(shouldGlow);
            }
        });
    }
}
