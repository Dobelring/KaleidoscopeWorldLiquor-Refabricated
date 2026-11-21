package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.init.ModEnchantments;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 铁砧"臻酿"：右键酿造中的酒桶提升酿造等级（原版 BrewAcceleratorEventHandler）。
 * <p>
 * 官方 1.2.0：冷却从「玩家全局附魔书冷却」改为每本书自身 NBT
 * （{@code kaleidoscope_brew_cooldown_until}，CUSTOM_DATA 组件）——不同书互不影响、
 * 冷却随物品走；FakePlayer 放行提示（26.x fabric-api 无 FakePlayer API，消息不屏蔽，
 * 与 Create 机械臂交互时会多一条 actionbar，无功能差异）。
 */
public final class MiscEvents {
    private MiscEvents() {
    }

    /** 官方 1.2.0：每本书的冷却 NBT 键 */
    public static final String COOLDOWN_TAG = "kaleidoscope_brew_cooldown_until";
    public static final int COOLDOWN_TICKS = 2400;

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
            // 官方 1.2.0：每本书自身 NBT 冷却
            long gameTime = level.getGameTime();
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            CompoundTag stackTag = customData == null ? new CompoundTag() : customData.copyTag();
            long cooldownUntil = stackTag.getLong(COOLDOWN_TAG).orElse(0L);
            if (cooldownUntil > gameTime) {
                player.sendOverlayMessage(Component.translatable(
                        "message.kaleidoscope_world_liquor.brew_accelerator.cooldown", (int) ((cooldownUntil - gameTime) / 20L)));
                return InteractionResult.FAIL;
            }
            if (!barrel.isBrewing()) {
                player.sendOverlayMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.not_brewing"));
                return InteractionResult.FAIL;
            }
            if (barrel.isMaxBrewLevel()) {
                player.sendOverlayMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.max_level"));
                return InteractionResult.FAIL;
            }
            // 升级（原版音效：附魔台使用音）——accessor 路线，不依赖 tavern 侧新增方法
            com.bmt.kaleidoscope_world_liquor.event.BrewCommands.advanceBrewLevel(barrel);
            stackTag.putLong(COOLDOWN_TAG, gameTime + COOLDOWN_TICKS);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(stackTag));
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
