package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.block.WallRecordBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.WallRecordBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModItems;
import com.bmt.kaleidoscope_world_liquor.init.ModJukeboxSongs;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MusicDiscEvents {
   private static final Map<ResourceLocation, Integer> VANILLA_RECORD_MAP = new HashMap<>();
   private static final int RANDOM_MODEL_COUNT = 6;
   private static final int RANDOM_MODEL_START_INDEX = 19;
   private static final Random RANDOM = new Random();
   private static final ResourceLocation DISC_13 = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_13");
   private static final ResourceLocation DISC_CAT = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_cat");
   private static final ResourceLocation DISC_BLOCKS = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_blocks");
   private static final ResourceLocation DISC_CHIRP = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_chirp");
   private static final ResourceLocation DISC_FAR = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_far");
   private static final ResourceLocation DISC_MALL = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_mall");
   private static final ResourceLocation DISC_MELLOHI = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_mellohi");
   private static final ResourceLocation DISC_STAL = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_stal");
   private static final ResourceLocation DISC_STRAD = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_strad");
   private static final ResourceLocation DISC_WARD = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_ward");
   private static final ResourceLocation DISC_11 = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_11");
   private static final ResourceLocation DISC_WAIT = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_wait");
   private static final ResourceLocation DISC_OTHERSIDE = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_otherside");
   private static final ResourceLocation DISC_PIGSTEP = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_pigstep");
   private static final ResourceLocation DISC_5 = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_5");
   private static final ResourceLocation DISC_RELIC = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_relic");
   private static final ResourceLocation DISC_CREATOR = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_creator");
   private static final ResourceLocation DISC_PRECIPICE = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_precipice");
   private static final ResourceLocation DISC_CREATOR_MUSIC_BOX = ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_creator_music_box");

   public static void register() {
      UseBlockCallback.EVENT.register(MusicDiscEvents::onRightClickPlaceRecord);
   }

   public static void registerClient() {
      ItemTooltipCallback.EVENT.register(MusicDiscEvents::onItemTooltip);
   }

   private static InteractionResult onRightClickPlaceRecord(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
      ItemStack stack = player.getItemInHand(hand);
      // 模组唱片放入唱片机前随机选定曲目（长/短），使 length_in_seconds 与实际音乐一致——
      // 否则声音层 50/50 随机会让短曲播完后唱片机仍按长曲时长发音符粒子
      if (!level.isClientSide
         && level.getBlockState(hitResult.getBlockPos()).getBlock() instanceof net.minecraft.world.level.block.JukeboxBlock
         && stack.is(ModItems.CUSTOM_RECORD)) {
         ResourceKey<net.minecraft.world.item.JukeboxSong> song = level.random.nextBoolean()
            ? ModJukeboxSongs.RANDOM_DISC
            : ModJukeboxSongs.RANDOM_DISC_SHORT;
         stack.set(
            DataComponents.JUKEBOX_PLAYABLE,
            new net.minecraft.world.item.JukeboxPlayable(new net.minecraft.world.item.EitherHolder<>(song), true)
         );
      }

      if (player.isShiftKeyDown()) {
         if (stack.has(DataComponents.JUKEBOX_PLAYABLE)) {
            BlockPlaceContext context = new BlockPlaceContext(player, hand, stack, hitResult);
            if (context.canPlace()) {
               BlockState state = ModBlocks.WALL_RECORD.getStateForPlacement(context);
               if (state != null) {
                  BlockPos placePos = context.getClickedPos();
                  if (state.canSurvive(level, placePos)) {
                     ResourceLocation recordId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                     int modelIndex;
                     if (recordId != null && VANILLA_RECORD_MAP.containsKey(recordId)) {
                        modelIndex = VANILLA_RECORD_MAP.get(recordId);
                     } else {
                        modelIndex = 19 + RANDOM.nextInt(6);
                     }

                     state = state.setValue(WallRecordBlock.MODEL_INDEX, modelIndex);
                     if (!level.isClientSide) {
                        if (level.setBlock(placePos, state, 3)) {
                           if (level.getBlockEntity(placePos) instanceof WallRecordBlockEntity be) {
                              be.setRecord(stack);
                           }

                           level.playSound(null, placePos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                           if (!player.getAbilities().instabuild) {
                              stack.shrink(1);
                           }
                        }
                     }
                     return InteractionResult.sidedSuccess(level.isClientSide);
                  }
               }
            }
         }
      }
      return InteractionResult.PASS;
   }

   private static void onItemTooltip(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.TooltipFlag flag, List<Component> lines) {
      if (stack.has(DataComponents.JUKEBOX_PLAYABLE)) {
         lines.add(
            Component.translatable("item.kaleidoscope_world_liquor.music_disc.tooltip")
               .withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
         );
      }
   }

   static {
      VANILLA_RECORD_MAP.put(DISC_13, 0);
      VANILLA_RECORD_MAP.put(DISC_CAT, 1);
      VANILLA_RECORD_MAP.put(DISC_BLOCKS, 2);
      VANILLA_RECORD_MAP.put(DISC_CHIRP, 3);
      VANILLA_RECORD_MAP.put(DISC_FAR, 4);
      VANILLA_RECORD_MAP.put(DISC_MALL, 5);
      VANILLA_RECORD_MAP.put(DISC_MELLOHI, 6);
      VANILLA_RECORD_MAP.put(DISC_STAL, 7);
      VANILLA_RECORD_MAP.put(DISC_STRAD, 8);
      VANILLA_RECORD_MAP.put(DISC_WARD, 9);
      VANILLA_RECORD_MAP.put(DISC_11, 10);
      VANILLA_RECORD_MAP.put(DISC_WAIT, 11);
      VANILLA_RECORD_MAP.put(DISC_OTHERSIDE, 12);
      VANILLA_RECORD_MAP.put(DISC_PIGSTEP, 13);
      VANILLA_RECORD_MAP.put(DISC_5, 14);
      VANILLA_RECORD_MAP.put(DISC_RELIC, 15);
      VANILLA_RECORD_MAP.put(DISC_CREATOR, 16);
      VANILLA_RECORD_MAP.put(DISC_PRECIPICE, 17);
      VANILLA_RECORD_MAP.put(DISC_CREATOR_MUSIC_BOX, 18);
   }
}
