package com.bmt.kaleidoscope_world_liquor.mixin;

import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 幸运+（官方 1.1.12）的 Fabric 实现，替代 NeoForge 全局战利品修改器。
 * 挂在 {@code LootTable#getRandomItemsRaw(LootContext, Consumer)}——所有顶层入口
 * （箱子/方块掉落/实体掉落/钓鱼）最终都经此方法；嵌套子表（loot_table 引用）也经
 * 此方法，故用深度计数只认顶层。命中幸运+ 后取消原调用，改为「收集本表正常掉落
 * → 按档位每档 25% 重掷本表、只补未出现过的新物品 → 一次性转发」，
 * 与官方 LuckLootModifier 行为一致（重掷经同一深度守卫天然防自递归）。
 */
@Mixin(LootTable.class)
public class LootTableLuckMixin {
    @Unique
    private static final ThreadLocal<Integer> KWL_LUCK_DEPTH = ThreadLocal.withInitial(() -> 0);
    @Unique
    private static final float KWL_CHANCE_PER_LEVEL = 0.25F;
    @Unique
    private static final int KWL_MAX_LEVEL = 5;

    @Inject(
        method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void kaleidoscopeWorldLiquor$applyLuck(LootContext context, Consumer<ItemStack> output, CallbackInfo ci) {
        if (KWL_LUCK_DEPTH.get() > 0) {
            return;
        }

        LivingEntity holder = findLuckHolder(context);
        if (holder == null) {
            return;
        }

        MobEffectInstance effect = holder.getEffect(ModEffects.LUCK);
        if (effect == null) {
            return;
        }

        int level = Math.min(effect.getAmplifier() + 1, KWL_MAX_LEVEL);
        ci.cancel();
        LootTable table = (LootTable)(Object)this;
        ObjectArrayList<ItemStack> generatedLoot = new ObjectArrayList<>();
        KWL_LUCK_DEPTH.set(1);
        try {
            table.getRandomItems(context, generatedLoot::add);
            Set<Item> seenItems = new HashSet<>();
            for (ItemStack stack : generatedLoot) {
                seenItems.add(stack.getItem());
            }

            for (int i = 0; i < level; i++) {
                if (context.getRandom().nextFloat() < KWL_CHANCE_PER_LEVEL) {
                    table.getRandomItems(context, stack -> {
                        if (!stack.isEmpty() && seenItems.add(stack.getItem())) {
                            generatedLoot.add(stack);
                        }
                    });
                }
            }
        } finally {
            KWL_LUCK_DEPTH.set(0);
        }

        for (ItemStack stack : generatedLoot) {
            output.accept(stack);
        }
    }

    @Unique
    private static LivingEntity findLuckHolder(LootContext context) {
        Entity rawThis = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        LivingEntity thisEntity = asLiving(rawThis);
        if (thisEntity == null && rawThis instanceof FishingHook hook) {
            thisEntity = hook.getPlayerOwner();
        }

        LivingEntity lastDamagePlayer = asLiving(context.getOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER));
        LivingEntity attacker = asLiving(context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY));
        LivingEntity directAttacker = asLiving(context.getOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY));
        LivingEntity[] candidates = new LivingEntity[]{lastDamagePlayer, attacker, directAttacker, thisEntity};
        for (LivingEntity candidate : candidates) {
            if (candidate != null && candidate.hasEffect(ModEffects.LUCK)) {
                return candidate;
            }
        }

        return null;
    }

    @Unique
    private static LivingEntity asLiving(Entity entity) {
        return entity instanceof LivingEntity living ? living : null;
    }
}
