package io.github.hawah.structure_crafter.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

public class CompressedTag {

    public static final StreamCodec<ByteBuf, CompressedTag> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, CompressedTag::tag,
            ByteBufCodecs.VAR_INT, CompressedTag::index,
            ByteBufCodecs.VAR_INT, CompressedTag::length,
            ByteBufCodecs.VAR_LONG, CompressedTag::id,
            CompressedTag::new
    );

    private long id() {
        return id;
    }

    private int length() {
        return length;
    }

    public int index() {
        return index;
    }

    private CompoundTag tag() {
        return tag;
    }

    public CompoundTag tag;
    public int index;
    public int length;
    public long id;

    public CompressedTag(CompoundTag tag, int index, int length, long id) {
        this.tag = tag;
        this.index = index;
        this.length = length;
        this.id = id;
    }

    public CompressedTag setLength(int length) {
        this.length = length;
        return this;
    }

    public static List<CompressedTag> split(CompoundTag tag, boolean force) {
        if (tag.sizeInBytes() < 2_000_000 && !force) {
            return List.of(new CompressedTag(tag, 0, tag.sizeInBytes(), 0));
        }

        ListTag blocks = tag.getList("blocks", Tag.TAG_COMPOUND);

        List<CompressedTag> compressedTags = new ArrayList<>();

        int compressLength = tag.sizeInBytes() / 1_000_000 + 1;

        CompoundTag first = new CompoundTag();
        for (String allKey : tag.getAllKeys()) {
            Tag t;
            if (allKey.equals("blocks") || (t = tag.get(allKey)) == null)
                continue;
            first.put(allKey, t);
        }

        if (compressLength == 1) {
            first.put("blocks", blocks);
            compressedTags.add(new CompressedTag(first, 0, 1, 0));
            return compressedTags;
        }

        long id = new Random().nextLong();

        CompoundTag dynamic = new CompoundTag();

        ListTag blockList = new ListTag();

        compressedTags.add(new CompressedTag(first, 0, -1, id));

        for (int i = 0; i < blocks.size(); i++) {
            blockList.add(blocks.getCompound(i));
            if (blockList.sizeInBytes() > 1_000_000) {
                dynamic.put("blocks", blockList);
                compressedTags.add(new CompressedTag(dynamic, compressedTags.size(), -1, id));
                blockList = new ListTag();
                dynamic = new CompoundTag();
            }
        }
        if (!blockList.isEmpty()) {
            dynamic.put("blocks", blockList);
            compressedTags.add(new CompressedTag(dynamic, compressedTags.size(), -1, id));
        }

        return compressedTags.stream().map(c -> c.setLength(compressedTags.size())).toList();
    }

}
