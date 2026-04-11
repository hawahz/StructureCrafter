package io.github.hawah.structure_crafter.networking.structure_sync;

import io.github.hawah.structure_crafter.util.CompressedTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ServerCompressedTagReceiver {
    public static Map<Long, List<CompressedTag>> compressedTags = new HashMap<>();

    public static Optional<CompoundTag> receive(CompressedTag tag) {
        compressedTags.merge(tag.id, new ArrayList<>(List.of(tag)), (a, b) -> {
            a.addAll(b);
            a.sort(Comparator.comparing(CompressedTag::index));
            return a;
        });
        List<CompressedTag> currentFocus = compressedTags.get(tag.id);
        if (currentFocus.size() < tag.length) {
            return Optional.empty();
        }
        if (currentFocus.size() > tag.length) {
            throw new IllegalStateException("Received more than expected");
        }
        return Optional.of(parse(currentFocus));
    }

    private static @NotNull CompoundTag parse(List<CompressedTag> currentFocus) {
        CompoundTag result = new CompoundTag();
        var first = currentFocus.getFirst().tag;
        ListTag mergedBlocks = new ListTag();
        for (String allKey : first.getAllKeys()) {
            Tag tag = first.get(allKey);
            if (allKey.equals("blocks") || tag == null)
                continue;
            result.put(allKey, tag);
        }
        for (int i = 1; i < currentFocus.size(); i++) {
            var fragment = currentFocus.get(i).tag;
            ListTag blocks = fragment.getList("blocks", Tag.TAG_COMPOUND);
            mergedBlocks.addAll(blocks);
        }
        result.put("blocks", mergedBlocks);
        return result;
    }
}
