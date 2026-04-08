package io.github.hawah.structure_crafter.client.handler;

import io.github.hawah.structure_crafter.Paths;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
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
        return consumes;
    }

    public static @NotNull NonNullList<ItemStack> getInventoryItems(Player player) {
        NonNullList<ItemStack> items = NonNullList.create();
        items.addAll(player.getInventory().getNonEquipmentItems());
//        items.addAll(player.getInventory().armor);
//        items.addAll(player.getInventory().offhand);
        return items;
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
        return new BlockPos(tag.getInt(0).orElseThrow(), tag.getInt(1).orElseThrow(), tag.getInt(2).orElseThrow());
    }
}
