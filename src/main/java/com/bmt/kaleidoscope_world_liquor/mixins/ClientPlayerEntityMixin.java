package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.hooks.ClientPlayerEntityMixinHooks;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer {
    @Unique
    private final ClientPlayerEntityMixinHooks kaleidoscope_world_liquor_1_20_1$hooks = new ClientPlayerEntityMixinHooks();

    public ClientPlayerEntityMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(
        method = {"aiStep"},
        at = {@At("HEAD")}
    )
    private void multiJump$tickMovement(CallbackInfo info) {
        this.kaleidoscope_world_liquor_1_20_1$hooks.tickMovement((LocalPlayer)(Object) this);
    }

    @Inject(
        method = {"serverAiStep"},
        at = {@At("RETURN")}
    )
    private void kaleidoscope$invertStrafe(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer)(Object) this;
        if (player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
            LivingEntity living = (LivingEntity)(Object) this;
            living.xxa = -living.xxa;
        }
    }
}
