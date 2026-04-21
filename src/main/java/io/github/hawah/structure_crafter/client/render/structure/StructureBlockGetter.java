package io.github.hawah.structure_crafter.client.render.structure;

import io.github.hawah.structure_crafter.mixin.StructureTemplateAccessor;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
// 创建一个只读取 StructureTemplate 内部方块的伪装接口
public class StructureBlockGetter extends Level implements BlockAndTintGetter {
    private final Map<BlockPos, BlockState> blockMap = new HashMap<>();
    private final Level realLevel;
    private final BoundingBox bounds;
    private BlockPos offset = BlockPos.ZERO;
    private Rotation rotation = Rotation.NONE;

    public StructureBlockGetter(StructureTemplate template, Level realLevel) {
        super(
                (WritableLevelData) realLevel.getLevelData(),
                realLevel.dimension(),
                realLevel.registryAccess(),
                realLevel.dimensionTypeRegistration(),
                realLevel.getProfilerSupplier(),
                realLevel.isClientSide(),
                realLevel.isDebug(),
                0,
                0
        );
        this.realLevel = realLevel;
        // 初始化时把结构里的方块全存入 Map，方便快速查找
        var blockInfos = ((StructureTemplateAccessor) template).getPalettes().getFirst().blocks();
        if (blockInfos.isEmpty()) {
            bounds = new BoundingBox(BlockPos.ZERO);
            return;
        }
        bounds = new BoundingBox(blockInfos.getFirst().pos());
        for (var info : blockInfos) {
            blockMap.put(info.pos(), info.state());
            //noinspection deprecation
            bounds.encapsulate(info.pos());
        }
    }

    public void setOffset(BlockPos offset) {
        this.offset = offset;
        // get -> pos.sub(offset)
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        // 如果结构里有这个方块，返回结构里的；否则返回空气
        return blockMap.getOrDefault(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public void playSeededSound(@Nullable Player p_262953_, double p_263004_, double p_263398_, double p_263376_, Holder<SoundEvent> p_263359_, SoundSource p_263020_, float p_263055_, float p_262914_, long p_262991_) {

    }

    @Override
    public void playSeededSound(@Nullable Player p_220372_, Entity p_220373_, Holder<SoundEvent> p_263500_, SoundSource p_220375_, float p_220376_, float p_220377_, long p_220378_) {

    }

    @Override
    public String gatherChunkSourceStats() {
        return "";
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return realLevel.getShade(direction, shade);
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

    }

    @Override
    public LevelLightEngine getLightEngine() {
        return realLevel.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
        return realLevel.getBlockTint(pos, colorResolver);
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return null;
    }

    // ... 其他需要实现的方法统一 fallback 到 realLevel 或返回默认值 (如 getBlockEntity 返回 null) ...
    @Nullable @Override public BlockEntity getBlockEntity(BlockPos p_45570_) { return null; }

    @Override
    public @Nullable Entity getEntity(int id) {
        return null;
    }

    @Override
    public TickRateManager tickRateManager() {
        return realLevel.tickRateManager();
    }

    @Override
    public @Nullable MapItemSavedData getMapData(MapId mapId) {
        return null;
    }

    @Override
    public ModelData getModelData(BlockPos pos) {
        return super.getModelData(pos);
    }

    @Override
    public void setMapData(MapId p_324009_, MapItemSavedData p_151534_) {

    }

    @Override
    public MapId getFreeMapId() {
        return null;
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public RecipeManager getRecipeManager() {
        return null;
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return null;
    }

    @Override
    public PotionBrewing potionBrewing() {
        return null;
    }

    @Override
    public void setDayTimeFraction(float dayTimeFraction) {

    }

    @Override
    public float getDayTimeFraction() {
        return 0;
    }

    @Override
    public float getDayTimePerTick() {
        return 0;
    }

    @Override
    public void setDayTimePerTick(float dayTimePerTick) {

    }

    @Override public int getBrightness(LightLayer lightLayer, BlockPos p_45555_) {

        if (lightLayer.equals(LightLayer.SKY)) {
            return 15;
        }

        return realLevel.getBrightness(lightLayer, p_45555_.rotate(rotation).offset(offset));
    }
    @Override public int getRawBrightness(BlockPos p_45558_, int p_45559_) { return realLevel.getRawBrightness(p_45558_, p_45559_); }
    @Override public int getHeight() { return realLevel.getHeight(); }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return realLevel.enabledFeatures();
    }

    @Override public int getMinBuildHeight() { return realLevel.getMinBuildHeight(); }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return realLevel.getBlockTicks();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return realLevel.getFluidTicks();
    }

    @Override
    public ChunkSource getChunkSource() {
        return realLevel.getChunkSource();
    }

    @Override
    public void levelEvent(@Nullable Player p_46771_, int p_46772_, BlockPos p_46773_, int p_46774_) {
    }

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    public void setRot(Rotation rotation) {
        this.rotation = rotation;
    }
}