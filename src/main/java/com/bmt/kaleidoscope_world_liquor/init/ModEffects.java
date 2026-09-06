package com.bmt.kaleidoscope_world_liquor.init;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.effect.ContinuousHealEffect;
import com.bmt.kaleidoscope_world_liquor.effect.CrazyEffect;
import com.bmt.kaleidoscope_world_liquor.effect.DoubleDamageEffect;
import com.bmt.kaleidoscope_world_liquor.effect.ExplosionEffect;
import com.bmt.kaleidoscope_world_liquor.effect.LevelBoostEffect;
import com.bmt.kaleidoscope_world_liquor.effect.RespawnEffect;
import com.bmt.kaleidoscope_world_liquor.effect.SMCEffect;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ModEffects {
   public static final Holder<MobEffect> TEQUILA_EFFECT = register(
      "tequila", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 16766720) {
         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
         }
      }
   );
   public static final Holder<MobEffect> CAPTAIN_GIFT_EFFECT = register(
      "captain_gift", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 2003199) {
         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
         }
      }
   );
   public static final Holder<MobEffect> TREASURE_GUIDE_EFFECT = register(
      "treasure_guide", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 16766720) {
         public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            return true;
         }

         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return false;
         }
      }
   );
   public static final Holder<MobEffect> MULTI_JUMP_EFFECT = register(
      "multi_jump", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 65280) {
         public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            return true;
         }

         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return false;
         }
      }
   );
   public static final Holder<MobEffect> REVERSE_GRAVITY = register(
      "reverse_gravity", () -> new MobEffect(MobEffectCategory.NEUTRAL, 65535) {
         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
         }
      }
   );
   public static final Holder<MobEffect> CRAZY_EFFECT = register("crazy", CrazyEffect::new);
   public static final Holder<MobEffect> RESPAWN_EFFECT = register("respawn", RespawnEffect::new);
   public static final Holder<MobEffect> DOUBLE_DAMAGE_EFFECT = register("double_damage", DoubleDamageEffect::new);
   public static final Holder<MobEffect> BOATING_MASTER_EFFECT = register(
      "boating_master", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 49151) {
         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
         }
      }
   );
   public static final Holder<MobEffect> HOSTILE_DETECTION_EFFECT = register(
      "hostile_detection", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 16729156) {
         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
         }
      }
   );
   public static final Holder<MobEffect> BEHEADING_EFFECT = register(
      "beheading", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 9109504) {
         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
         }
      }
   );
   public static final Holder<MobEffect> FROST_WALKER_EFFECT = register(
      "frost_walker", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 8900331) {
         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
         }
      }
   );
   public static final Holder<MobEffect> BONEMEAL_SPREADER_EFFECT = register(
      "bonemeal_spreader", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 9498256) {
         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration % 20 == 0;
         }
      }
   );
   public static final Holder<MobEffect> TREASURE_SENSE_EFFECT = register(
      "treasure_sense", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 16766720) {
         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
         }
      }
   );
   public static final Holder<MobEffect> GROUND_CRIT_EFFECT = register(
      "ground_crit", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 16737095) {
         public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
         }
      }
   );
   public static final Holder<MobEffect> EXPLOSION_EFFECT = register("explosion", () -> new ExplosionEffect(16729344));
   public static final Holder<MobEffect> LEVEL_BOOST_EFFECT = register("level_boost", () -> new LevelBoostEffect(65280));
   public static final Holder<MobEffect> CONTINUOUS_HEAL_EFFECT = register("continuous_heal", ContinuousHealEffect::new);

   // smc 联动：当 smc 模组未加载时注册 smc:elbow_strike（冰红茶 datamap 引用此效果）
   public static final Holder<MobEffect> ELBOW_STRIKE = FabricLoader.getInstance().isModLoaded("smc")
      ? null
      : Registry.registerForHolder(
         BuiltInRegistries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath("smc", "elbow_strike"), new SMCEffect()
      );

   private static Holder<MobEffect> register(String name, Supplier<MobEffect> effectSupplier) {
      return Registry.registerForHolder(
         BuiltInRegistries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(KaleidoscopeWorldLiquor.MODID, name), effectSupplier.get()
      );
   }

   public static void registerEffects() {
      // 所有效果已在静态初始化时注册
   }
}
