package com.bmt.kaleidoscope_world_liquor.client.render;

import com.bmt.kaleidoscope_world_liquor.api.IGlowingEntity;
import com.bmt.kaleidoscope_world_liquor.init.ModEffects;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * 宝藏感知（treasure_sense）。
 * 每 1s 扫描 2 chunk（24 格）内的箱子/陷阱箱/木桶，主通道末尾画穿墙线框；
 * 24 格内的运输矿车加入 GOLD 发光队伍（1.20.1 原版 kaleidoscope_gold_glow）。
 * 颜色：陷阱箱红、箱子/木桶金（1.20.1 原值 16729156/16766720）。
 *
 * 穿墙实现（1.21.11 已验证方案移植，照 Fabric 官方文档 "Rendering in the World" 穿墙示例）：
 * - 原版 Gizmos.setAlwaysOnTop = 清主目标深度后重画，Iris 重定向帧缓冲后失效（光影下不穿墙）；
 *   自定义 RenderType + MultiBufferSource 提交会被 Iris 丢弃（光影下完全不显示）。
 * - 官方方案：`RenderPipelines.register(RenderPipeline.builder(LINES_SNIPPET)
 *   .withDepthTestFunction(NO_DEPTH_TEST))` 注册自定义管线，BufferBuilder 收集顶点，
 *   RenderPass 直画主渲染目标（GpuDevice 编码器 + MappableRingBuffer + drawIndexed）。
 * - Iris 兼容：IrisPipelines.assignPipeline(管线, ShaderKey.LINES) 反射绑定（Iris 只给
 *   已知 shader key 渲染，未知管线走 FAKE_FUNCTION 被跳过）；绘制时机用
 *   AFTER_TRANSLUCENT_TERRAIN（官方示例时机；BEFORE_TRANSLUCENT_TERRAIN 太早，
 *   会被后续不透明/半透明地形渲染覆盖导致不可见）。
 * - LINES 顶点格式带 LineWidth 属性（rendertype_lines.vsh），每顶点必须 setLineWidth。
 */
@Environment(EnvType.CLIENT)
public final class TreasureSenseRenderer {
    private static final List<BlockPos> containers = new ArrayList<>();
    private static long lastScanTime = 0L;
    private static final int SCAN_INTERVAL_MS = 1000;
    private static final double RANGE_SQ = 576.0;
    private static final double MINECART_INFLATE = 29.0;
    private static final int COLOR_TRAPPED = 0xFFFF3C34;
    private static final int COLOR_CHEST = 0xFFFFCF00;
    private static final String GOLD_GLOW_TEAM = "kaleidoscope_gold_glow";
    private static final float BOX_LINE_WIDTH = 2.0F;
    /** 单容器 12 边 × 2 顶点 × 顶点大小约 48B，按容器数动态分配（下限 4KB） */
    private static final int VERTEX_BYTES_PER_BOX = 12 * 2 * 48;

