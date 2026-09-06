package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public abstract class EntityMixin implements IGlowingEntity {
   @Shadow
   public float xRotO;
   @Shadow
   public float yRotO;
   private boolean kaleidoscope_world_liquor$modGlowing = false;
   private boolean kaleidoscope_world_liquor$originalGlowing = false;

   @Shadow
   public abstract void setSharedFlag(int var1, boolean var2);

   @Shadow
   public abstract void setGlowingTag(boolean var1);

   @Shadow
   public abstract boolean isCurrentlyGlowing();

   @Shadow
   public abstract void turn(double var1, double var3);

   @Shadow
   public abstract float getXRot();

   @Shadow
   public abstract void setXRot(float var1);

   @Shadow
   public abstract float getYRot();

   @Shadow
   public abstract void setYRot(float var1);

   @Override
   public void setGlowing(boolean glowing) {
      if (glowing) {
         this.kaleidoscope_world_liquor$originalGlowing = this.isCurrentlyGlowing();
         this.kaleidoscope_world_liquor$modGlowing = true;
         this.setGlowingTag(true);
         this.setSharedFlag(6, true);
      } else {
         this.kaleidoscope_world_liquor$modGlowing = false;
         this.setGlowingTag(this.kaleidoscope_world_liquor$originalGlowing);
         this.setSharedFlag(6, this.kaleidoscope_world_liquor$originalGlowing);
      }
   }

   @Override
   public boolean isModGlowing() {
      return this.kaleidoscope_world_liquor$modGlowing;
   }

   @Override
   public void setModGlowing(boolean modGlowing) {
      this.kaleidoscope_world_liquor$modGlowing = modGlowing;
   }

   @Inject(
      method = {"setGlowingTag"},
      at = {@At("HEAD")}
   )
   private void onSetGlowingTag(boolean pGlowingTag, CallbackInfo ci) {
      if (!this.kaleidoscope_world_liquor$modGlowing) {
         this.kaleidoscope_world_liquor$originalGlowing = pGlowingTag;
      }
   }

   @Inject(
      method = {"turn(DD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void kaleidoscope$invertMouseInput(double yaw, double pitch, CallbackInfo ci) {
      Entity entity = (Entity)(Object)this;
      if (entity instanceof Player player) {
         if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            float f = (float)(-pitch * 0.15F);
            float f1 = (float)(-yaw * 0.15F);
            this.setXRot(this.getXRot() + f);
            this.setYRot(this.getYRot() + f1);
            this.setXRot(Mth.clamp(this.getXRot(), -90.0F, 90.0F));
            this.xRotO += f;
            this.yRotO += f1;
            this.xRotO = Mth.clamp(this.xRotO, -90.0F, 90.0F);
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"getEyePosition(F)Lnet/minecraft/world/phys/Vec3;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void kaleidoscope$adjustEyePositionForPick(float partialTick, CallbackInfoReturnable<Vec3> cir) {
      Entity entity = (Entity)(Object)this;
      if (entity instanceof Player player) {
         if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            Vec3 original = (Vec3)cir.getReturnValue();
            double eyeHeight = player.getEyeHeight();
            double bbHeight = player.getBbHeight();
            double offset = bbHeight - 2.0 * eyeHeight;
            cir.setReturnValue(new Vec3(original.x, original.y + offset, original.z));
         }
      }
   }
}
