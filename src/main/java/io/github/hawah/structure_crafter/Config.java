package io.github.hawah.structure_crafter;

import io.github.hawah.structure_crafter.datagen.lang.LangData;
import io.github.hawah.structure_crafter.util.BlackboardRenderType;
import io.github.hawah.structure_crafter.util.StarPattern;
import io.github.hawah.structure_crafter.util.StructurePlaceMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

@EventBusSubscriber(modid = StructureCrafter.MODID)
public class Config {
    public static class ClientConfig {
        public static final ModConfigSpec SPEC;

        public static final ModConfigSpec.EnumValue<BlackboardRenderType> BLACKBOARD_ANIMATION_TYPE;
        public static final ModConfigSpec.BooleanValue RENDER_LOW_COST;
        public static final ModConfigSpec.BooleanValue RENDER_TELEPHONE_BOOST_POSITION;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            BLACKBOARD_ANIMATION_TYPE = builder
                    .comment(LangData.CONFIGURATION_BLACKBOARD_ANIMATION_TYPE.def)
                    .translation(LangData.CONFIGURATION_BLACKBOARD_ANIMATION_TYPE.key)
                    .defineEnum("blackboard_animation_type", BlackboardRenderType.WRITE);

            RENDER_LOW_COST = builder
                    .comment(LangData.CONFIGURATION_RENDER_LOW_COST.def)
                    .translation(LangData.CONFIGURATION_RENDER_LOW_COST.key)
                    .define("render_low_cost", false);

            RENDER_TELEPHONE_BOOST_POSITION = builder
                    .comment(LangData.CONFIGURATION_RENDER_TELEPHONE_BOOST_POSITION.def)
                    .translation(LangData.CONFIGURATION_RENDER_TELEPHONE_BOOST_POSITION.key)
                    .define("render_telephone_boost_position", true);

            SPEC = builder.build();
        }
    }

    public static class CommonConfig {
        public static final ModConfigSpec SPEC;

        public static final ModConfigSpec.IntValue STRUCTURE_PLACE_DISTANCE;
        public static final ModConfigSpec.IntValue PREVIEW_UNLOCK_DISTANCE;
        public static final ModConfigSpec.BooleanValue MATERIAL_LIST_SCATTERED_ENABLED;
        public static final ModConfigSpec.IntValue MAX_SIZE_X;
        public static final ModConfigSpec.IntValue MAX_SIZE_Y;
        public static final ModConfigSpec.IntValue MAX_SIZE_Z;
        public static final ModConfigSpec.IntValue MAX_VOLUME;
        public static final ModConfigSpec.EnumValue<StructurePlaceMode> STRUCTURE_PLACE_MODE;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> STRUCTURE_BLACKLIST;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            builder.push("General");
            builder.push("Blackboard Record Size Limit");

            MAX_SIZE_X = builder
                    .comment(LangData.CONFIGURATION_BLACKBOARD_RECORD_SIZE_LIMIT_X.def)
                    .translation(LangData.CONFIGURATION_BLACKBOARD_RECORD_SIZE_LIMIT_X.key)
                    .defineInRange("max_size_x", 30, -1, Integer.MAX_VALUE);
            MAX_SIZE_Y = builder
                    .comment(LangData.CONFIGURATION_BLACKBOARD_RECORD_SIZE_LIMIT_Y.def)
                    .translation(LangData.CONFIGURATION_BLACKBOARD_RECORD_SIZE_LIMIT_Y.key)
                    .defineInRange("max_size_y", 60, -1, Integer.MAX_VALUE);
            MAX_SIZE_Z = builder
                    .comment(LangData.CONFIGURATION_BLACKBOARD_RECORD_SIZE_LIMIT_Z.def)
                    .translation(LangData.CONFIGURATION_BLACKBOARD_RECORD_SIZE_LIMIT_Z.key)
                    .defineInRange("max_size_z", 30, -1, Integer.MAX_VALUE);
            MAX_VOLUME = builder
                    .comment(LangData.CONFIGURATION_BLACKBOARD_RECORD_SIZE_LIMIT_VOLUME.def)
                    .translation(LangData.CONFIGURATION_BLACKBOARD_RECORD_SIZE_LIMIT_VOLUME.key)
                    .defineInRange("max_volume", 100000, -1, Integer.MAX_VALUE);

            builder.pop();

            STRUCTURE_PLACE_DISTANCE = builder
                    .comment(LangData.CONFIGURATION_STRUCTURE_PLACE_DISTANCE.def)
                    .translation(LangData.CONFIGURATION_STRUCTURE_PLACE_DISTANCE.key)
                    .defineInRange("structure_place_distance", 1, 1, 10);

            PREVIEW_UNLOCK_DISTANCE = builder
                    .comment(LangData.CONFIGURATION_PREVIEW_UNLOCK_DISTANCE.def)
                    .translation(LangData.CONFIGURATION_PREVIEW_UNLOCK_DISTANCE.key)
                    .defineInRange("preview_unlock_distance", 300, 1, Integer.MAX_VALUE);

            STRUCTURE_PLACE_MODE = builder
                    .comment(LangData.CONFIGURATION_STRUCTURE_PLACE_MODE.def)
                    .translation(LangData.CONFIGURATION_STRUCTURE_PLACE_MODE.key)
                    .defineEnum("structure_place_mode", StructurePlaceMode.BLACKLIST);

            STRUCTURE_BLACKLIST = builder
                    .comment(LangData.CONFIGURATION_STRUCTURE_BLACKLIST.def)
                    .translation(LangData.CONFIGURATION_STRUCTURE_BLACKLIST.key)
                    .defineListAllowEmpty(
                        "structure_blacklist",
                        List.of("minecraft:oak_wall_sign"),
                        () -> "",
                        Config::validateItemName
            );
            builder.pop();


            builder.push("Hidden Features");

            MATERIAL_LIST_SCATTERED_ENABLED = builder
                    .translation(LangData.CONFIGURATION_MATERIAL_LIST_SCATTERED_ENABLED.key)
                    .define("material_list_scattered", true);

            builder.pop();

            SPEC = builder.build();
        }
    }

    public static class ServerConfig {
        public static final ModConfigSpec SPEC;
        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();



            SPEC = builder.build();
        }
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }

    private static boolean validateItemName(final Object obj) {
        if (!(obj instanceof String raw)) {
            return false;
        }

        // 是否是 tag
        boolean isTag = raw.startsWith("#");

        // 去掉 # 再处理
        String id = isTag ? raw.substring(1) : raw;

        // 更建议用 MC 规范（小写！）
        if (!raw.matches("[#]?[a-z0-9.*_-]+:[a-z0-9/.*_-]+")) {
            return false;
        }

        if (id.isEmpty()) {
            return false;
        }

        if (raw.contains("*") && !isTag) {
            return true;
        } else if (raw.contains("*") && isTag) {
            return false;
        }

        ResourceLocation rl;
        try {
            rl = ResourceLocation.parse(id);
        } catch (Exception e) {
            return false;
        }

        if (isTag) {
            return true;
        } else {
            return BuiltInRegistries.BLOCK.containsKey(rl);
        }
    }

    public static boolean isBlockValid(BlockState blockState) {
        String descriptionId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
        return CommonConfig.STRUCTURE_BLACKLIST.get().stream().noneMatch(
                (name) -> {
                    boolean isTag = name.startsWith("#");

                    if (isTag) {
                        TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(name.substring(1)));
                        return blockState.is(tag);
                    } else if (!name.contains("*")) {
                        return name.equals(descriptionId);
                    } else {
                        return StarPattern.matchWildcard(name, descriptionId);
                    }
                }
        );
    }
}
