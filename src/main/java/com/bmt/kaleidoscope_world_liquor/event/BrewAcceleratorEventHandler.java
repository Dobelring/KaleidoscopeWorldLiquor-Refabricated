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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 原 Forge 版 @EventBusSubscriber 的两个监听器：
 * <ul>
 *   <li>onAnvilUpdate(AnvilUpdateEvent) → Fabric 无铁砧事件，由 mixin/AnvilMenuMixin
 *       在 AnvilMenu#createResult 头部回调本类 {@link #onAnvilUpdate}（见该 mixin）；</li>
 *   <li>onRightClickBlock(RightClickBlock, HIGHEST) → UseBlockCallback
 *       （仅主手、纯服务端结算、非 FakePlayer、2 分钟冷却、任意桶身部位语义全保留）。</li>
 * </ul>
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
     * 原 onAnvilUpdate(AnvilUpdateEvent)：右槽是带臻酿加速的附魔书、左槽不是附魔书时
     * 取消铁砧运算（原 event.setCanceled(true)）。
     *
     * @return true = 取消本次铁砧运算（由 mixins/AnvilMenuMixin 处理输出清空）
     */
    public static boolean onAnvilUpdate(ItemStack left, ItemStack right) {
        return right.getItem() instanceof EnchantedBookItem
            && EnchantmentHelper.getEnchantments(right).containsKey(ModEnchantments.BREW_ACCELERATOR)
            && !(left.getItem() instanceof EnchantedBookItem);
    }

    /**
     * 原 onRightClickBlock(RightClickBlock) → UseBlockCallback。
     * 原 setCanceled + setCancellationResult(FAIL/SUCCESS) → 直接返回 InteractionResult.FAIL/SUCCESS；
     * 未命中任何分支（非附魔书 / 冷却外且非酒桶 / 桶身非 IBarrel）官方未取消 → 返回 PASS。
     */
    private static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && !(player instanceof FakePlayer)) {
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
                if (player.getCooldowns().isOnCooldown(Items.ENCHANTED_BOOK)) {
                    float cooldownPercent = player.getCooldowns().getCooldownPercent(Items.ENCHANTED_BOOK, 0.0F);
                    int remainingTicks = (int) (cooldownPercent * 2400.0F);
                    int remainingSeconds = remainingTicks / 20;
                    player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.cooldown", remainingSeconds), true);
                    return InteractionResult.FAIL; // 原 setCanceled + FAIL
                }

                if (level.getBlockState(pos).getBlock() instanceof BarrelBlock) {
                    BlockEntity be = BarrelBlock.getBarrelEntity(level, pos, level.getBlockState(pos));
                    if (be instanceof IBarrel barrel) {
                        if (!barrel.isBrewing()) {
                            player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.not_brewing"), true);
                            return InteractionResult.FAIL;
                        } else if (barrel.isMaxBrewLevel()) {
                            player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.max_level"), true);
                            return InteractionResult.FAIL;
                        } else {
                            try {
                                BarrelBlockEntityAccessor accessor = (BarrelBlockEntityAccessor) barrel;
                                int newLevel = Math.min(barrel.getBrewLevel() + 1, 6);
                                accessor.setBrewLevel(newLevel);
                                int newBrewTime = accessor.invokeGetBrewTimeForLevel();
                                accessor.setBrewTime(newBrewTime);
                                ((BarrelBlockEntity) barrel).refresh();
                                player.getCooldowns().addCooldown(Items.ENCHANTED_BOOK, 2400);
                                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.2F);
                                player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.success", newLevel), true);
                                return InteractionResult.SUCCESS; // 原 setCanceled + SUCCESS
                            } catch (Exception var13) {
                                player.displayClientMessage(Component.translatable("message.kaleidoscope_world_liquor.brew_accelerator.error"), true);
                                return InteractionResult.FAIL;
                            }
                        }
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }
}
