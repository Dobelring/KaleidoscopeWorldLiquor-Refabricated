package com.bmt.kaleidoscope_world_liquor.mixins;

import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModCompatItems;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Registers the liquor drink blocks into the tavern drink block entity type's valid blocks.
 * Equivalent of the NeoForge BlockEntityTypeAddBlocksEvent handler.
 */
@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin {
   private static final ResourceLocation TAVERN_DRINK_BE = ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "drink");
   private static final Set<Block> LIQUOR_DRINK_BLOCKS = Set.of(
      ModBlocks.BOMBAY_SAPPHIRE_GIN,
      ModBlocks.JACK_DANIEL,
      ModBlocks.SMIRNOFF_RED_VODKA,
      ModBlocks.ABSOLUT_VODKA,
      ModBlocks.PINA_COLADA,
      ModBlocks.MAOTAI,
      ModBlocks.BACARDI_CARTA_BLANCA,
      ModBlocks.SPIRYT_VODKA,
      ModBlocks.SKYY_VODKA,
      ModBlocks.JOHNNIE_WALKER,
      ModBlocks.LAFITE_1982,
      ModBlocks.STRONGBOW,
      ModBlocks.DASSAI,
      ModBlocks.KWAS_CHLEBOWY,
      ModBlocks.BAMBOO_LEAF_GREEN_LIQUOR,
      ModBlocks.COOL_TEA,
      ModBlocks.SOUR_PLUM,
      ModCompatItems.ICE_TEA_BLOCK
   );

   @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
   private void kaleidoscope$validLiquorDrinkBlocks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
      if (TAVERN_DRINK_BE.equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey((BlockEntityType<?>)(Object)this))
         && LIQUOR_DRINK_BLOCKS.contains(state.getBlock())) {
         cir.setReturnValue(true);
      }
   }
}
