package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.KaleidoscopeWorldLiquor;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 唱片贴墙放置（潜行右键任意墙面 → wall_record）与唱片 tooltip 追加。
 * 1.21.11 无 RecordItem：用 JUKEBOX_PLAYABLE 组件判定唱片。
 * 原版 16 首唱片对应 0-15 贴图，其余（含 custom_record）随机 16-21。
 */
public final class MusicDiscEvents {
    private static final Map<Identifier, Integer> VANILLA_RECORD_MAP = new HashMap<>();
    private static final int RANDOM_MODEL_COUNT = 6;
    private static final int RANDOM_MODEL_START_INDEX = 16;
    private static final RandomSource RANDOM = RandomSource.create();

    private MusicDiscEvents() {
    }

    public static void register() {
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_13"), 0);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_cat"), 1);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_blocks"), 2);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_chirp"), 3);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_far"), 4);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_mall"), 5);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_mellohi"), 6);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_stal"), 7);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_strad"), 8);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_ward"), 9);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_11"), 10);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_wait"), 11);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_otherside"), 12);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_pigstep"), 13);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_5"), 14);
        VANILLA_RECORD_MAP.put(Identifier.withDefaultNamespace("music_disc_relic"), 15);

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!player.isCrouching() || hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            // 唱片 = 带 jukebox_playable 组件的物品
            if (stack.get(DataComponents.JUKEBOX_PLAYABLE) == null) {
                return InteractionResult.PASS;
            }
            Level level = world;
            BlockPlaceContext context = new BlockPlaceContext(player, hand, stack, hitResult);
            if (context.canPlace()) {
                BlockState state = ModBlocks.WALL_RECORD.getStateForPlacement(context);
                if (state != null) {
                    BlockPos placePos = context.getClickedPos();
                    if (state.canSurvive(level, placePos) && level.getBlockState(placePos).canBeReplaced()) {
                        Identifier recordId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
                        int modelIndex = VANILLA_RECORD_MAP.getOrDefault(recordId,
                                RANDOM_MODEL_START_INDEX + RANDOM.nextInt(RANDOM_MODEL_COUNT));
                        BlockState withIndex = state.setValue(com.bmt.kaleidoscope_world_liquor.block.WallRecordBlock.MODEL_INDEX, modelIndex);
                        level.setBlock(placePos, withIndex, 3);
                        // BE 记录唱片物品
                        if (level.getBlockEntity(placePos) instanceof com.bmt.kaleidoscope_world_liquor.blockentity.WallRecordBlockEntity be) {
                            be.setRecord(stack.copyWithCount(1));
                        }
                        level.playSound(null, placePos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }

    /** 唱片 tooltip："可放置"（暗灰斜体）。1.21.11 挂 item tooltip 需要客户端 mixin；此处静态工具方法供 mixin 调用 */
    @Environment(EnvType.CLIENT)
    public static boolean appendPlaceTooltip(ItemStack stack) {
        return stack.get(DataComponents.JUKEBOX_PLAYABLE) != null;
    }
}
