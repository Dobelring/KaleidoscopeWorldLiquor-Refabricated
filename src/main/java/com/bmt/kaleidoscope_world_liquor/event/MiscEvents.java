package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import com.bmt.kaleidoscope_world_liquor.init.ModPaintings;
import com.bmt.kaleidoscope_world_liquor.init.ModTags;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * 唱片贴墙放置 + tooltip；创造栏追加（8 幅画进酒水创造栏——原版 CreativeTabEvents）；
 * 铁砧"臻酿"：附魔书 + 非附魔书合成的拦截由 GuiGraphics 冷却显示与
 * brew_accelerator 右键事件完成（本类持右键逻辑）。
 */
public final class MiscEvents {
    private MiscEvents() {
    }

    public static void register() {

        // ===== 唱片 tooltip（服务端侧无需处理；客户端在 ItemTooltipCallback）=====
    }

    /** 铁砧"臻酿"升级酒桶（原版 BrewAcceleratorEventHandler.RightClickBlock） */
    public static void registerBrewAccelerator() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            Level level = world;
            if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            // 手持附魔书且含 brew_accelerator
            ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
            ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
            int level2 = 0;
            Identifier enchId = com.bmt.kaleidoscope_world_liquor.init.ModEnchantments.BREW_ACCELERATOR.identifier();
            level2 = Math.max(level2, findEnchLevel(enchantments, enchId));
            level2 = Math.max(level2, findEnchLevel(stored, enchId));
            if (level2 <= 0) {
                return InteractionResult.PASS;
            }
            // 冷却检查（2400t，挂在玩家 cooldown 上）
            net.minecraft.world.item.ItemCooldowns cooldowns = player.getCooldowns();
            if (cooldowns.isOnCooldown(stack)) {
                float percent = cooldowns.getCooldownPercent(stack, 0.0F);
                int remainingSeconds = (int) (percent * 2400) / 20;
                player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.cooldown", remainingSeconds), true);
                return InteractionResult.FAIL;
            }
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (!(level.getBlockEntity(pos) instanceof com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity barrel)) {
                return InteractionResult.PASS;
            }
            if (!barrel.isBrewing()) {
                player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.not_brewing"), true);
                return InteractionResult.FAIL;
            }
            if (barrel.isMaxBrewLevel()) {
                player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.max_level"), true);
                return InteractionResult.FAIL;
            }
            // 升级
            barrel.advanceBrewLevel();
            cooldowns.addCooldown(stack, 2400);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.2F);
            player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.success", barrel.getBrewLevel()), true);
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
