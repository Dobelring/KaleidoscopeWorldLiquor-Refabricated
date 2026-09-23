package com.bmt.kaleidoscope_world_liquor.compat.ponder.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.compat.ponder.scenes.FreezerScenes;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/** 把冰柜方块挂到 Create 的 Ponder 索引上（官方同名类的移植，只改 Create 包名）。 */
@Environment(EnvType.CLIENT)
public final class FreezerPonderScenes {
    private FreezerPonderScenes() {
    }

    public static void register(PonderSceneRegistrationHelper<Identifier> helper) {
        helper.forComponents(new Identifier[]{KaleidoscopeWorldLiquor.id("freezer")})
                .addStoryBoard("freezer/introduction", FreezerScenes::introduction);
    }
}