    /** LINES 管线克隆：深度测试恒过（穿墙）。register() 必须——Iris 只认注册表内管线 */
    private static final RenderPipeline TREASURE_LINES_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation("pipeline/kwl_treasure_lines")
                    // 26.1.2：深度/模板状态合并进 DepthStencilState（CompareOp.ALWAYS_PASS=深度恒过=穿墙，不写深度）
                    .withDepthStencilState(Optional.of(new DepthStencilState(CompareOp.ALWAYS_PASS, false)))
                    .build());

    private static BufferBuilder buffer;
    private static MappableRingBuffer vertexBuffer;
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private TreasureSenseRenderer() {
    }

    public static void register() {
        bindIrisShaderKey();
        net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(context -> {
            if (containers.isEmpty()) {
                return;
            }
            extractBoxes(context);
            drawBoxes(Minecraft.getInstance());
        });
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null) {
                containers.clear();
                return;
            }
            if (!mc.player.hasEffect(ModEffects.TREASURE_SENSE)) {
                // 效果结束后必须继续跑矿车清理：无效果分支会解除残留的金色发光
                // 与队伍（1.20.1 原版 onRenderTick 同样无条件调用），否则矿车
                // 轮廓会永远残留。
                containers.clear();
                tickMinecartGlow(mc);
                return;
            }
            long now = System.currentTimeMillis();
            if (now - lastScanTime > SCAN_INTERVAL_MS) {
                lastScanTime = now;
                scanForContainers(mc);
            }
            tickMinecartGlow(mc);
        });
    }

    /** 提取：把容器 AABB 的 12 条边写入 BufferBuilder（世界坐标-相机，与 poseStack 相机相对一致） */
    private static void extractBoxes(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = context.levelState().cameraRenderState.pos;
        PoseStack poseStack = context.poseStack();
        if (buffer == null) {
            int size = Math.max(containers.size() * VERTEX_BYTES_PER_BOX, 4096);
            buffer = new BufferBuilder(new ByteBufferBuilder(size),
                    TREASURE_LINES_PIPELINE.getVertexFormatMode(), TREASURE_LINES_PIPELINE.getVertexFormat());
        }
        Matrix4fc positionMatrix = poseStack.last().pose();
        for (BlockPos pos : containers) {
            int color = mc.level.getBlockEntity(pos) instanceof TrappedChestBlockEntity
                    ? COLOR_TRAPPED : COLOR_CHEST;
            float r = (color >> 16 & 0xFF) / 255.0F;
            float g = (color >> 8 & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            renderBoxEdges(positionMatrix, pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z, r, g, b);
        }
    }

    /** 画单位盒的 12 条边（LINES 模式，每顶点带 LineWidth 属性） */
    private static void renderBoxEdges(Matrix4fc m, double x, double y, double z, float r, float g, float b) {
        float x0 = (float) x, y0 = (float) y, z0 = (float) z;
        float x1 = x0 + 1.0F, y1 = y0 + 1.0F, z1 = z0 + 1.0F;
        edge(m, x0, y0, z0, x1, y0, z0, r, g, b);
        edge(m, x1, y0, z0, x1, y0, z1, r, g, b);
        edge(m, x1, y0, z1, x0, y0, z1, r, g, b);
        edge(m, x0, y0, z1, x0, y0, z0, r, g, b);
        edge(m, x0, y1, z0, x1, y1, z0, r, g, b);
        edge(m, x1, y1, z0, x1, y1, z1, r, g, b);
        edge(m, x1, y1, z1, x0, y1, z1, r, g, b);
        edge(m, x0, y1, z1, x0, y1, z0, r, g, b);
        edge(m, x0, y0, z0, x0, y1, z0, r, g, b);
        edge(m, x1, y0, z0, x1, y1, z0, r, g, b);
        edge(m, x1, y0, z1, x1, y1, z1, r, g, b);
        edge(m, x0, y0, z1, x0, y1, z1, r, g, b);
    }

    private static void edge(Matrix4fc m, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {
        // lines shader：Position+Normal 决定线段，Normal=边方向；LineWidth 是顶点属性
        float nx = x2 - x1, ny = y2 - y1, nz = z2 - z1;
        buffer.addVertex(m, x1, y1, z1).setColor(r, g, b, 1.0F).setNormal(nx, ny, nz).setLineWidth(BOX_LINE_WIDTH);
        buffer.addVertex(m, x2, y2, z2).setColor(r, g, b, 1.0F).setNormal(nx, ny, nz).setLineWidth(BOX_LINE_WIDTH);
    }

    /** 绘制：照官方文档——MeshData 上传 MappableRingBuffer，RenderPass 直画主目标 */
    private static void drawBoxes(Minecraft client) {
        MeshData builtBuffer = buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = upload(drawParameters, format, builtBuffer);
        draw(client, TREASURE_LINES_PIPELINE, builtBuffer, drawParameters, vertices, format);

        vertexBuffer.rotate();
        buffer = null;
    }

    private static GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();
        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }
            vertexBuffer = new MappableRingBuffer(() -> "kwl treasure lines", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }
        return vertexBuffer.currentBuffer();
    }

    private static void draw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format) {
        // LINES 模式：顺序索引缓冲（QUADS 的 sortQuads 分支不需要——管线固定为 LINES）
        RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
        GpuBuffer indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
        VertexFormat.IndexType indexType = shapeIndexBuffer.type();

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "kwl treasure lines", client.getMainRenderTarget().getColorTextureView(),
                        OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0, 0, drawParameters.indexCount(), 1);
        }
        builtBuffer.close();
    }

    /**
     * Iris 兼容：Iris 只给注册表内管线/已知 ShaderKey 绑定 shader（未知管线走 FAKE_FUNCTION 被跳过）。
     * 用 Iris 暴露的 IrisPipelines.assignPipeline(pipeline, ShaderKey.LINES) 把自定义穿墙管线
     * 显式绑定到 lines shader key（反射调用，无 Iris 时静默跳过）。
     */
    private static void bindIrisShaderKey() {
        try {
            Class<?> irisPipelines = Class.forName("net.irisshaders.iris.pipeline.IrisPipelines");
            Class<?> shaderKey = Class.forName("net.irisshaders.iris.pipeline.programs.ShaderKey");
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object lines = Enum.valueOf((Class<? extends Enum>) shaderKey, "LINES");
            irisPipelines.getMethod("assignPipeline", RenderPipeline.class, shaderKey)
                    .invoke(null, TREASURE_LINES_PIPELINE, lines);
        } catch (Exception ignored) {
            // 无 Iris：原版 RenderPipelines.register 即可渲染自定义管线
        }
    }

    /** GameRenderer.close 时释放 GPU 资源（照官方文档，由 mixin 调用） */
    public static void close() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }

    private static void scanForContainers(Minecraft mc) {
        containers.clear();
        BlockPos playerPos = mc.player.blockPosition();
        ChunkPos playerChunkPos = mc.player.chunkPosition();
        int chunkRadius = 2;
        for (int cx = playerChunkPos.x() - chunkRadius; cx <= playerChunkPos.x() + chunkRadius; cx++) {
            for (int cz = playerChunkPos.z() - chunkRadius; cz <= playerChunkPos.z() + chunkRadius; cz++) {
                LevelChunk chunk = mc.level.getChunk(cx, cz);
                if (chunk != null && !chunk.isEmpty()) {
                    for (var be : chunk.getBlockEntities().values()) {
                        if (!be.isRemoved() && !(be.getBlockPos().distSqr(playerPos) > RANGE_SQ)
                                && (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity)) {
                            containers.add(be.getBlockPos());
                        }
                    }
                }
            }
        }
    }

    /**
     * 运输矿车金色发光：1.20.1 原版在 RenderTick END 管理
     * kaleidoscope_gold_glow 队伍（GOLD 色、无碰撞、隐藏名牌）。
     */
    private static void tickMinecartGlow(Minecraft mc) {
        if (mc.isPaused()) {
            return;
        }
        boolean hasTreasureSense = mc.player.hasEffect(ModEffects.TREASURE_SENSE);
        List<MinecartChest> minecarts = mc.level.getEntitiesOfClass(MinecartChest.class,
                mc.player.getBoundingBox().inflate(MINECART_INFLATE), minecart -> !minecart.isRemoved());
        PlayerTeam goldTeam = mc.level.getScoreboard().getPlayerTeam(GOLD_GLOW_TEAM);
        if (goldTeam == null) {
            goldTeam = mc.level.getScoreboard().addPlayerTeam(GOLD_GLOW_TEAM);
            goldTeam.setColor(ChatFormatting.GOLD);
            goldTeam.setCollisionRule(Team.CollisionRule.NEVER);
            goldTeam.setNameTagVisibility(Team.Visibility.NEVER);
        }

        for (MinecartChest minecart : minecarts) {
            IGlowingEntity glowingMinecart = (IGlowingEntity) minecart;
            boolean shouldGlow = hasTreasureSense && mc.player.distanceToSqr(minecart) <= RANGE_SQ;
            if (glowingMinecart.isModGlowing() != shouldGlow) {
                if (shouldGlow) {
                    mc.level.getScoreboard().addPlayerToTeam(minecart.getStringUUID(), goldTeam);
                    glowingMinecart.setGlowing(true);
                } else {
                    glowingMinecart.setGlowing(false);
                    mc.level.getScoreboard().removePlayerFromTeam(minecart.getStringUUID(), goldTeam);
                }
            }
        }

        if (!hasTreasureSense) {
            for (MinecartChest minecart : minecarts) {
                IGlowingEntity glowingMinecart = (IGlowingEntity) minecart;
                if (glowingMinecart.isModGlowing()) {
                    glowingMinecart.setGlowing(false);
                    mc.level.getScoreboard().removePlayerFromTeam(minecart.getStringUUID(), goldTeam);
                }
            }
            if (goldTeam.getPlayers().isEmpty()) {
                mc.level.getScoreboard().removePlayerTeam(goldTeam);
            }
        }
    }
}
