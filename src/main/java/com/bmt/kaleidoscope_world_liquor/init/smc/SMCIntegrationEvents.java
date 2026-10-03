package com.bmt.kaleidoscope_world_liquor.init.smc;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * smc 联动事件（原 Forge 版由主类在 {@code ModList.isLoaded("smc")} 守卫下
 * {@code MinecraftForge.EVENT_BUS.register(SMCIntegrationEvents.class)} 注册；
 * Fabric 版同样只在 smc 已装时由主类调用 {@link #register()}）。
 */
public class SMCIntegrationEvents {
    private static final ResourceLocation ELBOWING_EFFECT_ID = new ResourceLocation("smc", "elbow_strike");
    private static final ResourceLocation SMC_ORIGINAL_ICE_TEA_ID = new ResourceLocation("smc", "ice_tea");

    // 原 Forge：RegistryObject<MobEffect> / RegistryObject<Item>（smc 未装时静态块里为 null）。
    // Fabric 无 RegistryObject → 改为注册表懒查找后缓存（首次使用时解析，
    // 避免依赖主类里 ModEffects / SMCItems 的注册先后顺序）。
    private static MobEffect ELBOWING_EFFECT;
    private static Item SMC_ORIGINAL_ICE_TEA;

    public SMCIntegrationEvents() {
    }

    /**
     * 原 Forge 版没有同名方法（主类直接 {@code EVENT_BUS.register(类)} 挂 @SubscribeEvent）；
     * Fabric 无事件总线，改为主类守卫调用本方法。
     * <p>
     * TODO(fabric): 原 {@code LivingEntityUseItemEvent.Finish}（玩家用完手上物品）
     * 在 Fabric 1.20.1 没有等价回调——已核对 fabric-api 0.92.12+1.20.1 的
     * entity-events / lifecycle-events / events-interaction / item-api 全部事件类，
     * 只有 UseItemCallback（「开始使用」，语义相反）与
     * ServerLivingEntityEvents.ALLOW_DAMAGE/ALLOW_DEATH/AFTER_DEATH 等（粒度不符），
     * CF/CK/TV 三个参考工程也无先例。等价实现需 mixin 进
     * {@code LivingEntity.completeUsingItem}（@Inject HEAD，且 {@code !level.isClientSide}，
     * 用 {@code livingEntity.getUseItem()} 取用完的栈）后调用
     * {@link #onPlayerFinishDrink(LivingEntity, ItemStack)}；接线：mixins/CompleteUsingItemMixin（completeUsingItem HEAD、服务端）已接入。
     */
    public static void register() {
    }

    /**
     * 原 Forge：{@code @SubscribeEvent public static void onPlayerFinishDrink(Finish event)}。
     * 逻辑逐行保留，只是把 event 参数拆成（使用物品的实体, 用完的物品栈），
     * 待上面的 mixin 接入后调用。
     */
    public static void onPlayerFinishDrink(LivingEntity entity, ItemStack usedItem) {
        if (entity instanceof Player player) {
            MobEffect elbowingEffect = getElbowingEffect();
            Item smcOriginalIceTea = getSmcOriginalIceTea();
            if (elbowingEffect != null) { // 不再按 smc 装载门控：smc 未装时解析到本模组代注册的 smc:elbow_strike
                Item item = usedItem.getItem();
                boolean isIceTea = false;
                if (item == SMCItems.SMC_ICE_TEA_ITEM) {
                    isIceTea = true;
                } else if (smcOriginalIceTea != null && item == smcOriginalIceTea) {
                    isIceTea = true;
                }

                if (isIceTea) {
                    if (usedItem.getTag() == null) {
                        player.addEffect(new MobEffectInstance(elbowingEffect, 1000, 2));
                        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 1));
                        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400, 0));
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
                    }
                }
            }
        }
    }

    /** 原 {@code RegistryObject.create(ResourceLocation("smc", "elbow_strike"), ForgeRegistries.MOB_EFFECTS)}；缺失时保持 null（原 isPresent/get 语义）。 */
    private static MobEffect getElbowingEffect() {
        if (ELBOWING_EFFECT == null) { // smc 未装时同样解析：本模组在 !smc 环境代注册了 smc:elbow_strike
            ELBOWING_EFFECT = BuiltInRegistries.MOB_EFFECT.getOptional(ELBOWING_EFFECT_ID).orElse(null);
        }
        return ELBOWING_EFFECT;
    }

    /** 原 {@code RegistryObject.create(ResourceLocation("smc", "ice_tea"), ForgeRegistries.ITEMS)}；缺失时保持 null（原 isPresent 语义）。 */
    private static Item getSmcOriginalIceTea() {
        if (SMC_ORIGINAL_ICE_TEA == null && FabricLoader.getInstance().isModLoaded("smc")) {
            SMC_ORIGINAL_ICE_TEA = BuiltInRegistries.ITEM.getOptional(SMC_ORIGINAL_ICE_TEA_ID).orElse(null);
        }
        return SMC_ORIGINAL_ICE_TEA;
    }
}
