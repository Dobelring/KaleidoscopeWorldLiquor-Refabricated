package com.bmt.kaleidoscope_world_liquor.compat.create;

import com.bmt.kaleidoscope_world_liquor.block.FreezerBlock;
import com.bmt.kaleidoscope_world_liquor.block.entity.FreezerBlockEntity;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;

/**
 * 官方 1.2.0：装置上的虚拟方块实体系统（ContraptionVirtualBlockEntities 移植）——
 * 冰箱进入装置后，虚拟 BE 持续 tick（真实合成推进），进度每 4t 回写装置 NBT 并同步客户端。
 */
public final class ContraptionVirtualBlockEntities {
    private static final int SAVE_INTERVAL = 4;
    private static final int DISCOVER_INTERVAL = 20;
    private static final Map<UUID, Data> DATA = new HashMap<>();
    private static int discoverTimer;

    private ContraptionVirtualBlockEntities() {
    }

    public static boolean isTickedBlock(BlockState state) {
        return state.getBlock() instanceof FreezerBlock;
    }

    public static Data getOrCreate(AbstractContraptionEntity entity) {
        return DATA.computeIfAbsent(entity.getUUID(), id -> new Data(entity));
    }

    @Nullable
    public static BlockEntity getOrCreateBlockEntity(AbstractContraptionEntity entity, BlockPos localPos, StructureBlockInfo info) {
        Data data = getOrCreate(entity);
        BlockEntity existing = data.blockEntities.get(localPos);
        if (existing != null) {
            return existing;
        } else {
            BlockEntity be = create(info.state(), localPos);
            if (be == null) {
                return null;
            } else {
                be.setLevel(data.level);
                if (info.nbt() != null) {
                    be.load(info.nbt().copy());
                }

                if (be instanceof FreezerBlockEntity freezer) {
                    freezer.refreshRecipeCache();
                }

                data.blockEntities.put(localPos, be);
                data.level.putBlockEntity(localPos, be);
                return be;
            }
        }
    }

    public static void setBlock(AbstractContraptionEntity entity, BlockPos localPos, BlockState state) {
        getOrCreate(entity).level.setBlock(localPos, state, 3);
    }

    @Nullable
    private static BlockEntity create(BlockState state, BlockPos pos) {
        return state.getBlock() instanceof FreezerBlock ? new FreezerBlockEntity(pos, state) : null;
    }

    public static void tickAll(MinecraftServer server) {
        if (++discoverTimer >= DISCOVER_INTERVAL) {
            discoverTimer = 0;
            discover(server);
        }

        Iterator<Entry<UUID, Data>> iterator = DATA.entrySet().iterator();

        while (iterator.hasNext()) {
            Data data = iterator.next().getValue();
            AbstractContraptionEntity entity = data.entity;
            if (!entity.isRemoved() && entity.getContraption() != null && entity.level() instanceof ServerLevel) {
                tickContraption(data);
            } else {
                iterator.remove();
            }
        }
    }

    private static void discover(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof AbstractContraptionEntity contraptionEntity
                    && contraptionEntity.getContraption() != null
                    && !DATA.containsKey(contraptionEntity.getUUID())) {
                    Data data = getOrCreate(contraptionEntity);
                    if (data.positions.isEmpty()) {
                        DATA.remove(contraptionEntity.getUUID());
                    }
                }
            }
        }
    }

    private static void tickContraption(Data data) {
        AbstractContraptionEntity entity = data.entity;

        for (BlockPos pos : data.positions) {
            StructureBlockInfo info = ContraptionBlocks.getInfo(entity, pos);
            if (info == null) {
                BlockEntity removed = data.blockEntities.remove(pos);
                if (removed != null) {
                    data.level.removeBlockEntity(pos);
                }
            } else if (getOrCreateBlockEntity(entity, pos, info) instanceof FreezerBlockEntity freezer) {
                FreezerBlockEntity.tick(data.level, pos, info.state(), freezer);
                if (data.tickCount % SAVE_INTERVAL == 0) {
                    ContraptionBlocks.updateNbt(entity, pos, ContraptionBlocks.save(freezer, data.level));
                }
            }
        }

        data.tickCount++;
    }

    public static void clear() {
        DATA.clear();
        discoverTimer = 0;
    }

    public static final class Data {
        public final AbstractContraptionEntity entity;
        public final ContraptionVirtualLevel level;
        public final List<BlockPos> positions;
        public final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
        int tickCount;

        Data(AbstractContraptionEntity entity) {
            this.entity = entity;
            this.level = new ContraptionVirtualLevel(entity);
            List<BlockPos> list = new ArrayList<>();

            for (StructureBlockInfo info : entity.getContraption().getBlocks().values()) {
                if (ContraptionVirtualBlockEntities.isTickedBlock(info.state())) {
                    list.add(info.pos());
                }
            }

            this.positions = list;
        }
    }
}
