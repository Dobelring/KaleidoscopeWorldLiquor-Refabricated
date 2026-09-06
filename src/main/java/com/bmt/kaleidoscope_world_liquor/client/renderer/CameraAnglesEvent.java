package com.bmt.kaleidoscope_world_liquor.client.renderer;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public final class CameraAnglesEvent {
   private static final float ROT = 540.3539364174444F;

   private CameraAnglesEvent() {
   }

   /**
    * Applies the reverse-gravity camera roll. Called from {@code CameraMixin} at the end of
    * {@code Camera#setup}, since Fabric has no camera-angles event.
    */
   public static void applyReverseGravityRoll(Quaternionf rotation) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && player.hasEffect(ModEffects.REVERSE_GRAVITY)) {
         rotation.rotateZ((float)Math.toRadians(ROT));
      }
   }
}
