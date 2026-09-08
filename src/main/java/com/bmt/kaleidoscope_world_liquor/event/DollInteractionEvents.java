package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight.KTItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 波奇布丁喂玩偶 doll_4（doll 模组在场时其方块 id；1.20.1 还支持 nether:doll_4）。
 * 冷却 20 tick / 方块位置；吃掉布丁→还碗→音效+爱心粒子→给幸运 2652t。
 */
public final class DollInteractionEvents {
    private static final Map<BlockPos, Long> DOLL_FEED_COOLDOWNS = new HashMap<>();
    private static final int FEED_COOLDOWN_TICKS = 20;
    private static final Set<Identifier> SUPPORTED_DOLL_4_IDS = new HashSet<>();

    private DollInteractionEvents() {
    }

    public static void register() {
        SUPPORTED_DOLL_4_IDS.add(KaleidoscopeWorldLiquor.id("doll_4"));
        SUPPORTED_DOLL_4_IDS.add(Identifier.fromNamespaceAndPath("kaleidoscope_nether", "doll_4"));

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            Level level = world;
            if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            ItemStack heldItem = player.getItemInHand(hand);
            if (!heldItem.is(KTItems.POCHI_PUDDING)) {
                return InteractionResult.PASS;
            }
            BlockPos pos = hitResult.getBlockPos();
            Block clickedBlock = level.getBlockState(pos).getBlock();
            Identifier clickedBlockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(clickedBlock);
            if (!SUPPORTED_DOLL_4_IDS.contains(clickedBlockId)) {
                return InteractionResult.PASS;
            }
            long currentTime = level.getGameTime();
            Long cooldownUntil = DOLL_FEED_COOLDOWNS.get(pos);
            if (cooldownUntil != null && currentTime < cooldownUntil) {
                return InteractionResult.PASS;
            }
            if (!(level instanceof ServerLevel serverLevel)) {
                return InteractionResult.PASS;
            }
            if (!player.isCreative()) {
                heldItem.shrink(1);
                ItemStack bowl = new ItemStack(Items.BOWL);
                if (!player.getInventory().add(bowl)) {
                    player.drop(bowl, false);
                }
            }
            level.playSound(null, pos, ModSounds.POCHI_PUDDING_FEED, SoundSource.PLAYERS, 1.0F, 1.0F);
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.5;
            double z = pos.getZ() + 0.5;
            serverLevel.sendParticles(ParticleTypes.HEART, x, y, z, 7, 0.4, 0.15, 0.4, 0.15);
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 2652, 0, false, true));
            DOLL_FEED_COOLDOWNS.put(pos, currentTime + FEED_COOLDOWN_TICKS);
            return InteractionResult.SUCCESS;
        });
    }
}
