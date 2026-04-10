package io.github.hawah.structure_crafter.client.render;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.structure_crafter.mixin.StructureTemplateAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
// 创建一个只读取 StructureTemplate 内部方块的伪装接口
public class StructureBlockGetter extends Level implements BlockAndTintGetter {
    private final Map<BlockPos, BlockState> blockMap = new HashMap<>();
    private final Level realLevel;

    public StructureBlockGetter(StructureTemplate template, BlockPos origin, Level realLevel) {
        super(
                (WritableLevelData) realLevel.getLevelData(),
                realLevel.dimension(),
                realLevel.registryAccess(),
                realLevel.dimensionTypeRegistration(),
                realLevel.isClientSide(),
                realLevel.isDebug(),
                0,
                0
        );
        this.realLevel = realLevel;
        // 初始化时把结构里的方块全存入 Map，方便快速查找
        var blockInfos = ((StructureTemplateAccessor) template).getPalettes().getFirst().blocks();
        for (var info : blockInfos) {
            blockMap.put(origin.offset(info.pos()), info.state());
        }
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
    public void playSeededSound(@org.jspecify.annotations.Nullable Entity entity, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {
    }

    @Override
    public void playSeededSound(@org.jspecify.annotations.Nullable Entity entity, Entity sourceEntity, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {
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
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {

    }

    @Override
    public Scoreboard getScoreboard() {
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

        return realLevel.getBrightness(lightLayer, p_45555_);
    }
    @Override public int getRawBrightness(BlockPos p_45558_, int p_45559_) { return realLevel.getRawBrightness(p_45558_, p_45559_); }
    @Override public int getHeight() { return realLevel.getHeight(); }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return realLevel.enabledFeatures();
    }


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
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Override
    public void explode(@org.jspecify.annotations.Nullable Entity source, @org.jspecify.annotations.Nullable DamageSource damageSource, @org.jspecify.annotations.Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, ExplosionInteraction explosionInteraction, ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles, WeightedList<ExplosionParticleInfo> blockParticles, Holder<SoundEvent> explosionSound) {
    }

    @Override
    public void setRespawnData(LevelData.RespawnData respawnData) {
    }

    @Override
    public LevelData.RespawnData getRespawnData() {
        return realLevel.getRespawnData();
    }

    @Override
    public Collection<? extends PartEntity<?>> dragonParts() {
        return List.of();
    }

    @Override
    public RecipeAccess recipeAccess() {
        return realLevel.recipeAccess();
    }

    @Override
    public EnvironmentAttributeSystem environmentAttributes() {
        return realLevel.environmentAttributes();
    }

    @Override
    public FuelValues fuelValues() {
        return realLevel.fuelValues();
    }

    @Override
    public void levelEvent(@org.jspecify.annotations.Nullable Entity entity, int type, BlockPos pos, int data) {

    }

    @Override
    public WorldBorder getWorldBorder() {
        return realLevel.getWorldBorder();
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }
}