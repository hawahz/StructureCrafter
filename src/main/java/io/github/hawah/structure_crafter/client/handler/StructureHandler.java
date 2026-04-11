package io.github.hawah.structure_crafter.client.handler;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.hawah.structure_crafter.Config;
import io.github.hawah.structure_crafter.Paths;
import io.github.hawah.structure_crafter.mixin.StructureTemplateAccessor;
import io.github.hawah.structure_crafter.util.StructurePlaceMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StructureHandler {
    public static void loadStructures(List<Component> allStructures) {
        allStructures.clear();
        try (Stream<Path> paths = Files.list(Paths.STRUCTURE_DIR)) {
            paths.filter(f -> !Files.isDirectory(f) && f.getFileName().toString().endsWith(".nbt"))
                    .forEach(path -> {
                        if (Files.isDirectory(path))
                            return;

                        allStructures.add(Component.literal(path.getFileName().toString()));
                    });
        } catch (NoSuchFileException ignored) {
            // No Schematics created yet
        } catch (IOException ignored) {
        }
    }

    public static void loadStructuresString(List<String> allStructures) {
        allStructures.clear();
        try (Stream<Path> paths = Files.list(Paths.STRUCTURE_DIR)) {
            paths.filter(f -> !Files.isDirectory(f) && f.getFileName().toString().endsWith(".nbt"))
                    .forEach(path -> {
                        if (Files.isDirectory(path))
                            return;

                        allStructures.add(path.getFileName().toString());
                    });
        } catch (NoSuchFileException ignored) {
            // No Schematics created yet
        } catch (IOException ignored) {
        }
    }

    public static @NotNull HashMap<Item, Integer> getNeededItems(List<StructureTemplate.StructureBlockInfo> blockInfos) {
        return getNeededItems(blockInfos, List.of(), null);
    }

    public static @NotNull HashMap<Item, Integer> getNeededItems(
            List<StructureTemplate.StructureBlockInfo> blockInfos,
            List<StructureTemplate.StructureEntityInfo> entityInfos,
            ServerLevelAccessor accessor
    ) {
        HashMap<Item, Integer> consumes = new HashMap<>();
        for (StructureTemplate.StructureBlockInfo info : blockInfos) {
            BlockState state = info.state();
            Block block = state.getBlock();
            if (BedPart.FOOT.equals(state.getOptionalValue(BlockStateProperties.BED_PART).orElse(BedPart.HEAD))) {
                continue;
            } else if (DoubleBlockHalf.UPPER.equals(state.getOptionalValue(BlockStateProperties.DOUBLE_BLOCK_HALF).orElse(DoubleBlockHalf.LOWER))) {
                continue;
            }

            ItemStack itemStack = new ItemStack(block);
            if (itemStack.isEmpty()) {
                continue;
            }
            int counts = state.getOptionalValue(BlockStateProperties.CANDLES).orElse(
                    state.getOptionalValue(BlockStateProperties.PICKLES).orElse(1)
            );
            Item item = itemStack.getItem();
            if (consumes.containsKey(item)) {
                consumes.put(item, consumes.get(item) + counts);
            } else {
                consumes.put(item, counts);
            }
        }

        if (accessor == null)
            return consumes;

        for (StructureTemplate.StructureEntityInfo entityInfo : entityInfos) {
            Optional<Entity> entity = createEntityIgnoreException(accessor, entityInfo.nbt);
            if (entity.isEmpty()) {
                continue;
            }
            ItemStack pickResult = entity.get().getPickResult();
            if (pickResult == null) {
                entity.get().setRemoved(Entity.RemovalReason.DISCARDED);
                continue;
            }
            consumes.put(pickResult.getItem(), pickResult.getCount());

            if (entity.get() instanceof ContainerEntity containerEntity) {
                NonNullList<ItemStack> itemStacks = containerEntity.getItemStacks();
                consumes.putAll(itemStacks.stream().collect(Collectors.toMap(ItemStack::getItem, ItemStack::getCount)));
            }
            entity.get().setRemoved(Entity.RemovalReason.DISCARDED);
        }
        return consumes;
    }

    public static @NotNull NonNullList<ItemStack> getInventoryItems(Player player) {
        NonNullList<ItemStack> items = NonNullList.create();
        items.addAll(player.getInventory().items);
        items.addAll(player.getInventory().armor);
        items.addAll(player.getInventory().offhand);
        return items;
    }

    public static void placeInWorld(
            StructureTemplate template, ServerLevelAccessor serverLevel, BlockPos offset, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags
    ) {
        if (( (StructureTemplateAccessor) template).getPalettes().isEmpty()) {
        } else {
            List<StructureTemplate.StructureBlockInfo> list = settings.getRandomPalette(( (StructureTemplateAccessor) template).getPalettes(), offset).blocks();
            if ((!list.isEmpty() || !settings.isIgnoreEntities() && !( (StructureTemplateAccessor) template).getEntityInfoList().isEmpty())
                    && template.getSize().getX() >= 1
                    && template.getSize().getY() >= 1
                    && template.getSize().getZ() >= 1) {
                BoundingBox boundingbox = settings.getBoundingBox();
                List<BlockPos> list1 = Lists.newArrayListWithCapacity(settings.shouldApplyWaterlogging() ? list.size() : 0);
                List<BlockPos> list2 = Lists.newArrayListWithCapacity(settings.shouldApplyWaterlogging() ? list.size() : 0);
                List<Pair<BlockPos, CompoundTag>> list3 = Lists.newArrayListWithCapacity(list.size());
                int i = Integer.MAX_VALUE;
                int j = Integer.MAX_VALUE;
                int k = Integer.MAX_VALUE;
                int l = Integer.MIN_VALUE;
                int i1 = Integer.MIN_VALUE;
                int j1 = Integer.MIN_VALUE;

                for (StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : StructureTemplate.processBlockInfos(
                        serverLevel, offset, pos, settings, list, template
                )) {
                    BlockPos blockpos = structuretemplate$structureblockinfo.pos();
                    if (boundingbox == null || boundingbox.isInside(blockpos)) {
                        FluidState fluidstate = settings.shouldApplyWaterlogging() ? serverLevel.getFluidState(blockpos) : null;
                        BlockState blockstate = structuretemplate$structureblockinfo.state().mirror(settings.getMirror()).rotate(settings.getRotation());
                        boolean enableNbt = blockChecker(blockstate);
                        if (structuretemplate$structureblockinfo.nbt() != null && enableNbt) {
                            BlockEntity blockentity = serverLevel.getBlockEntity(blockpos);
                            Clearable.tryClear(blockentity);
                            serverLevel.setBlock(blockpos, Blocks.BARRIER.defaultBlockState(), 20);
                        }

                        if (serverLevel.setBlock(blockpos, blockstate, flags)) {
                            i = Math.min(i, blockpos.getX());
                            j = Math.min(j, blockpos.getY());
                            k = Math.min(k, blockpos.getZ());
                            l = Math.max(l, blockpos.getX());
                            i1 = Math.max(i1, blockpos.getY());
                            j1 = Math.max(j1, blockpos.getZ());
                            list3.add(Pair.of(blockpos, enableNbt? structuretemplate$structureblockinfo.nbt() : null));
                            if (structuretemplate$structureblockinfo.nbt() != null && enableNbt) {
                                BlockEntity blockentity1 = serverLevel.getBlockEntity(blockpos);
                                if (blockentity1 != null) {
                                    if (blockentity1 instanceof RandomizableContainer) {
                                        structuretemplate$structureblockinfo.nbt().putLong("LootTableSeed", random.nextLong());
                                    }

                                    blockentity1.loadWithComponents(structuretemplate$structureblockinfo.nbt(), serverLevel.registryAccess());
                                }
                            }

                            if (fluidstate != null) {
                                if (blockstate.getFluidState().isSource()) {
                                    list2.add(blockpos);
                                } else if (blockstate.getBlock() instanceof LiquidBlockContainer) {
                                    ((LiquidBlockContainer)blockstate.getBlock()).placeLiquid(serverLevel, blockpos, blockstate, fluidstate);
                                    if (!fluidstate.isSource()) {
                                        list1.add(blockpos);
                                    }
                                }
                            }
                        }
                    }
                }

                boolean flag = true;
                Direction[] adirection = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

                while (flag && !list1.isEmpty()) {
                    flag = false;
                    Iterator<BlockPos> iterator = list1.iterator();

                    while (iterator.hasNext()) {
                        BlockPos blockpos3 = iterator.next();
                        FluidState fluidstate2 = serverLevel.getFluidState(blockpos3);

                        for (int i2 = 0; i2 < adirection.length && !fluidstate2.isSource(); i2++) {
                            BlockPos blockpos1 = blockpos3.relative(adirection[i2]);
                            FluidState fluidstate1 = serverLevel.getFluidState(blockpos1);
                            if (fluidstate1.isSource() && !list2.contains(blockpos1)) {
                                fluidstate2 = fluidstate1;
                            }
                        }

                        if (fluidstate2.isSource()) {
                            BlockState blockstate1 = serverLevel.getBlockState(blockpos3);
                            Block block = blockstate1.getBlock();
                            if (block instanceof LiquidBlockContainer) {
                                ((LiquidBlockContainer)block).placeLiquid(serverLevel, blockpos3, blockstate1, fluidstate2);
                                flag = true;
                                iterator.remove();
                            }
                        }
                    }
                }

                if (i <= l) {
                    if (!settings.getKnownShape()) {
                        DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(l - i + 1, i1 - j + 1, j1 - k + 1);
                        int k1 = i;
                        int l1 = j;
                        int j2 = k;

                        for (Pair<BlockPos, CompoundTag> pair1 : list3) {
                            BlockPos blockpos2 = pair1.getFirst();
                            discretevoxelshape.fill(blockpos2.getX() - k1, blockpos2.getY() - l1, blockpos2.getZ() - j2);
                        }

                        StructureTemplate.updateShapeAtEdge(serverLevel, flags, discretevoxelshape, k1, l1, j2);
                    }

                    for (Pair<BlockPos, CompoundTag> pair : list3) {
                        BlockPos blockpos4 = pair.getFirst();
                        if (!settings.getKnownShape()) {
                            BlockState blockstate2 = serverLevel.getBlockState(blockpos4);
                            BlockState blockstate3 = Block.updateFromNeighbourShapes(blockstate2, serverLevel, blockpos4);
                            if (blockstate2 != blockstate3) {
                                serverLevel.setBlock(blockpos4, blockstate3, flags & -2 | 16);
                            }

                            serverLevel.blockUpdated(blockpos4, blockstate3.getBlock());
                        }

                        if (pair.getSecond() != null) {
                            BlockEntity blockentity2 = serverLevel.getBlockEntity(blockpos4);
                            if (blockentity2 != null) {
                                blockentity2.setChanged();
                            }
                        }
                    }
                }

                if (!settings.isIgnoreEntities()) {
                    addEntitiesToWorld(template, serverLevel, offset, settings);
                }

            } else {
            }
        }
    }

    private static void addEntitiesToWorld(StructureTemplate template, ServerLevelAccessor serverLevelAccessor, BlockPos pos, StructurePlaceSettings placementIn) {
        for(StructureTemplate.StructureEntityInfo structuretemplate$structureentityinfo : StructureTemplate.processEntityInfos(template, serverLevelAccessor, pos, placementIn, ((StructureTemplateAccessor)template).getEntityInfoList())) {
            BlockPos blockpos = structuretemplate$structureentityinfo.blockPos; // FORGE: Position will have already been transformed by processEntityInfos
            if (placementIn.getBoundingBox() == null || placementIn.getBoundingBox().isInside(blockpos)) {
                CompoundTag compoundtag = structuretemplate$structureentityinfo.nbt.copy();
                Vec3 vec31 = structuretemplate$structureentityinfo.pos; // FORGE: Position will have already been transformed by processEntityInfos
                ListTag listtag = new ListTag();

                listtag.add(DoubleTag.valueOf(vec31.x));
                listtag.add(DoubleTag.valueOf(vec31.y));
                listtag.add(DoubleTag.valueOf(vec31.z));
                compoundtag.put("Pos", listtag);
                compoundtag.remove("UUID");
                createEntityIgnoreException(serverLevelAccessor, compoundtag).ifPresent(entity -> {
                    if (entity instanceof LivingEntity)
                        return;
                    float f = entity.rotate(placementIn.getRotation());
                    f += entity.mirror(placementIn.getMirror()) - entity.getYRot();
                    entity.moveTo(vec31.x, vec31.y, vec31.z, f, entity.getXRot());

                    serverLevelAccessor.addFreshEntityWithPassengers(entity);
                });
            }
        }
    }

    private static Optional<Entity> createEntityIgnoreException(ServerLevelAccessor level, CompoundTag tag) {
        try {
            return EntityType.create(tag, level.getLevel());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private static boolean blockChecker(BlockState state) {
        return switch (Config.CommonConfig.STRUCTURE_PLACE_MODE.get()) {
            case ALL -> true;
            case BLACKLIST -> {
                String descriptionId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                List<? extends String> list = Config.CommonConfig.STRUCTURE_BLACKLIST.get();
                yield !list.contains(descriptionId);
            }
            default -> false;
        };
    }

    public static ListTag newIntegerList(int... pValues) {
        ListTag listtag = new ListTag();
        for (int i : pValues)
            listtag.add(IntTag.valueOf(i));
        return listtag;
    }

    public static ListTag posTag(BlockPos pos) {
        return newIntegerList(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos parsePos(ListTag tag) {
        return new BlockPos(tag.getInt(0), tag.getInt(1), tag.getInt(2));
    }
}
