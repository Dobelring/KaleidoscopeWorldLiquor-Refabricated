package com.bmt.kaleidoscope_world_liquor.event;

import com.bmt.kaleidoscope_world_liquor.block.WallRecordBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.WallRecordBlockEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModBlocks;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 原 Forge 版 @EventBusSubscriber 的两个监听器：
 * <ul>
 *   <li>onRightClickPlaceRecord(RightClickBlock, LOW) → UseBlockCallback
 *       （潜行右键唱片直接放置挂墙唱片机；保留双侧计算 + 仅服务端落地）；</li>
 *   <li>onItemTooltip(ItemTooltipEvent) → ItemTooltipCallback（客户端 API，
 *       由 {@link #registerClient()} 在客户端入口点注册）。</li>
 * </ul>
 * Fabric 的 UseBlockCallback 无优先级：实际注册序（主类）在本模组 DollInteractionEvents 之后、
 * BrewAcceleratorEventHandler 之前，未复刻原 LOW——三者由手持物品种类互斥触发，顺序无行为影响。
 */
public class MusicDiscEvents {
    private static final Map<ResourceLocation, Integer> VANILLA_RECORD_MAP = new HashMap<>();
    private static final int RANDOM_MODEL_COUNT = 6;
    private static final int RANDOM_MODEL_START_INDEX = 16;
    private static final Random RANDOM = new Random();
    private static final ResourceLocation DISC_13 = new ResourceLocation("minecraft:music_disc_13");
    private static final ResourceLocation DISC_CAT = new ResourceLocation("minecraft:music_disc_cat");
    private static final ResourceLocation DISC_BLOCKS = new ResourceLocation("minecraft:music_disc_blocks");
    private static final ResourceLocation DISC_CHIRP = new ResourceLocation("minecraft:music_disc_chirp");
    private static final ResourceLocation DISC_FAR = new ResourceLocation("minecraft:music_disc_far");
    private static final ResourceLocation DISC_MALL = new ResourceLocation("minecraft:music_disc_mall");
    private static final ResourceLocation DISC_MELLOHI = new ResourceLocation("minecraft:music_disc_mellohi");
    private static final ResourceLocation DISC_STAL = new ResourceLocation("minecraft:music_disc_stal");
    private static final ResourceLocation DISC_STRAD = new ResourceLocation("minecraft:music_disc_strad");
    private static final ResourceLocation DISC_WARD = new ResourceLocation("minecraft:music_disc_ward");
    private static final ResourceLocation DISC_11 = new ResourceLocation("minecraft:music_disc_11");
    private static final ResourceLocation DISC_WAIT = new ResourceLocation("minecraft:music_disc_wait");
    private static final ResourceLocation DISC_OTHERSIDE = new ResourceLocation("minecraft:music_disc_otherside");
    private static final ResourceLocation DISC_PIGSTEP = new ResourceLocation("minecraft:music_disc_pigstep");
    private static final ResourceLocation DISC_5 = new ResourceLocation("minecraft:music_disc_5");
    private static final ResourceLocation DISC_RELIC = new ResourceLocation("minecraft:music_disc_relic");

    public MusicDiscEvents() {
    }

    /** 服务端+客户端共用的 UseBlockCallback 注册（主类调用）。 */
    public static void register() {
        UseBlockCallback.EVENT.register(MusicDiscEvents::onRightClickPlaceRecord);
    }

    /**
     * 客户端 tooltip 注册（原 ItemTooltipEvent 是双侧 Forge 事件，tooltip 只在客户端计算；
     * Fabric 的 ItemTooltipCallback 是客户端 API）——由客户端入口点
     * KaleidoscopeWorldLiquorClient#onInitializeClient 调用，服务端不触碰。
     */
    public static void registerClient() {
        ItemTooltipCallback.EVENT.register(MusicDiscEvents::onItemTooltip);
    }

    /**
     * 原 onRightClickPlaceRecord(RightClickBlock, LOW) → UseBlockCallback。
     * 原 setCanceled + setCancellationResult(sidedSuccess) → 命中放置分支返回
     * InteractionResult.sidedSuccess(level.isClientSide)（客户端摆手、服务端落地）；
     * 未命中（非潜行/非唱片/放不下）官方未取消 → 返回 PASS。
     */
    private static InteractionResult onRightClickPlaceRecord(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof RecordItem) {
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
                                modelIndex = 16 + RANDOM.nextInt(6);
                            }

                            state = state.setValue(WallRecordBlock.MODEL_INDEX, modelIndex);
                            if (!level.isClientSide) {
                                if (level.setBlock(placePos, state, 3)) {
                                    if (level.getBlockEntity(placePos) instanceof WallRecordBlockEntity be) {
                                        be.setRecord(stack);
                                    }

                                    level.playSound(null, placePos, SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                                    if (!player.getAbilities().instabuild) {
                                        stack.shrink(1);
                                    }
                                }
                            }
                            return InteractionResult.sidedSuccess(level.isClientSide); // 原 setCanceled + cancellationResult
                        }
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    /** 原 onItemTooltip(ItemTooltipEvent) → ItemTooltipCallback（参数即原 event 的取值）。 */
    private static void onItemTooltip(ItemStack stack, TooltipFlag tooltipFlag, List<Component> lines) {
        if (stack.getItem() instanceof RecordItem) {
            lines.add(
                Component.translatable("item.kaleidoscope_world_liquor.music_disc.tooltip")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
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
    }
}
