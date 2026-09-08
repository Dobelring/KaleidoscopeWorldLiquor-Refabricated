package com.bmt.kaleidoscope_world_liquor.compat.jade;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import net.minecraft.resources.Identifier;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ModJadePlugin implements IWailaPlugin {
    public static final Identifier FREEZER = Identifier.fromNamespaceAndPath("kaleidoscope_world_liquor", "freezer");

    @Override
    public void register(IWailaCommonRegistration registration) {
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(FreezerComponentProvider.INSTANCE, FreezerBlock.class);
    }
}
