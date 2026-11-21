package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.init.ModEnchantments;
import com.bmt.kaleidoscope_world_liquor.mixins.accessor.BarrelBlockEntityAccessor;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 原 Forge 版 @EventBusSubscriber 的右键监听器：
 * onRightClickBlock(RightClickBlock, HIGHEST) → UseBlockCallback
 * （仅主手、纯服务端结算、2 分钟每书 NBT 冷却、任意桶身部位语义全保留）。
 * 官方 1.2.0：铁砧拦截已随每书 NBT 冷却机制整体移除（原 AnvilUpdateEvent 监听与
 * mixins/AnvilMenuMixin 删除）；FakePlayer 不再被主条件排除——Create 等 FakePlayer
 * 可以正常触发加速，只是不弹消息（notify 屏蔽）。
 * <p>
 * Fabric 的 UseBlockCallback 无优先级：实际注册序（主类）在本模组 DollInteractionEvents /
 * MusicDiscEvents 之后，未复刻原 HIGHEST——三者由手持物品种类互斥触发（附魔书/唱片/布丁），
 * 注册顺序无行为影响。
 */
public class BrewAcceleratorEventHandler {
    public BrewAcceleratorEventHandler() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register(BrewAcceleratorEventHandler::onRightClickBlock);
    }

    /**
     * 原 onRightClickBlock(RightClickBlock) → UseBlockCallback。
     * 原 setCanceled + setCancellationResult(FAIL/SUCCESS) → 直接返回 InteractionResult.FAIL/SUCCESS；
     * 未命中任何分支（非附魔书 / 冷却外且非酒桶 / 桶身非 IBarrel）官方未取消 → 返回 PASS。
     */
    private static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            boolean isEnchantedBook = stack.getItem() instanceof EnchantedBookItem;
            int enchantLevel = 0;
            if (isEnchantedBook) {
                for (Tag tag : EnchantedBookItem.getEnchantments(stack)) {
                    CompoundTag enchantTag = (CompoundTag) tag;
                    ResourceLocation enchantId = ResourceLocation.tryParse(enchantTag.getString("id"));
                    if (enchantId != null && enchantId.equals(BuiltInRegistries.ENCHANTMENT.getKey(ModEnchantments.BREW_ACCELERATOR))) {
                        enchantLevel = enchantTag.getInt("lvl");
                        break;
                    }
                }
            }

            if (isEnchantedBook && enchantLevel != 0) {
                // 官方 1.2.0：冷却改为每本书自身 NBT（kaleidoscope_brew_cooldown_until），不再全局冷却附魔书
                long gameTime = level.getGameTime();
                net.minecraft.nbt.CompoundTag stackTag = stack.getOrCreateTag();
                long cooldownUntil = stackTag.getLong("kaleidoscope_brew_cooldown_until");
                if (cooldownUntil > gameTime) {
                    notify(player, Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.cooldown", (int) ((cooldownUntil - gameTime) / 20L)));
                    return InteractionResult.FAIL;
                }

                if (level.getBlockState(pos).getBlock() instanceof BarrelBlock) {
                    BlockEntity be = BarrelBlock.getBarrelEntity(level, pos, level.getBlockState(pos));
                    if (be instanceof IBarrel barrel) {
                        if (!barrel.isBrewing()) {
                            notify(player, Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.not_brewing"));
                            return InteractionResult.FAIL;
                        } else if (barrel.isMaxBrewLevel()) {
                            notify(player, Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.max_level"));
                            return InteractionResult.FAIL;
                        } else {
                            try {
                                BarrelBlockEntityAccessor accessor = (BarrelBlockEntityAccessor) barrel;
                                int newLevel = Math.min(barrel.getBrewLevel() + 1, 6);
                                accessor.setBrewLevel(newLevel);
                                int newBrewTime = accessor.invokeGetBrewTimeForLevel();
                                accessor.setBrewTime(newBrewTime);
                                ((BarrelBlockEntity) barrel).refresh();
                                stackTag.putLong("kaleidoscope_brew_cooldown_until", gameTime + 2400L);
                                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.2F);
                                notify(player, Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.success", newLevel));
                                return InteractionResult.SUCCESS; // 原 setCanceled + SUCCESS
                            } catch (Exception var13) {
                                notify(player, Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.error"));
                                return InteractionResult.FAIL;
                            }
                        }
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    /** 官方 1.2.0：FakePlayer（Create 机械臂等）静默触发，不弹 actionbar 消息。 */
    private static void notify(Player player, Component message) {
        if (!(player instanceof FakePlayer)) {
            player.displayClientMessage(message, true);
        }
    }
}
