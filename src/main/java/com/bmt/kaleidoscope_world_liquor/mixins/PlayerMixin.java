package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.api.event.PlayerTickEvents;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void kaleidoscope$tickPre(CallbackInfo ci) {
      PlayerTickEvents.START.invoker().onStartOfPlayerTick((Player)(Object)this);
   }

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void kaleidoscope$tickPost(CallbackInfo ci) {
      PlayerTickEvents.END.invoker().onEndOfPlayerTick((Player)(Object)this);
   }
}
