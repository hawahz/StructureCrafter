package io.github.hawah.structure_crafter;

import io.github.hawah.structure_crafter.util.BlackboardRenderType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@EventBusSubscriber(modid = StructureCrafter.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<BlackboardRenderType> BLACKBOARD_RENDER_TYPE = BUILDER
            .comment("Render Type of Blackboard")
            .defineEnum("blackboard_render_type", BlackboardRenderType.WRITE);

    public static final ModConfigSpec.IntValue MAX_SIZE_X = BUILDER
            .comment("Max Size X of Selection Area")
            .defineInRange("max_size_x", 30, -1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_SIZE_Y = BUILDER
            .comment("Max Size Y of Selection Area")
            .defineInRange("max_size_y", 60, -1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_SIZE_Z = BUILDER
            .comment("Max Size Z of Selection Area")
            .defineInRange("max_size_z", 30, -1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_VOLUME = BUILDER
            .comment("Max Size of Structure")
            .defineInRange("max_volume", 100000, -1, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }
}
