package io.github.hawah.structure_crafter.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class NBTHelper {
    public static class BlockEntity {
        public static CompoundTag cleanTag(CompoundTag tag, HolderLookup.Provider registries) {
            if (tag == null) return null;

            CompoundTag cleaned = tag.copy();

            cleaned.remove("x");
            cleaned.remove("y");
            cleaned.remove("z");
            cleaned.remove("id");
            cleaned.remove("keepPacked");

            cleaned.remove("Command");
            cleaned.remove("auto");
            cleaned.remove("powered");
            cleaned.remove("conditionMet");
            cleaned.remove("SuccessCount");
            cleaned.remove("LastExecution");

            cleaned.remove("mode");
            cleaned.remove("structureName");
            cleaned.remove("name");
            cleaned.remove("metadata");
            cleaned.remove("pool");
            cleaned.remove("target");

            cleaned.remove("LootTable");
            cleaned.remove("LootTableSeed");

            cleaned.remove("Items");

            sanitizeText(cleaned, "CustomName", registries);

            if (cleaned.contains("front_text", Tag.TAG_COMPOUND)) {
                sanitizeSignText(cleaned.getCompound("front_text"), registries);
            }
            if (cleaned.contains("back_text", Tag.TAG_COMPOUND)) {
                sanitizeSignText(cleaned.getCompound("back_text"), registries);
            }

            for (int i = 1; i <= 4; i++) {
                sanitizeText(cleaned, "Text" + i, registries);
            }

            if (cleaned.contains("SpawnData", Tag.TAG_COMPOUND)) {
                CompoundTag spawn = cleaned.getCompound("SpawnData");
                if (spawn.contains("entity", Tag.TAG_COMPOUND)) {
                    CompoundTag entity = spawn.getCompound("entity");
                    if (entity.contains("id")) {
                        String id = entity.getString("id");
                        // 简单白名单（你可以扩展）
                        if (!isSafeEntity(id)) {
                            cleaned.remove("SpawnData");
                        }
                    }
                }
            }

            if (!cleanNested(cleaned, 0)) {
                return new CompoundTag();
            }

            return cleaned;
        }

        private static void sanitizeText(CompoundTag tag, String key, HolderLookup.Provider registries) {
            if (!tag.contains(key, Tag.TAG_STRING)) return;

            String json = tag.getString(key);

            try {
                Component.Serializer.fromJson(json, registries);
            } catch (Exception e) {
                tag.remove(key); // 非法直接删
            }
        }

        private static void sanitizeSignText(CompoundTag textTag, HolderLookup.Provider registries) {
            if (!textTag.contains("messages", Tag.TAG_LIST)) return;

            ListTag list = textTag.getList("messages", Tag.TAG_STRING);

            for (int i = 0; i < list.size(); i++) {
                String json = list.getString(i);

                try {
                    MutableComponent mutableComponent = Component.Serializer.fromJson(json, registries);
                    ClickEvent clickEvent = mutableComponent.getStyle().getClickEvent();
                    if (clickEvent != null) {
                        mutableComponent.setStyle(mutableComponent.getStyle().withClickEvent(null));
                        list.set(i, StringTag.valueOf(
                                Component.Serializer.toJson(mutableComponent, registries)
                        ));
                    }
                } catch (Exception e) {
                    list.set(i, StringTag.valueOf("{\"text\":\"\"}"));
                }
            }
        }
        private static boolean isSafeEntity(String id) {
            return id.startsWith("minecraft:"); // 最基础策略
        }

        private static boolean cleanNested(Tag tag, int times) {
            if (times >= 10) {
                return false;
            }
            boolean returnable = true;
            if (tag instanceof CompoundTag compound) {
                for (String key : compound.getAllKeys()) {
                    Tag child = compound.get(key);

                    if (key.equals("clickEvent")) {
                        compound.remove(key);
                        continue;
                    }

                    returnable &= cleanNested(child, times + 1);
                }
            } else if (tag instanceof ListTag list) {
                for (Tag child : list) {
                    returnable &= cleanNested(child, times + 1);
                }
            }
            return returnable;
        }
    }
}
