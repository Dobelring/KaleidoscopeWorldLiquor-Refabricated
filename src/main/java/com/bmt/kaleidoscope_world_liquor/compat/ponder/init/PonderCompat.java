package com.bmt.kaleidoscope_world_liquor.compat.ponder.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class PonderCompat {
    public static final String PONDER_MOD_ID = "ponder";
    public static boolean PONDER_LOADED = false;

    public PonderCompat() {
    }

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded(PONDER_MOD_ID)) {
            PONDER_LOADED = true;
            FreezerPonderPlugin.init();
        }
    }
}
