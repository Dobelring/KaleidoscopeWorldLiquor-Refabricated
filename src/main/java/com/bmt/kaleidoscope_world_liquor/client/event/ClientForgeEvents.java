package com.bmt.kaleidoscope_world_liquor.client.event;

import com.bmt.kaleidoscope_world_liquor.client.renderer.TreasureSenseRender;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;

/**
 * 原 Forge：{@code @EventBusSubscriber(Dist.CLIENT)} + {@code RenderLevelStageEvent}
 * （Stage.AFTER_CUTOUT_BLOCKS，画宝物感知线框）+ {@code PlayerLoggedOutEvent}（释放 VertexBuffer）。
 *
 * <p>Fabric 映射：
 * <ul>
 *   <li>RenderLevelStageEvent → {@code WorldRenderEvents.BEFORE_ENTITIES}。Fabric 没有
 *       solid/cutout 分段渲染事件，实体前回调点是与原两个 Forge 阶段（AFTER_SOLID_BLOCKS /
 *       AFTER_CUTOUT_BLOCKS 均在实体之前）时间上最接近的统一位置。</li>
 *   <li>PlayerLoggedOutEvent → {@code ClientPlayConnectionEvents.DISCONNECT}。原 Forge 事件是
 *       服务端侧事件（{@code getEntity().level().isClientSide()} 恒为 false），其 freeBuffer
 *       实际不会执行；Fabric 的 DISCONNECT 只在客户端断开时触发，等价于原代码意图的客户端分支。</li>
 * </ul>
 */
@Environment(EnvType.CLIENT)
public class ClientForgeEvents {
    // 幂等守卫：避免重复注册导致线框一帧多次叠加绘制
    private static boolean registered = false;

    public ClientForgeEvents() {
    }

    /**
     * 原 Forge 的 @SubscribeEvent 注册；由主线的客户端入口类调用。
     * <p>
     * 注：KaleidoscopeWorldLiquorClient 目前只调用本方法与 CameraAnglesEvent.register()，
     * 未直接调用 TreasureSenseRender.register()，故在这里代为调用（其内部有幂等守卫），
     * 否则发光队伍的客户端 tick 钩子会漏挂。
     */
    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        TreasureSenseRender.register();
        WorldRenderEvents.BEFORE_ENTITIES.register(ClientForgeEvents::onRenderLevelStage);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onPlayerLoggedOut());
    }

    public static void onRenderLevelStage(WorldRenderContext event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            if (mc.player.hasEffect(ModEffects.TREASURE_SENSE_EFFECT)) {
                TreasureSenseRender.renderTreasures(event);
            }
        }
    }

    // 原签名 onPlayerLoggedOut(PlayerLoggedOutEvent)：Fabric DISCONNECT 只在客户端触发，
    // 等价于原 isClientSide() 分支，事件对象本身在原方法里只用于该判断。
    public static void onPlayerLoggedOut() {
        TreasureSenseRender.freeBuffer();
    }
}
