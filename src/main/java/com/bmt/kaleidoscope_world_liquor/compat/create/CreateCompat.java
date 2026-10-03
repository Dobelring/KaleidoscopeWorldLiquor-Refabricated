package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.compat.create.network.PacketHandler;
import com.bmt.kaleidoscope_world_liquor.compat.kaleidoscope_contraption.KaleidoscopeContraptionCompat;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.Block;

public final class CreateCompat {
    private CreateCompat() {
    }

    public static void register() {
        PacketHandler.register();
        ChairBlockMovementBehaviour chairMovement = new ChairBlockMovementBehaviour();
        ChairMovingInteraction chairInteraction = new ChairMovingInteraction();
        registerBarStools(chairMovement, chairInteraction);
        BarCabinetMovingInteraction cabinetInteraction = new BarCabinetMovingInteraction();
        registerBarCabinets(cabinetInteraction);
        BarCellarCabinetMovingInteraction cellarCabinetInteraction = new BarCellarCabinetMovingInteraction();
        registerCellarCabinets(cellarCabinetInteraction);
        if (FabricLoader.getInstance().isModLoaded("kaleidoscope_contraption")) {
            KaleidoscopeContraptionCompat.register();
        }
    }

    private static void registerBarStools(MovementBehaviour movement, MovingInteractionBehaviour interaction) {
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_BLACK, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_WHITE, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_LIGHT_GRAY, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_GRAY, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_BROWN, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_RED, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_ORANGE, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_YELLOW, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_LIME, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_GREEN, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_CYAN, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_LIGHT_BLUE, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_BLUE, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_PURPLE, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_MAGENTA, movement);
        MovementBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_PINK, movement);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_BLACK, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_WHITE, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_LIGHT_GRAY, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_GRAY, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_BROWN, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_RED, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_ORANGE, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_YELLOW, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_LIME, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_GREEN, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_CYAN, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_LIGHT_BLUE, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_BLUE, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_PURPLE, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_MAGENTA, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAR_STOOL_PINK, interaction);
    }

    private static void registerBarCabinets(MovingInteractionBehaviour interaction) {
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.OAK_BAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BIRCH_BAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.SPRUCE_BAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.DARK_OAK_BAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.CHERRY_BAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.JUNGLE_BAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.ACACIA_BAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.MANGROVE_BAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAMBOO_BAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.CRIMSON_BAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.WARPED_BAR_CABINET, interaction);
    }

    private static void registerCellarCabinets(MovingInteractionBehaviour interaction) {
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.OAK_CELLAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BIRCH_CELLAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.SPRUCE_CELLAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.DARK_OAK_CELLAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.CHERRY_CELLAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.JUNGLE_CELLAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.ACACIA_CELLAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.MANGROVE_CELLAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.BAMBOO_CELLAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.CRIMSON_CELLAR_CABINET, interaction);
        MovingInteractionBehaviour.REGISTRY.register(ModBlocks.WARPED_CELLAR_CABINET, interaction);
    }
}
