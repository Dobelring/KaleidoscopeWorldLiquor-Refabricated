package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.effect.ContinuousHealEffect;
import com.bmt.kaleidoscope_world_liquor.effect.CrazyEffect;
import com.bmt.kaleidoscope_world_liquor.effect.DoubleDamageEffect;
import com.bmt.kaleidoscope_world_liquor.effect.ExplosionEffect;
import com.bmt.kaleidoscope_world_liquor.effect.LevelBoostEffect;
import com.bmt.kaleidoscope_world_liquor.effect.RespawnEffect;
import com.bmt.kaleidoscope_world_liquor.init.smc.SMCEffect;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ModEffects {
    public static final MobEffect TEQUILA_EFFECT = new MobEffect(MobEffectCategory.BENEFICIAL, 16766720) {
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    };
    public static final MobEffect CAPTAIN_GIFT_EFFECT = new MobEffect(MobEffectCategory.BENEFICIAL, 2003199) {
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    };
    public static final MobEffect TREASURE_GUIDE_EFFECT = new MobEffect(MobEffectCategory.BENEFICIAL, 16766720) {
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        }

        public boolean isDurationEffectTick(int duration, int amplifier) {
            return false;
        }
    };
    public static final MobEffect MULTI_JUMP_EFFECT = new MobEffect(MobEffectCategory.BENEFICIAL, 65280) {
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        }

        public boolean isDurationEffectTick(int duration, int amplifier) {
            return false;
        }
    };
    public static final MobEffect REVERSE_GRAVITY = new MobEffect(MobEffectCategory.NEUTRAL, 65535) {
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    };
    public static final MobEffect BOATING_MASTER_EFFECT = new MobEffect(MobEffectCategory.BENEFICIAL, 49151) {
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    };
    public static final MobEffect HOSTILE_DETECTION_EFFECT = new MobEffect(MobEffectCategory.BENEFICIAL, 16729156) {
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    };
    public static final MobEffect BEHEADING_EFFECT = new MobEffect(MobEffectCategory.BENEFICIAL, 9109504) {
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    };
    public static final MobEffect FROST_WALKER_EFFECT = new MobEffect(MobEffectCategory.BENEFICIAL, 8900331) {
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    };
    // 官方 1.1.12 移除春野之息（bonemeal_spreader）
    public static final MobEffect TREASURE_SENSE_EFFECT = new MobEffect(MobEffectCategory.BENEFICIAL, 16766720) {
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    };
    public static final MobEffect GROUND_CRIT_EFFECT = new MobEffect(MobEffectCategory.BENEFICIAL, 16737095) {
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    };
    public static final MobEffect EXPLOSION = new ExplosionEffect(16720418);
    public static final MobEffect LEVEL_BOOST = new LevelBoostEffect(3066993);
    public static final MobEffect CONTINUOUS_HEAL = new ContinuousHealEffect();
    public static final MobEffect CRAZY = new CrazyEffect();
    public static final MobEffect RESPAWN = new RespawnEffect();
    public static final MobEffect DOUBLE_DAMAGE_EFFECT = new DoubleDamageEffect();
    public static final MobEffect CREATIVE_FLIGHT = new MobEffect(MobEffectCategory.BENEFICIAL, 8900346) {
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return false;
        }
    };
    // 原版 smc 已装时不代注册肘击效果（否则与 smc 自己的注册冲突），语义与官方 Forge 版一致
    public static final MobEffect ELBOW_STRIKE;

    public ModEffects() {
    }

    static {
        if (!FabricLoader.getInstance().isModLoaded("smc")) {
            ELBOW_STRIKE = new SMCEffect();
        } else {
            ELBOW_STRIKE = null;
        }
    }

    public static void registerEffects() {
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "tequila"), TEQUILA_EFFECT);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "captain_gift"), CAPTAIN_GIFT_EFFECT);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "treasure_guide"), TREASURE_GUIDE_EFFECT);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "multi_jump"), MULTI_JUMP_EFFECT);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "reverse_gravity"), REVERSE_GRAVITY);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "boating_master"), BOATING_MASTER_EFFECT);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "hostile_detection"), HOSTILE_DETECTION_EFFECT);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "beheading"), BEHEADING_EFFECT);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "frost_walker"), FROST_WALKER_EFFECT);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "treasure_sense"), TREASURE_SENSE_EFFECT);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "ground_crit"), GROUND_CRIT_EFFECT);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "explosion"), EXPLOSION);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "level_boost"), LEVEL_BOOST);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "continuous_heal"), CONTINUOUS_HEAL);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "crazy"), CRAZY);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "respawn"), RESPAWN);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "double_damage"), DOUBLE_DAMAGE_EFFECT);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("kaleidoscope_world_liquor", "creative_flight"), CREATIVE_FLIGHT);
        if (ELBOW_STRIKE != null) {
            Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("smc", "elbow_strike"), ELBOW_STRIKE);
        }
    }
}
