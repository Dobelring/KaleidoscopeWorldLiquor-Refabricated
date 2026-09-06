package com.bmt.kaleidoscope_world_liquor.compat.jade;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.compat.jade.block.FreezerComponentProvider;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ModPlugin implements IWailaPlugin {
   public static final ResourceLocation FREEZER = ResourceLocation.fromNamespaceAndPath("kaleidoscope_world_liquor", "freezer");

   public void registerClient(IWailaClientRegistration registration) {
      registration.registerBlockComponent(FreezerComponentProvider.INSTANCE, FreezerBlock.class);
   }
}
