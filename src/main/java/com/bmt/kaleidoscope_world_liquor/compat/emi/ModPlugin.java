package com.bmt.kaleidoscope_world_liquor.compat.emi;

import com.bmt.kaleidoscope_world_liquor.compat.emi.category.EmiFreezerRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@EmiEntrypoint
public class ModPlugin implements EmiPlugin {
   @Override
   public void register(EmiRegistry registry) {
      EmiFreezerRecipe.register(registry);
   }
}
