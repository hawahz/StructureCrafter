package io.github.hawah.structure_crafter;

import io.github.hawah.structure_crafter.util.BlackboardRenderType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber(modid = StructureCrafter.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<BlackboardRenderType> BLACKBOARD_RENDER_TYPE = BUILDER
            .comment("Render Type of Blackboard")
            .defineEnum("blackboard_render_type", BlackboardRenderType.WRITE);

    public static final ModConfigSpec.DoubleValue FALL_ATTENUATION = BUILDER
            .comment("How much is the ability of a hay block to mitigate the momentum of falling")
            .defineInRange("fallAttenuation", 2, 1, Double.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }
}
