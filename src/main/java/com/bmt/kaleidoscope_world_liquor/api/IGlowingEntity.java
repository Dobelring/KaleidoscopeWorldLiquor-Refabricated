package com.bmt.kaleidoscope_world_liquor.api;

/**
 * 模组控制的发光状态（与"原版发光来源"分离，冥视切换时不覆盖玩家的
 * 原版发光来源，如发光箭矢）。
 */
public interface IGlowingEntity {
    boolean isModGlowing();

    void setModGlowing(boolean modGlowing);

    void setGlowing(boolean glowing);
}
