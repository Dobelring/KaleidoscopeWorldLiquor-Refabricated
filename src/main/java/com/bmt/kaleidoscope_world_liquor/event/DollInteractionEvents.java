package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight.KTItems;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.phys.BlockHitResult;

/**
 * 原 Forge 版 @EventBusSubscriber 的 PlayerInteractEvent.RightClickBlock 监听 →
 * Fabric 的 UseBlockCallback（本类不 import 任何 doll 类，纯 id 匹配，门控语义不变）。
 * 原 setCanceled(true) + setCancellationResult(SUCCESS) → 命中时直接返回 SUCCESS；
 * 官方整个方法体在 !isClientSide 内（客户端不取消、返回 PASS），与原一致。
 */
public class DollInteractionEvents {
    private static final Map<BlockPos, Long> DOLL_FEED_COOLDOWNS = new HashMap<>();
    private static final int FEED_COOLDOWN_TICKS = 20;
    private static final Set<ResourceLocation> SUPPORTED_DOLL_4_IDS = new HashSet<>();

    public DollInteractionEvents() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register(DollInteractionEvents::onRightClickDoll4);
    }

    /** 原 onRightClickDoll4(RightClickBlock) → UseBlockCallback（波奇布丁右键 doll_4：音效+粒子+幸运）。 */
    private static InteractionResult onRightClickDoll4(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            ItemStack heldItem = player.getItemInHand(hand);
            BlockPos pos = hitResult.getBlockPos();
            Block clickedBlock = level.getBlockState(pos).getBlock();
            ResourceLocation heldItemId = BuiltInRegistries.ITEM.getKey(heldItem.getItem());
            if (KTItems.POCHI_PUDDING_ID.equals(heldItemId)) {
                ResourceLocation clickedBlockId = BuiltInRegistries.BLOCK.getKey(clickedBlock);
                if (SUPPORTED_DOLL_4_IDS.contains(clickedBlockId)) {
                    if (hand == player.getUsedItemHand()) {
                        long currentTime = level.getGameTime();
                        if (!DOLL_FEED_COOLDOWNS.containsKey(pos) || currentTime >= DOLL_FEED_COOLDOWNS.get(pos)) {
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
                            DOLL_FEED_COOLDOWNS.put(pos, currentTime + 20L);
                            return InteractionResult.SUCCESS; // 原 setCanceled + setCancellationResult(SUCCESS)
                        }
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    static {
        SUPPORTED_DOLL_4_IDS.add(KaleidoscopeWorldLiquor.id("doll_4"));
        SUPPORTED_DOLL_4_IDS.add(new ResourceLocation("kaleidoscope_nether", "doll_4"));
    }
}
