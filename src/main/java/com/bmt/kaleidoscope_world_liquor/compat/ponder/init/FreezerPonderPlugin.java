package com.bmt.kaleidoscope_world_liquor.compat.ponder.init;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
@MethodsReturnNonnullByDefault
public class FreezerPonderPlugin implements PonderPlugin {
    public FreezerPonderPlugin() {
    }

    public String getModId() {
        return "kaleidoscope_world_liquor";
    }

    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        FreezerPonderScenes.register(helper);
    }

    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
    }

    public static void init() {
        PonderIndex.addPlugin(new FreezerPonderPlugin());
    }
}
