package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.init.ModEnchantments;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 铁砧"臻酿"：右键酿造中的酒桶提升酿造等级（原版 BrewAcceleratorEventHandler）。
 */
public final class MiscEvents {
    private MiscEvents() {
    }

    /** 铁砧"臻酿"升级酒桶（原版 BrewAcceleratorEventHandler.RightClickBlock） */
    public static void registerBrewAccelerator() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            Level level = world;
            if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            // 手持附魔书且含 brew_accelerator（附魔台出的书在 STORED_ENCHANTMENTS，
            // 铁砧/指令给的物品附魔在 ENCHANTMENTS——两处都查）
            ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
            ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
            Identifier enchId = ModEnchantments.BREW_ACCELERATOR.identifier();
            int enchantLevel = Math.max(findEnchLevel(enchantments, enchId), findEnchLevel(stored, enchId));
            if (enchantLevel <= 0) {
                return InteractionResult.PASS;
            }
            // 冷却检查（2400t = 2min，挂玩家 cooldown，键 = 物品）
            net.minecraft.world.item.ItemCooldowns cooldowns = player.getCooldowns();
            if (cooldowns.isOnCooldown(stack)) {
                float percent = cooldowns.getCooldownPercent(stack, 0.0F);
                int remainingSeconds = (int) (percent * 2400) / 20;
                player.sendOverlayMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.cooldown", remainingSeconds));
                return InteractionResult.FAIL;
            }
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            // 酒桶是多方块结构：右键的可能不是底层原点方块，必须走 tavern 的
            // getBarrelEntity（isBarrelPart 判定 + getOriginPos 反推原点取 BE）
            if (!(state.getBlock() instanceof com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock)) {
                return InteractionResult.PASS;
            }
            com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity barrel =
                    com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock.getBarrelEntity(level, pos, state);
            if (barrel == null) {
                return InteractionResult.PASS;
            }
            if (!barrel.isBrewing()) {
                player.sendOverlayMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.not_brewing"));
                return InteractionResult.FAIL;
            }
            if (barrel.isMaxBrewLevel()) {
                player.sendOverlayMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.max_level"));
                return InteractionResult.FAIL;
            }
            // 升级（原版音效：附魔台使用音）
            barrel.advanceBrewLevel();
            cooldowns.addCooldown(stack, 2400);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.2F);
            player.sendOverlayMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.success", barrel.getBrewLevel()));
            return InteractionResult.SUCCESS;
        });
    }

    private static int findEnchLevel(ItemEnchantments enchantments, Identifier enchId) {
        if (enchantments == null) {
            return 0;
        }
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().unwrapKey().map(k -> k.identifier().equals(enchId)).orElse(false)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }
}
