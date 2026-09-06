package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.client.renderer.CameraAnglesEvent;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Camera.class})
public abstract class CameraMixin {
   @Shadow
   private Vec3 position;
   @Shadow
   private Quaternionf rotation;

   @Inject(
      method = {"setup"},
      at = {@At("TAIL")}
   )
   private void kaleidoscope$adjustEyePosition(BlockGetter level, Entity focusedEntity, boolean detached, boolean mirror, float partialTick, CallbackInfo ci) {
      if (focusedEntity instanceof Player player) {
         if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            double eyeHeight = player.getEyeHeight();
            double bbHeight = player.getBbHeight();
            double offset = bbHeight - 2.0 * eyeHeight;
            this.position = new Vec3(this.position.x, this.position.y + offset, this.position.z);
            CameraAnglesEvent.applyReverseGravityRoll(this.rotation);
         }
      }
   }
}
