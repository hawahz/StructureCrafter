package io.github.hawah.structure_crafter.util;

import net.minecraft.nbt.CompoundTag;

import java.util.List;

public class CompressedTag {

    public CompoundTag tag;
    public int index;
    public int size;
    public long id;

    public CompressedTag(CompoundTag tag, int index, int size, long id) {
        this.tag = tag;
        this.index = index;
        this.size = size;
        this.id = id;
    }

    public static List<CompressedTag> compress(CompoundTag tag, boolean force) {
        if (tag.sizeInBytes() < 2_000_000 && !force) {
            return List.of(new CompressedTag(tag, 0, tag.sizeInBytes(), 0));
        }

        return List.of();
    }



}
