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

    public EntityMixin() {
    }

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

    // 官方 1.2.0：发光改为叠加标记——不再拦截 setGlowingTag 保存/恢复原状（旧方案会踩掉
    // 效果期间其他来源的发光状态），改为增强 isCurrentlyGlowing 的返回值，原版发光位不动。
    @Override
    public void setGlowing(boolean glowing) {
        this.kaleidoscope_world_liquor$modGlowing = glowing;
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
        method = {"isCurrentlyGlowing"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void kaleidoscope$augmentGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (this.kaleidoscope_world_liquor$modGlowing) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = {"turn(DD)V"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void kaleidoscope$invertMouseInput(double yaw, double pitch, CallbackInfo ci) {
        Entity entity = (Entity)(Object) this;
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
        Entity entity = (Entity)(Object) this;
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
