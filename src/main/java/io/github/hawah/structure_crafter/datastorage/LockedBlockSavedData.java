package io.github.hawah.structure_crafter.datastorage;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber
public class LockedBlockSavedData extends SavedData {

    public Set<BlockPos> lockedBlocks = new HashSet<>();
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag lockedBlocksTag = new ListTag();
        for (BlockPos pos : lockedBlocks) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt("x", pos.getX());
            compoundTag.putInt("y", pos.getY());
            compoundTag.putInt("z", pos.getZ());
            lockedBlocksTag.add(compoundTag);
        }
        tag.put("Content", lockedBlocksTag);
        return tag;
    }

    public static LockedBlockSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        LockedBlockSavedData data = new LockedBlockSavedData();
        ListTag lockedBlocksTag = tag.getList("Content", ListTag.TAG_COMPOUND);
        for (int i = 0; i < lockedBlocksTag.size(); i++) {
            CompoundTag compoundTag = lockedBlocksTag.getCompound(i);
            int x = compoundTag.getInt("x");
            int y = compoundTag.getInt("y");
            int z = compoundTag.getInt("z");
            data.lockedBlocks.add(new BlockPos(x, y, z));
        }
        return data;
    }

    public void add(BlockPos pos) {
        lockedBlocks.add(pos);
        setDirty();
    }

    public static LockedBlockSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                LockedBlockSavedData::new,
                LockedBlockSavedData::load
        ), "locked_block");
    }

    public void remove(BlockPos pos) {
        lockedBlocks.remove(pos);
        setDirty();
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            getOrCreate(serverLevel);
        }
    }
}
