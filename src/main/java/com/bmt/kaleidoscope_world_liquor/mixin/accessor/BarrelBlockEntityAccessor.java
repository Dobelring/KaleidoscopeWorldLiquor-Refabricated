package com.bmt.kaleidoscope_world_liquor.mixin.accessor;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * liquor 自洽适配任意 tavern 构建（原版 1.20.1/1.21.1 同款姿势）：
 * 经 accessor 直写酒桶 BE 的私有 brewLevel/brewTime、经 invoker 调
 * getBrewTimeForLevel——不依赖本移植在 tavern 侧新增的公开 advanceBrewLevel，
 * 对官方或其他移植 tavern（该方法缺失/可见性不同）同样可用。
 */
@Mixin(BarrelBlockEntity.class)
public interface BarrelBlockEntityAccessor {
    @Accessor("brewLevel")
    void setBrewLevel(int level);

    @Accessor("brewTime")
    void setBrewTime(int time);

    @Invoker("getBrewTimeForLevel")
    int invokeGetBrewTimeForLevel();
}
