package com.bmt.kaleidoscope_world_liquor.event;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;

/**
 * /brew add | /brew max（准心指向酿造中的酒桶，提升/拉满酿造等级）。
 * 1.21.11 直接调 tavern 的 advanceBrewLevel（第 9 步在 tavern 侧新增的公开方法）。
 */
public final class BrewCommands {
    private BrewCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("brew")
                        .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("add").executes(ctx -> addOneLevel(ctx.getSource())))
                        .then(Commands.literal("max").executes(ctx -> setMaxLevel(ctx.getSource())))
        ));
    }

    private static int addOneLevel(CommandSourceStack source) {
        BarrelBlockEntity barrel = raycastBarrel(source);
        if (barrel == null) {
            return 0;
        }
        if (!barrel.isBrewing()) {
            source.sendFailure(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.not_brewing"));
            return 0;
        }
        if (barrel.isMaxBrewLevel()) {
            source.sendFailure(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.max_level"));
            return 0;
        }
        barrel.advanceBrewLevel();
        source.sendSuccess(() -> Component.translatable("message.kaleidoscope_world_liquor.command.add_success", barrel.getBrewLevel()), false);
        return 1;
    }

    private static int setMaxLevel(CommandSourceStack source) {
        BarrelBlockEntity barrel = raycastBarrel(source);
        if (barrel == null) {
            return 0;
        }
        if (!barrel.isBrewing()) {
            source.sendFailure(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.not_brewing"));
            return 0;
        }
        while (!barrel.isMaxBrewLevel()) {
            barrel.advanceBrewLevel();
        }
        source.sendSuccess(() -> Component.translatable("message.kaleidoscope_world_liquor.command.max_success"), false);
        return 1;
    }

    private static BarrelBlockEntity raycastBarrel(CommandSourceStack source) {
        try {
            var player = source.getPlayerOrException();
            HitResult hit = player.pick(5.0, 0.0F, false);
            if (hit.getType() != HitResult.Type.BLOCK) {
                source.sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.not_barrel"));
                return null;
            }
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            BlockState state = source.getLevel().getBlockState(pos);
            if (!(state.getBlock() instanceof BarrelBlock)) {
                source.sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.not_barrel"));
                return null;
            }
            BlockEntity be = source.getLevel().getBlockEntity(pos);
            if (!(be instanceof BarrelBlockEntity barrel)) {
                source.sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.barrel_invalid"));
                return null;
            }
            return barrel;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.error"));
            return null;
        }
    }
}
