package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 波奇布丁喂玩偶：手持波奇布丁右键 doll_4（本模组或 kaleidoscope_nether 的）
 * 触发特殊音效 + 爱心粒子 + 幸运效果 + 消耗布丁并返还碗（20 tick 冷却）。
 * 对应原版 Forge 的 DollInteractionEvents。
 */
public class DollInteractionEvents {
   private static final Map<BlockPos, Long> DOLL_FEED_COOLDOWNS = new HashMap<>();
   private static final int FEED_COOLDOWN_TICKS = 20;
   private static final Set<ResourceLocation> SUPPORTED_DOLL_4_IDS = new HashSet<>();

   private DollInteractionEvents() {
   }

   public static void register() {
      UseBlockCallback.EVENT.register(DollInteractionEvents::onRightClickDoll4);
   }

   private static InteractionResult onRightClickDoll4(
      Player player, Level level, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult
   ) {
      if (level.isClientSide) {
         return InteractionResult.PASS;
      }

      ItemStack heldItem = player.getItemInHand(hand);
      ResourceLocation heldItemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(heldItem.getItem());
      if (!ResourceLocation.fromNamespaceAndPath("kaleidoscope_twilight", "pochi_pudding").equals(heldItemId)) {
         return InteractionResult.PASS;
      }

      BlockPos pos = hitResult.getBlockPos();
      BlockState clickedState = level.getBlockState(pos);
      Block clickedBlock = clickedState.getBlock();
      ResourceLocation clickedBlockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(clickedBlock);
      if (!SUPPORTED_DOLL_4_IDS.contains(clickedBlockId)) {
         return InteractionResult.PASS;
      }

      if (hand != player.getUsedItemHand()) {
         return InteractionResult.PASS;
      }

      long currentTime = level.getGameTime();
      if (DOLL_FEED_COOLDOWNS.containsKey(pos) && currentTime < DOLL_FEED_COOLDOWNS.get(pos)) {
         return InteractionResult.SUCCESS;
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
      if (level instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(ParticleTypes.HEART, x, y, z, 7, 0.4, 0.15, 0.4, 0.15);
      }

      player.addEffect(new MobEffectInstance(MobEffects.LUCK, 2652, 0, false, true));
      DOLL_FEED_COOLDOWNS.put(pos, currentTime + FEED_COOLDOWN_TICKS);
      return InteractionResult.SUCCESS;
   }

   static {
      SUPPORTED_DOLL_4_IDS.add(KaleidoscopeWorldLiquor.id("doll_4"));
      SUPPORTED_DOLL_4_IDS.add(ResourceLocation.fromNamespaceAndPath("kaleidoscope_nether", "doll_4"));
   }
}
