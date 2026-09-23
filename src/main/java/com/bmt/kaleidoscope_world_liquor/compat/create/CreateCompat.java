package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.BarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.block.BarCellarCabinetBlock;
import com.bmt.kaleidoscope_world_liquor.block.ChairBlock;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/**
 * Create 联动总注册（官方 {@code compat/create/CreateCompat} 的移植）。
 * <p>
 * 官方是按 {@code ModBlocks} 字段逐个登记（当时只有 5 种木头的酒柜/酒窖柜）；
 * 本端口的木种多得多（26.3 有 16 吧台凳、14 酒柜、13 酒窖柜），所以改成
 * <b>按方块类遍历注册表</b>登记——木种增减都不用再改这里。
 * <p>
 * 官方的末尾还会在装了 {@code kaleidoscope_contraption} 时调用
 * {@code KaleidoscopeContraptionCompat.register()}；该模组只有 forge/neoforge 构建、
 * 没有 Fabric 版，且它依赖的 Create 侧 API 也无法在 Fabric 上链接，故本端口整块不移植
 * （详见工程文档/记忆里的结论）。
 */
public final class CreateCompat {
    private CreateCompat() {
    }

    public static void register() {
        ChairBlockMovementBehaviour chairMovement = new ChairBlockMovementBehaviour();
        ChairMovingInteraction chairInteraction = new ChairMovingInteraction();
        BarCabinetMovingInteraction cabinetInteraction = new BarCabinetMovingInteraction();
        BarCellarCabinetMovingInteraction cellarCabinetInteraction = new BarCellarCabinetMovingInteraction();

        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof ChairBlock) {
                MovementBehaviour.REGISTRY.register(block, chairMovement);
                MovingInteractionBehaviour.REGISTRY.register(block, chairInteraction);
            } else if (block instanceof BarCabinetBlock) {
                MovingInteractionBehaviour.REGISTRY.register(block, cabinetInteraction);
            } else if (block instanceof BarCellarCabinetBlock) {
                MovingInteractionBehaviour.REGISTRY.register(block, cellarCabinetInteraction);
            }
        }
    }
}
