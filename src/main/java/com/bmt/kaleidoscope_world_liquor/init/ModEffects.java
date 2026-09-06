package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.effect.BaseEffect;
import com.bmt.kaleidoscope_world_liquor.effect.ContinuousHealEffect;
import com.bmt.kaleidoscope_world_liquor.effect.InstantEffects;
import com.bmt.kaleidoscope_world_liquor.init.smc.SMCEffect;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 18 个本模组效果 + smc 联动效果（Holder 模式，1.21.11 必需）。
 * tick 节奏对应 1.20.1 shouldApplyEffectTickThisTick：
 * 常驻类每 tick 或标记型（TICK_NEVER）；bonemeal 每秒。
 */
public final class ModEffects {
    private ModEffects() {
    }

    public static Holder<MobEffect> TEQUILA;
    public static Holder<MobEffect> CAPTAIN_GIFT;
    public static Holder<MobEffect> TREASURE_GUIDE;
    public static Holder<MobEffect> MULTI_JUMP;
    public static Holder<MobEffect> REVERSE_GRAVITY;
    public static Holder<MobEffect> BOATING_MASTER;
    public static Holder<MobEffect> HOSTILE_DETECTION;
    public static Holder<MobEffect> BEHEADING;
    public static Holder<MobEffect> FROST_WALKER;
    public static Holder<MobEffect> BONEMEAL_SPREADER;
    public static Holder<MobEffect> TREASURE_SENSE;
    public static Holder<MobEffect> GROUND_CRIT;
    public static Holder<MobEffect> EXPLOSION;
    public static Holder<MobEffect> LEVEL_BOOST;
    public static Holder<MobEffect> CONTINUOUS_HEAL;
    public static Holder<MobEffect> CRAZY;
    public static Holder<MobEffect> RESPAWN;
    public static Holder<MobEffect> DOUBLE_DAMAGE;
    /** smc:elbow_strike——smc 模组在场时用其 Holder，缺失时本模组代注册 */
    public static Holder<MobEffect> ELBOW_STRIKE;

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MOD_ID, name), effect);
    }

    private static Holder<MobEffect> registerForeign(String namespace, String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(namespace, name), effect);
    }

    public static void register() {
        TEQUILA = register("tequila", new BaseEffect(MobEffectCategory.BENEFICIAL, 0xFFC700, BaseEffect.TICK_NEVER));
        CAPTAIN_GIFT = register("captain_gift", new BaseEffect(MobEffectCategory.BENEFICIAL, 0x1E93FF, BaseEffect.TICK_NEVER));
        TREASURE_GUIDE = register("treasure_guide", new BaseEffect(MobEffectCategory.BENEFICIAL, 0xFFC700, BaseEffect.TICK_NEVER));
        MULTI_JUMP = register("multi_jump", new BaseEffect(MobEffectCategory.BENEFICIAL, 0x00FF00, BaseEffect.TICK_NEVER));
        REVERSE_GRAVITY = register("reverse_gravity", new BaseEffect(MobEffectCategory.NEUTRAL, 0x00FFFF, BaseEffect.TICK_NEVER));
        BOATING_MASTER = register("boating_master", new BaseEffect(MobEffectCategory.BENEFICIAL, 0xBFFFFF, BaseEffect.TICK_NEVER));
        HOSTILE_DETECTION = register("hostile_detection", new BaseEffect(MobEffectCategory.BENEFICIAL, 0xFF4404, BaseEffect.TICK_NEVER));
        BEHEADING = register("beheading", new BaseEffect(MobEffectCategory.BENEFICIAL, 0x8B0000, BaseEffect.TICK_NEVER));
        FROST_WALKER = register("frost_walker", new BaseEffect(MobEffectCategory.BENEFICIAL, 0x87CEEB, BaseEffect.TICK_NEVER));
        BONEMEAL_SPREADER = register("bonemeal_spreader", new BaseEffect(MobEffectCategory.BENEFICIAL, 0x90D490, BaseEffect.TICK_PER_SECOND));
        TREASURE_SENSE = register("treasure_sense", new BaseEffect(MobEffectCategory.BENEFICIAL, 0xFFC700, BaseEffect.TICK_NEVER));
        GROUND_CRIT = register("ground_crit", new BaseEffect(MobEffectCategory.BENEFICIAL, 0xFF3207, BaseEffect.TICK_NEVER));
        EXPLOSION = register("explosion", new InstantEffects.ExplosionEffect(0xFF0FA2));
        LEVEL_BOOST = register("level_boost", new InstantEffects.LevelBoostEffect(0x2EBD51));
        CONTINUOUS_HEAL = register("continuous_heal", new ContinuousHealEffect());
        CRAZY = register("crazy", new InstantEffects.CrazyEffect());
        RESPAWN = register("respawn", new InstantEffects.RespawnEffect());
        DOUBLE_DAMAGE = register("double_damage", new InstantEffects.DoubleDamageEffect());
        // smc 联动：模组缺失时本模组以 smc 命名空间代注册（与 1.20.1 一致）
        if (FabricLoader.getInstance().isModLoaded("smc")) {
            Holder<MobEffect> smcHolder = BuiltInRegistries.MOB_EFFECT
                    .get(Identifier.fromNamespaceAndPath(SMCEffect.SMC_MODID, "elbow_strike"))
                    .orElse(null);
            ELBOW_STRIKE = smcHolder != null ? smcHolder : registerForeign(SMCEffect.SMC_MODID, "elbow_strike", new SMCEffect());
        } else {
            ELBOW_STRIKE = registerForeign(SMCEffect.SMC_MODID, "elbow_strike", new SMCEffect());
        }
    }
}
