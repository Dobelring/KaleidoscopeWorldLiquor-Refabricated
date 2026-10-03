package com.bmt.kaleidoscope_world_liquor.compat.ponder.scenes;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.PositionUtil;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.api.scene.SelectionUtil;
import net.createmod.ponder.api.scene.VectorUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

@Environment(EnvType.CLIENT)
public class FreezerScenes {
    public FreezerScenes() {
    }

    public static void introduction(SceneBuilder scene, SceneBuildingUtil util) {
        VectorUtil vector = util.vector();
        SelectionUtil select = util.select();
        PositionUtil grid = util.grid();
        scene.title("freezer", "ponder.kaleidoscope_world_liquor.freezer.title");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        BlockPos freezerPos = grid.at(2, 1, 2);
        Selection freezerSel = select.position(freezerPos);
        ResourceLocation iceTexture = new ResourceLocation("kaleidoscope_world_liquor", "block/ice");
        scene.idle(20);
        scene.world().showSection(freezerSel, Direction.DOWN);
        scene.idle(30);
        scene.overlay()
            .showText(60)
            .text("ponder.kaleidoscope_world_liquor.freezer.step1")
            .pointAt(vector.blockSurface(freezerPos, Direction.WEST))
            .placeNearTarget();
        scene.overlay().showControls(vector.blockSurface(freezerPos, Direction.UP), Pointing.DOWN, 35).rightClick().whileSneaking();
        scene.idle(7);
        scene.world().modifyBlock(freezerPos, s -> (BlockState)s.setValue(FreezerBlock.OPEN, true), false);
        scene.idle(55);
        scene.addKeyframe();
        scene.idle(20);
        scene.overlay()
            .showText(60)
            .text("ponder.kaleidoscope_world_liquor.freezer.step2")
            .pointAt(vector.blockSurface(freezerPos, Direction.WEST))
            .placeNearTarget();
        scene.overlay().showControls(vector.blockSurface(freezerPos, Direction.UP), Pointing.DOWN, 35).rightClick().withItem(new ItemStack(Items.WATER_BUCKET));
        scene.idle(7);
        scene.world().modifyBlockEntity(freezerPos, FreezerBlockEntity.class, be -> be.tank.fill(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, CustomFluidTank.FluidAction.EXECUTE));
        scene.idle(55);
        scene.addKeyframe();
        scene.idle(20);
        scene.overlay()
            .showText(60)
            .text("ponder.kaleidoscope_world_liquor.freezer.step3")
            .pointAt(vector.blockSurface(freezerPos, Direction.WEST))
            .placeNearTarget();
        scene.overlay().showControls(vector.blockSurface(freezerPos, Direction.UP), Pointing.DOWN, 35).rightClick().whileSneaking();
        scene.idle(7);
        scene.world().modifyBlock(freezerPos, s -> (BlockState)((BlockState)s.setValue(FreezerBlock.OPEN, false)).setValue(FreezerBlock.WORKING, true), false);
        scene.world().modifyBlockEntity(freezerPos, FreezerBlockEntity.class, be -> {
            be.setMaxProgress(200);
            be.setProgress(0);
        });
        scene.idle(55);
        scene.addKeyframe();
        scene.idle(20);
        scene.overlay().showText(60).text("ponder.kaleidoscope_world_liquor.freezer.working").placeNearTarget();
        scene.overlay().showControls(vector.blockSurface(grid.at(2, 3, 2), Direction.UP), Pointing.DOWN, 55).withItem(new ItemStack(Items.CLOCK));

        for (int i = 0; i < 10; i++) {
            int prog = (i + 1) * 20;
            scene.world().modifyBlockEntity(freezerPos, FreezerBlockEntity.class, be -> be.setProgress(prog));
            scene.idle(6);
        }

        scene.addKeyframe();
        scene.idle(20);
        scene.idle(20);
        scene.world().modifyBlock(freezerPos, s -> (BlockState)((BlockState)s.setValue(FreezerBlock.WORKING, false)).setValue(FreezerBlock.OPEN, true), false);
        scene.world().modifyBlockEntity(freezerPos, FreezerBlockEntity.class, be -> {
            be.setProgress(0);
            be.tank.drain(FluidConstants.BUCKET, CustomFluidTank.FluidAction.EXECUTE);
            be.setOutputCount(3);
            be.setOutputTexture(iceTexture);
        });
        scene.overlay()
            .showText(60)
            .text("ponder.kaleidoscope_world_liquor.freezer.step4")
            .pointAt(vector.blockSurface(freezerPos, Direction.WEST))
            .placeNearTarget();
        scene.overlay().showControls(vector.blockSurface(freezerPos, Direction.UP), Pointing.DOWN, 35).rightClick();
        scene.idle(80);
        scene.world().modifyBlockEntity(freezerPos, FreezerBlockEntity.class, be -> be.setOutputCount(2));
        scene.overlay().showControls(vector.blockSurface(freezerPos, Direction.UP), Pointing.DOWN, 35).withItem(new ItemStack(Items.ICE));
        scene.idle(50);
        scene.world().hideSection(freezerSel, Direction.DOWN);
        scene.idle(20);
    }
}
