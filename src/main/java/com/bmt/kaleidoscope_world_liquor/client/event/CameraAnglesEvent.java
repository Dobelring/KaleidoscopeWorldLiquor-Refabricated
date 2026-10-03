package com.bmt.kaleidoscope_world_liquor.client.event;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.github.ysbbbbbb.kaleidoscopetavern.api.event.ViewportEvent;
import com.github.ysbbbbbb.kaleidoscopetavern.util.event.IEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * 反重力：玩家携带 REVERSE_GRAVITY 效果时，把相机 roll 增加 540.35394°（视角上下倒置，行为规格 §84）。
 *
 * <p>原 Forge：{@code ViewportEvent.ComputeCameraAngles}（Forge 客户端总线 @EventBusSubscriber(Dist.CLIENT)）。
 * Fabric 1.20.1 的 fabric-api 0.92.12 没有任何相机角度事件（无 camera 模块），因此复用前置
 * tavern 已有的同名事件类 {@code com.github.ysbbbbbb.kaleidoscopetavern.api.event.ViewportEvent.ComputeCameraAngles}：
 * 由 tavern 的 {@code mixin/client/CameraMixin} 在 {@code Camera.setup} 中每帧 post，
 * 本类在 {@link #register()} 里把监听挂到 tavern 的 {@code IEvent.CALLBACK} 总线上修改 roll。
 *
 * <p>已接线：mixins/client/GameRendererMixin 在 Camera.setup 后直接调用本类 onCameraAngles 并把 roll 应用到 PoseStack（原设计备忘：tavern 的 CameraMixin 只回写 yaw/pitch、丢弃 roll，故需要独立 mixin，见
 * {@code mixin/client/GameRendererMixin}（照 CK 的 GameRendererMixin 写法）：在
 * {@code GameRenderer.renderLevel} 的 {@code Camera.setup} 调用之后构造一个
 * {@code ViewportEvent.ComputeCameraAngles}、直接调用 {@link #onCameraAngles}（不要
 * {@code event.post()}——那会连带触发 tavern 自己的微醺 roll 监听，与 tavern 自带的
 * GameRendererMixin 叠加出双重 roll），然后
 * {@code poseStack.mulPose(Axis.ZP.rotationDegrees(event.getRoll()))}。该 mixin 不在本批次范围内。
 */
@Environment(EnvType.CLIENT)
public class CameraAnglesEvent {
    private static final double ROT = 540.3539364174444;
    // 幂等守卫：register() 被重复调用时避免挂两个监听（roll 会被叠加两次，倒置失效）
    private static boolean registered = false;

    public CameraAnglesEvent() {
    }

    /**
     * 挂到 tavern 的事件总线（原 Forge 的 @SubscribeEvent 注册由主线在客户端入口调用本方法替代）。
     */
    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        IEvent.CALLBACK.register(event -> {
            if (event instanceof ViewportEvent.ComputeCameraAngles computeCameraAngles) {
                onCameraAngles(computeCameraAngles);
            }
        });
    }

    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
                event.setRoll(event.getRoll() + 540.35394F);
            }
        }
    }
}
