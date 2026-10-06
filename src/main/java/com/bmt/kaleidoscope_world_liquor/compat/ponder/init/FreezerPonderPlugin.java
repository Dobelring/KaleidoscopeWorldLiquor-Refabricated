package com.bmt.kaleidoscope_world_liquor.compat.ponder.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.zurrtum.create.client.ponder.api.registration.PonderPlugin;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * 向 Create 的 Ponder 注册冰柜讲解场景（官方同名类的移植）。
 * <p>
 * 官方用 {@code net.createmod.ponder.*}，Create Fly 是整体重打包的 Create 移植，
 * 对应 {@code com.zurrtum.create.client.ponder.*}，故此处只改包名。
 */
@Environment(EnvType.CLIENT)
public final class FreezerPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return KaleidoscopeWorldLiquor.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<Identifier> helper) {
        FreezerPonderScenes.register(helper);
        KaleidoscopeWorldLiquor.LOGGER.info("Registered the Freezer ponder scene (kaleidoscope_world_liquor:freezer)");
    }

    /**
     * 官方 1.1.11：把冰柜挂进 tavern 的「酿造」Ponder 标签索引。
     * 26.x 的 tavern 标签常量在 {@code compat/create/ponder/init/ModPonderTags}（不再是 TavernPonderTags）。
     */
    @Override
    public void registerTags(com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper<Identifier> helper) {
        helper.addToTag(com.github.ysbbbbbb.kaleidoscopetavern.compat.create.ponder.init.ModPonderTags.BREWING)
                .add(Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, "freezer"));
    }

    public static void init() {
        PonderIndex.addPlugin(new FreezerPonderPlugin());
    }
}
