package com.bmt.kaleidoscope_world_liquor.compat.ponder.init;

import com.bmt.kaleidoscope_world_liquor.compat.ponder.scenes.FreezerScenes;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class FreezerPonderScenes {
    public FreezerPonderScenes() {
    }

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(new ResourceLocation[]{new ResourceLocation("kaleidoscope_world_liquor", "freezer")})
            .addStoryBoard("freezer/introduction", FreezerScenes::introduction);
    }
}
