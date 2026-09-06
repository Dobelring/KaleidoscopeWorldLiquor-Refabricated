package com.bmt.kaleidoscope_world_liquor.hooks;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.LivingEntityAccessor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ClientPlayerEntityMixinHooks {
   private int multiJump$jumpCount = 0;
   private boolean multiJump$jumpedLastTick = false;

   public void tickMovement(LocalPlayer player) {
      if (!player.hasEffect(ModEffects.MULTI_JUMP_EFFECT)) {
         this.multiJump$jumpCount = 0;
         this.multiJump$jumpedLastTick = false;
      } else {
         MobEffectInstance effect = player.getEffect(ModEffects.MULTI_JUMP_EFFECT);
         if (effect != null) {
            int amplifier = effect.getAmplifier();
            int maxJumps = amplifier + 1;
            if (player.onGround() || player.onClimbable()) {
               this.multiJump$jumpCount = maxJumps;
            }

            if (this.canJump(player)
               && !player.onGround()
               && !this.multiJump$jumpedLastTick
               && this.multiJump$jumpCount > 0
               && player.getDeltaMovement().y < 0.0
               && ((LivingEntityAccessor)player).isJumping()
               && !player.getAbilities().flying) {
               this.multiJump$jumpCount--;
               player.jumpFromGround();
               player.fallDistance = 0.0F;
               this.multiJump$jumpedLastTick = true;
            } else {
               this.multiJump$jumpedLastTick = ((LivingEntityAccessor)player).isJumping();
            }
         }
      }
   }

   private boolean wearingUsableElytra(LocalPlayer player) {
      ItemStack chestItemStack = player.getItemBySlot(EquipmentSlot.CHEST);
      return chestItemStack.getItem() == Items.ELYTRA && ElytraItem.isFlyEnabled(chestItemStack);
   }

   private boolean canJump(LocalPlayer player) {
      return !this.wearingUsableElytra(player)
         && !player.isFallFlying()
         && !player.isPassenger()
         && !player.isInWater()
         && !player.hasEffect(MobEffects.LEVITATION);
   }
}
