package com.bmt.kaleidoscope_world_liquor.fluids;

import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 牛奶流体（结构照 1.21.11 原版 WaterFluid：Still/Flowing 内部类）。
 * 桶 = 原版牛奶桶（倒出后不再回收原桶，与 1.20.1 一致）。
 */
public abstract class MilkFluid extends FlowingFluid {
    public static final Fluid STILL = new MilkFluid.Still();
    public static final Fluid FLOWING = new MilkFluid.Flowing();

    @Override
    public Fluid getFlowing() {
        return FLOWING;
    }

    @Override
    public Fluid getSource() {
        return STILL;
    }

    @Override
    public Item getBucket() {
        return Items.MILK_BUCKET;
    }

    @Override
    public void animateTick(@NotNull Level level, @NotNull BlockPos pos, @NotNull FluidState state, @NotNull RandomSource random) {
        if (!state.isSource() && random.nextInt(64) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SPLASH, x, y, z, 0.0, 0.0, 0.0);
        }
        // 与 1.20.1 一致：牛奶仅保留落水音效行为（无水下气泡粒子）
        if (random.nextInt(10) == 0 && level.getFluidState(pos.below()).is(this)) {
            SoundEvent sound = SoundEvents.WATER_AMBIENT;
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound, SoundSource.BLOCKS,
                    random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
        }
    }

    @Override
    public ParticleOptions getDripParticle() {
        return ParticleTypes.FALLING_DRIPSTONE_WATER;
    }

    @Override
    protected boolean canConvertToSource(@NotNull net.minecraft.server.level.ServerLevel level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(@NotNull net.minecraft.world.level.LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state) {
        net.minecraft.world.level.block.Block.dropResources(state, level, pos, null);
    }

    @Override
    public int getSlopeFindDistance(@NotNull LevelReader level) {
        return 4;
    }

    @Override
    public @NotNull BlockState createLegacyBlock(@NotNull FluidState state) {
        return ModBlocks.MILK_LIQUID_BLOCK.defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(@NotNull Fluid fluid) {
        return fluid == STILL || fluid == FLOWING;
    }

    @Override
    public int getDropOff(@NotNull LevelReader level) {
        return 1;
    }

    @Override
    public int getTickDelay(@NotNull LevelReader level) {
        return 5;
    }

    @Override
    public boolean canBeReplacedWith(@NotNull FluidState state, @NotNull net.minecraft.world.level.BlockGetter level, @NotNull BlockPos pos, @NotNull Fluid fluid, @NotNull net.minecraft.core.Direction direction) {
        return false;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    public @NotNull Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    @Override
    public @NotNull Vec3 getFlow(@NotNull net.minecraft.world.level.BlockGetter level, @NotNull BlockPos pos, @NotNull FluidState state) {
        return super.getFlow(level, pos, state);
    }

    public static class Still extends MilkFluid {
        @Override
        public boolean isSource(@NotNull FluidState state) {
            return true;
        }

        @Override
        public int getAmount(@NotNull FluidState state) {
            return 8;
        }
    }

    public static class Flowing extends MilkFluid {
        @Override
        protected void createFluidStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public boolean isSource(@NotNull FluidState state) {
            return false;
        }

        @Override
        public int getAmount(@NotNull FluidState state) {
            return state.getValue(FlowingFluid.LEVEL);
        }
    }
}
