package com.bmt.kaleidoscope_world_liquor.item;

import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;

public abstract class CustomDrinkItem extends DrinkBlockItem {
    public CustomDrinkItem(Block block) {
        super(block);
    }

    protected abstract SoundEvent getCustomSound();

    public SoundEvent getDrinkingSound() {
        return this.getCustomSound();
    }

    public SoundEvent getEatingSound() {
        return this.getCustomSound();
    }

    public static class CoolTea extends CustomDrinkItem {
        public CoolTea(Block block) {
            super(block);
        }

        @Override
        protected SoundEvent getCustomSound() {
            return ModSounds.COOL_ICE_TEA_DRINK;
        }
    }

    public static class IceTea extends CustomDrinkItem {
        public IceTea(Block block) {
            super(block);
        }

        @Override
        protected SoundEvent getCustomSound() {
            return ModSounds.ICE_TEA_EAT;
        }
    }

    public static class SourPlum extends CustomDrinkItem {
        public SourPlum(Block block) {
            super(block);
        }

        @Override
        protected SoundEvent getCustomSound() {
            return ModSounds.SOUR_PLUM_DRINK;
        }
    }
}
