package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.init.ModSounds;
import com.bmt.kaleidoscope_world_liquor.init.kaleidoscope_twilight.KTItems;
import com.bmt.kaleidoscope_world_liquor.init.smc.SMCItems;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 喝东西音效（cool_tea / sour_plum）与 smc 肘击触发（喝冰红茶给肘击）。
 * 由 UseItemEventsMixin 的四个生命周期钩子驱动，与 1.20.1 Forge 四事件一致：
 * - 客户端 tick：DRINK 动画每 4t 播一次啜饮音
 * - 服务端 tick：每 10t 播一次吞咽音（ice_tea_eat / sour_plum_drink）
 * - Finish：清 map 并播尾音；Stop：清 map
 * - Finish 且是冰红茶：smc 在场时给 elbow_strike（原 SMCIntegrationEvents）
 */
public final class DrinkingSounds {
    private static final Map<UUID, Integer> drinkingTicksMap = new HashMap<>();
    private static final Identifier COOL_ICE_TEA_ITEM = Identifier.fromNamespaceAndPath("kaleidoscope_world_liquor", "cool_tea");
    private static final Identifier SOUR_PLUM_ITEM = Identifier.fromNamespaceAndPath("kaleidoscope_world_liquor", "sour_plum");

    private DrinkingSounds() {
    }

    private static boolean isIceTeaItem(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null && id.equals(COOL_ICE_TEA_ITEM)) {
            return true;
        }
        // smc 联动的冰红茶也算（原版 ice_tea 物品同 id 双命名空间判断）
        return id != null && id.getNamespace().equals("smc") && id.getPath().equals("ice_tea");
    }

    private static boolean isSourPlum(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.equals(SOUR_PLUM_ITEM);
    }

    /** 手持物品是否带本模组自定义喝声音效（酒柜客户端拦截判定用） */
    public static boolean hasCustomDrinkSound(ItemStack stack) {
        return isCoolIceTea(stack) || isSourPlum(stack) || isSmcIceTea(stack);
    }

    private static boolean isSmcIceTea(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.getNamespace().equals("smc") && id.getPath().equals("ice_tea");
    }

    private static boolean isCoolIceTea(ItemStack stack) {
        // 1.20.1 语义：啜饮音只属于劲爽冰啤酒（cool_tea 精确匹配），
        // smc 冰红茶走自己的吞咽音，两者互不混音
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.equals(COOL_ICE_TEA_ITEM);
    }

    public static void onStart(LivingEntity entity, ItemStack stack) {
        if (entity instanceof Player player) {
            // isIceTeaItem 涵盖 cool_tea 与 smc 冰红茶：两者都要记录吞咽计数
            if (isIceTeaItem(stack) || isSourPlum(stack)) {
                drinkingTicksMap.put(player.getUUID(), 0);
            }
        }
    }

    public static void onTick(LivingEntity entity, ItemStack stack) {
        Level level = entity.level();
        // 客户端：啜饮音（每 4t）
        if (level.isClientSide() && isCoolIceTea(stack) && stack.getUseAnimation() == ItemUseAnimation.DRINK) {
            int usedTicks = stack.getUseDuration(entity) - entity.getUseItemRemainingTicks();
            if (usedTicks % 4 == 0) {
                playSoundNear((Player) entity, ModSounds.COOL_ICE_TEA_DRINK);
            }
        }
        if (level.isClientSide() && isSourPlum(stack) && stack.getUseAnimation() == ItemUseAnimation.DRINK) {
            int usedTicks = stack.getUseDuration(entity) - entity.getUseItemRemainingTicks();
            if (usedTicks % 4 == 0) {
                playSoundNear((Player) entity, ModSounds.SOUR_PLUM_DRINK);
            }
        }
        // 服务端：吞咽音（每 10t）——只给 smc 冰红茶；劲爽冰啤酒/cooler 有自己的啜饮音，
        // 原版两条音轨同时触发会混音（用户要求修复）
        if (entity instanceof Player player) {
            if (isIceTeaItem(stack)) {
                UUID playerId = player.getUUID();
                if (drinkingTicksMap.containsKey(playerId)) {
                    int ticks = drinkingTicksMap.get(playerId);
                    drinkingTicksMap.put(playerId, ticks + 1);
                    if (ticks % 10 == 0 && !level.isClientSide() && isSmcIceTea(stack)) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.ICE_TEA_EAT, SoundSource.PLAYERS, 0.5F, 1.0F);
                    }
                }
            }
            if (isSourPlum(stack)) {
                UUID playerId = player.getUUID();
                if (drinkingTicksMap.containsKey(playerId)) {
                    int ticks = drinkingTicksMap.get(playerId);
                    drinkingTicksMap.put(playerId, ticks + 1);
                    if (ticks % 10 == 0 && !level.isClientSide()) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.SOUR_PLUM_DRINK, SoundSource.PLAYERS, 0.5F, 1.0F);
                    }
                }
            }
        }
    }

    public static void onFinish(LivingEntity entity, ItemStack itemStack) {
        if (entity instanceof Player player) {
            if (isIceTeaItem(itemStack)) {
                UUID playerId = player.getUUID();
                drinkingTicksMap.remove(playerId);
                if (!player.level().isClientSide() && isSmcIceTea(itemStack)) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModSounds.ICE_TEA_EAT, SoundSource.PLAYERS, 1.3F, 0.7F);
                    // smc 肘击（原 SMCIntegrationEvents）：喝完 smc 冰红茶给 1000t/2 级
                    player.addEffect(new MobEffectInstance(ModEffects.ELBOW_STRIKE, 1000, 2));
                }
            }
            if (isSourPlum(itemStack)) {
                UUID playerId = player.getUUID();
                drinkingTicksMap.remove(playerId);
                if (!player.level().isClientSide()) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModSounds.SOUR_PLUM_DRINK, SoundSource.PLAYERS, 1.3F, 0.7F);
                }
            }
        }
    }

    public static void onStop(LivingEntity entity, ItemStack itemStack) {
        if (entity instanceof Player player) {
            if (isIceTeaItem(itemStack)) {
                drinkingTicksMap.remove(player.getUUID());
            }
            if (isSourPlum(itemStack)) {
                drinkingTicksMap.remove(player.getUUID());
            }
        }
    }

    private static void playSoundNear(Player player, net.minecraft.sounds.SoundEvent sound) {
        player.level().playSound(player, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 0.5F, 1.0F);
    }
}
