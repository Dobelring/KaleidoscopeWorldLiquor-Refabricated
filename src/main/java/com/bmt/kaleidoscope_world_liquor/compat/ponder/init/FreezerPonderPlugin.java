package com.bmt.kaleidoscope_world_liquor.compat.ponder.init;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.create.ponder.init.TavernPonderTags;
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
        // 官方 1.1.12（Ponder 联动修复）：冰柜注册进酒馆酿造标签
        helper.addToTag(TavernPonderTags.BREWING).add(new ResourceLocation("kaleidoscope_world_liquor", "freezer"));
    }

    public static void init() {
        PonderIndex.addPlugin(new FreezerPonderPlugin());
    }
}
