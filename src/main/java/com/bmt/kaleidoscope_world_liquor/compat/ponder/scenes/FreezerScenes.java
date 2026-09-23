package com.bmt.kaleidoscope_world_liquor.compat.ponder.scenes;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.blockentity.FreezerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.util.fluids.CustomFluidTank;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.ponder.api.scene.PositionUtil;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.api.scene.SceneBuildingUtil;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.client.ponder.api.scene.SelectionUtil;
import com.zurrtum.create.client.ponder.api.scene.VectorUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/**
 * 冰柜的 Ponder 讲解场景（官方同名类的移植）。
 * <p>
 * 除 Create 包名外，与官方还有两处差别：
 * <ol>
 *   <li>流体：官方用 NeoForge 的 {@code FluidStack}/{@code FluidAction} 往
 *       {@code tank.fill(...)} 里注水、用 {@code tank.setFluid(FluidStack.EMPTY)} 清空；
 *       本端口的冰柜水箱是 tavern 的 {@code CustomFluidTank}（Fabric Transfer API），
 *       注水用 {@code fill(FluidVariant, amount, FluidAction)}，清空改用
 *       {@code drain(当前量, FluidAction.EXECUTE)}（没有 setFluid）。</li>
 *   <li>方块实体包名是 {@code blockentity}（本端口没有 {@code block.entity}）。</li>
 * </ol>
 */
@Environment(EnvType.CLIENT)
public final class FreezerScenes {
    private FreezerScenes() {
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
        Identifier iceTexture = Identifier.fromNamespaceAndPath("kaleidoscope_world_liquor", "block/ice");

        scene.idle(20);
        scene.world().showSection(freezerSel, Direction.DOWN);
        scene.idle(30);

        scene.overlay().showText(60)
                .text("ponder.kaleidoscope_world_liquor.freezer.step1")
                .pointAt(vector.blockSurface(freezerPos, Direction.WEST))
                .placeNearTarget();
        scene.overlay().showControls(vector.blockSurface(freezerPos, Direction.UP), Pointing.DOWN, 35)
                .rightClick().whileSneaking();
        scene.idle(7);
        scene.world().modifyBlock(freezerPos, s -> s.setValue(FreezerBlock.OPEN, true), false);
        scene.idle(55);
        scene.addKeyframe();
        scene.idle(20);

        scene.overlay().showText(60)
                .text("ponder.kaleidoscope_world_liquor.freezer.step2")
                .pointAt(vector.blockSurface(freezerPos, Direction.WEST))
                .placeNearTarget();
        scene.overlay().showControls(vector.blockSurface(freezerPos, Direction.UP), Pointing.DOWN, 35)
                .rightClick().withItem(new ItemStack(Items.WATER_BUCKET));
        scene.idle(7);
        scene.world().modifyBlockEntity(freezerPos, FreezerBlockEntity.class,
                be -> be.tank.fill(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, CustomFluidTank.FluidAction.EXECUTE));
        scene.idle(55);
        scene.addKeyframe();
        scene.idle(20);

        scene.overlay().showText(60)
                .text("ponder.kaleidoscope_world_liquor.freezer.step3")
                .pointAt(vector.blockSurface(freezerPos, Direction.WEST))
                .placeNearTarget();
        scene.overlay().showControls(vector.blockSurface(freezerPos, Direction.UP), Pointing.DOWN, 35)
                .rightClick().whileSneaking();
        scene.idle(7);
        scene.world().modifyBlock(freezerPos, s -> s.setValue(FreezerBlock.OPEN, false)
                .setValue(FreezerBlock.WORKING, true), false);
        scene.world().modifyBlockEntity(freezerPos, FreezerBlockEntity.class, be -> {
            be.setMaxProgress(200);
            be.setProgress(0);
        });
        scene.idle(55);
        scene.addKeyframe();
        scene.idle(20);

        scene.overlay().showText(60)
                .text("ponder.kaleidoscope_world_liquor.freezer.working")
                .placeNearTarget();
        scene.overlay().showControls(vector.blockSurface(grid.at(2, 3, 2), Direction.UP), Pointing.DOWN, 55)
                .withItem(new ItemStack(Items.CLOCK));

        for (int i = 0; i < 10; i++) {
            int prog = (i + 1) * 20;
            scene.world().modifyBlockEntity(freezerPos, FreezerBlockEntity.class, be -> be.setProgress(prog));
            scene.idle(6);
        }

        scene.addKeyframe();
        scene.idle(20);
        scene.idle(20);
        scene.world().modifyBlock(freezerPos, s -> s.setValue(FreezerBlock.WORKING, false)
                .setValue(FreezerBlock.OPEN, true), false);
        scene.world().modifyBlockEntity(freezerPos, FreezerBlockEntity.class, be -> {
            be.setProgress(0);
            be.tank.drain(be.tank.getFluidAmountTransfer(), CustomFluidTank.FluidAction.EXECUTE);
            be.setOutputCount(3);
            be.setOutputTexture(iceTexture);
        });
        scene.overlay().showText(60)
                .text("ponder.kaleidoscope_world_liquor.freezer.step4")
                .pointAt(vector.blockSurface(freezerPos, Direction.WEST))
                .placeNearTarget();
        scene.overlay().showControls(vector.blockSurface(freezerPos, Direction.UP), Pointing.DOWN, 35).rightClick();
        scene.idle(80);
        scene.world().modifyBlockEntity(freezerPos, FreezerBlockEntity.class, be -> be.setOutputCount(2));
        scene.overlay().showControls(vector.blockSurface(freezerPos, Direction.UP), Pointing.DOWN, 35)
                .withItem(new ItemStack(Items.ICE));
        scene.idle(50);

        scene.world().hideSection(freezerSel, Direction.DOWN);
        scene.idle(20);
    }
}
