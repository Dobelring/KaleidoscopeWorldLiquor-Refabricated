package com.bmt.kaleidoscope_world_liquor.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * 通用标记型/匀速型效果基类，覆盖 1.20.1 里 12 个匿名效果类的行为：
 * tequila / captain_gift / treasure_guide / multi_jump / reverse_gravity /
 * boating_master / hostile_detection / beheading / frost_walker /
 * bonemeal_spreader / treasure_sense / ground_crit。
 * tick 节奏与 1.20.1 的 shouldApplyEffectTickThisTick 一一对应；
 * 具体行为逻辑在事件层（第 9 步）接线。
 */
public class BaseEffect extends MobEffect {
    /** 每 tick 触发（原版 m_6584_ 返回 true 的效果） */
    public static final int TICK_EVERY = 1;
    /** 永不 tick（纯标记效果） */
    public static final int TICK_NEVER = -1;
    /** 每秒触发（bonemeal_spreader 的 duration % 20 == 0） */
    public static final int TICK_PER_SECOND = 20;

    private final int tickInterval;

    public BaseEffect(MobEffectCategory category, int color, int tickInterval) {
        super(category, color);
        this.tickInterval = tickInterval;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        if (this.tickInterval == TICK_NEVER) {
            return false;
        }
        if (this.tickInterval == TICK_EVERY) {
            return true;
        }
        return duration % this.tickInterval == 0;
    }

    @Override
    public boolean applyEffectTick(@NotNull ServerLevel level, @NotNull LivingEntity entity, int amplifier) {
        // 行为在事件层实现（hostile_detection 发光 / bonemeal 扩散 / treasure_sense 线框等）
        return true;
    }
}
