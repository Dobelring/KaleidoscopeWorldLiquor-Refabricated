package com.bmt.kaleidoscope_world_liquor.command;

import com.bmt.kaleidoscope_world_liquor.mixins.accessor.BarrelBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class BrewCommands {
   public static void register() {
      CommandRegistrationCallback.EVENT.register(BrewCommands::registerCommands);
   }

   private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, CommandSelection environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("brew").requires(source -> source.hasPermission(2)))
               .then(Commands.literal("add").executes(BrewCommands::addOneLevel)))
            .then(Commands.literal("max").executes(BrewCommands::setMaxLevel))
      );
   }

   private static int addOneLevel(CommandContext<CommandSourceStack> context) {
      try {
         Player player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
         Level level = ((CommandSourceStack)context.getSource()).getLevel();
         HitResult hit = player.pick(5.0, 0.0F, false);
         if (hit.getType() != Type.BLOCK) {
            ((CommandSourceStack)context.getSource()).sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.not_barrel"));
            return 0;
         } else {
            BlockPos pos = ((BlockHitResult)hit).getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BarrelBlock)) {
               ((CommandSourceStack)context.getSource()).sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.not_barrel"));
               return 0;
            } else {
               BarrelBlockEntity barrel = BarrelBlock.getBarrelEntity(level, pos, state);
               if (barrel == null) {
                  ((CommandSourceStack)context.getSource()).sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.error"));
                  return 0;
               } else if (!barrel.isBrewing()) {
                  ((CommandSourceStack)context.getSource())
                     .sendFailure(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.not_brewing"));
                  return 0;
               } else if (barrel.isMaxBrewLevel()) {
                  ((CommandSourceStack)context.getSource()).sendFailure(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.max_level"));
                  return 0;
               } else {
                  BarrelBlockEntityAccessor accessor = (BarrelBlockEntityAccessor)barrel;
                  int newLevel = Math.min(barrel.getBrewLevel() + 1, 6);
                  accessor.setBrewLevel(newLevel);
                  accessor.setBrewTime(accessor.invokeGetBrewTimeForLevel());
                  barrel.refresh();
                  ((CommandSourceStack)context.getSource())
                     .sendSuccess(() -> Component.translatable("message.kaleidoscope_world_liquor.command.add_success", new Object[]{newLevel}), false);
                  return 1;
               }
            }
         }
      } catch (Exception var9) {
         ((CommandSourceStack)context.getSource()).sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.error"));
         return 0;
      }
   }

   private static int setMaxLevel(CommandContext<CommandSourceStack> context) {
      try {
         Player player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
         Level level = ((CommandSourceStack)context.getSource()).getLevel();
         HitResult hit = player.pick(5.0, 0.0F, false);
         if (hit.getType() != Type.BLOCK) {
            ((CommandSourceStack)context.getSource()).sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.no_target"));
            return 0;
         } else {
            BlockPos pos = ((BlockHitResult)hit).getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BarrelBlock)) {
               ((CommandSourceStack)context.getSource()).sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.not_barrel"));
               return 0;
            } else {
               BarrelBlockEntity barrel = BarrelBlock.getBarrelEntity(level, pos, state);
               if (barrel == null) {
                  ((CommandSourceStack)context.getSource()).sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.barrel_invalid"));
                  return 0;
               } else if (!barrel.isBrewing()) {
                  ((CommandSourceStack)context.getSource())
                     .sendFailure(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.not_brewing"));
                  return 0;
               } else if (barrel.isMaxBrewLevel()) {
                  ((CommandSourceStack)context.getSource()).sendFailure(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.max_level"));
                  return 0;
               } else {
                  BarrelBlockEntityAccessor accessor = (BarrelBlockEntityAccessor)barrel;
                  accessor.setBrewLevel(6);
                  accessor.setBrewTime(accessor.invokeGetBrewTimeForLevel());
                  barrel.refresh();
                  ((CommandSourceStack)context.getSource())
                     .sendSuccess(() -> Component.translatable("message.kaleidoscope_world_liquor.command.max_success"), false);
                  return 1;
               }
            }
         }
      } catch (Exception var8) {
         ((CommandSourceStack)context.getSource()).sendFailure(Component.translatable("message.kaleidoscope_world_liquor.command.error"));
         return 0;
      }
   }
}
