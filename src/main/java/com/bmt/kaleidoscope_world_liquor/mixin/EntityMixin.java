package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 冥视发光状态追踪（IGlowingEntity）+ 反重力鼠标反转 + 反重力视线位置修正。
 * 1.21.11 的发光渲染直接走 setGlowingTag 即可（无需 1.20.1 的 setSharedFlag 组合）。
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements IGlowingEntity {
    @Shadow public float yRotO;
    @Shadow public float xRotO;
    @Shadow public abstract void setYRot(float yRot);
    @Shadow public abstract void setXRot(float xRot);
    @Shadow public abstract float getYRot();
    @Shadow public abstract float getXRot();
    @Shadow public abstract void turn(double yaw, double pitch);
    @Shadow protected abstract boolean getSharedFlag(int flag);

    @Unique
    private boolean kaleidoscope_world_liquor$modGlowing = false;
    @Unique
    private boolean kaleidoscope_world_liquor$originalGlowing = false;

    @Override
    public void setGlowing(boolean glowing) {
        if (glowing) {
            this.kaleidoscope_world_liquor$originalGlowing = this.getSharedFlag(6);
            this.kaleidoscope_world_liquor$modGlowing = true;
            ((Entity) (Object) this).setGlowingTag(true);
        } else {
            this.kaleidoscope_world_liquor$modGlowing = false;
            ((Entity) (Object) this).setGlowingTag(this.kaleidoscope_world_liquor$originalGlowing);
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

    @Inject(method = "setGlowingTag", at = @At("HEAD"))
    private void kwl$trackOriginalGlowing(boolean glowing, CallbackInfo ci) {
        if (!this.kaleidoscope_world_liquor$modGlowing) {
            this.kaleidoscope_world_liquor$originalGlowing = glowing;
        }
    }

    @Inject(method = "turn(DD)V", at = @At("HEAD"), cancellable = true)
    private void kwl$invertMouseInput(double yaw, double pitch, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            float f = (float) (-pitch * 0.15F);
            float f1 = (float) (-yaw * 0.15F);
            this.setXRot(this.getXRot() + f);
            this.setYRot(this.getYRot() + f1);
            this.setXRot(Mth.clamp(this.getXRot(), -90.0F, 90.0F));
            this.xRotO += f;
            this.yRotO += f1;
            this.xRotO = Mth.clamp(this.xRotO, -90.0F, 90.0F);
            ci.cancel();
        }
    }

    @Inject(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void kwl$adjustEyePositionForPick(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof Player player && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            Vec3 original = cir.getReturnValue();
            double eyeHeight = player.getEyeHeight();
            double bbHeight = player.getBbHeight();
            double offset = bbHeight - 2.0 * eyeHeight;
            cir.setReturnValue(new Vec3(original.x, original.y + offset, original.z));
        }
    }
}
