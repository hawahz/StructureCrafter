package io.github.hawah.structure_crafter.data_component;

import com.mojang.serialization.Codec;
import io.github.hawah.structure_crafter.StructureCrafter;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class DataComponentTypeRegistries {
    public static DeferredRegister.DataComponents DATA_COMPONENT = DeferredRegister.createDataComponents(
            Registries.DATA_COMPONENT_TYPE,
            StructureCrafter.MODID
    );

    public static DeferredHolder<DataComponentType<?>, DataComponentType<StructureSelectorComponent>> STRUCTURE_SELECTOR = DATA_COMPONENT.registerComponentType(
            "structure_selector",
            builder -> builder
                    .networkSynchronized(StructureSelectorComponent.STREAM_CODEC)
    );

    public static DeferredHolder<DataComponentType<?>, DataComponentType<Empty>> BLACKBOARD_WRITING = DATA_COMPONENT.registerComponentType(
            "blackboard_writing",
            builder -> builder
                    .persistent(Codec.unit(Empty::new))
    );

    public static final DataComponentType<String> STRUCTURE_FILE = register(
            "structure_file",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DataComponentType<Vec3i> STRUCTURE_BOUNDS = register(
            "schematic_bounds",
            builder -> builder.persistent(Vec3i.CODEC).networkSynchronized(CatnipStreamCodecs.VEC3I)
    );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT.register(eventBus);
    }

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENT.register(name, () -> type);
        return type;
    }
}
