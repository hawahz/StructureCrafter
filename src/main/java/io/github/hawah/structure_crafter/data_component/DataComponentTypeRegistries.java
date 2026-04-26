package io.github.hawah.structure_crafter.data_component;

import com.mojang.serialization.Codec;
import io.github.hawah.structure_crafter.StructureCrafter;
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

    public static DeferredHolder<DataComponentType<?>, DataComponentType<Empty>> BLACKBOARD_WRITING = DATA_COMPONENT.registerComponentType(
            "blackboard_writing",
            builder -> builder
                    .persistent(Codec.unit(Empty::new))
    );

    public static final DataComponentType<String> STRUCTURE_FILE = register(
            "structure_file",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DataComponentType<String> STRUCTURE_OWNER = register(
            "structure_owner",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final DataComponentType<Vec3i> STRUCTURE_SIZE = register(
            "structure_size",
            builder -> builder.persistent(Vec3i.CODEC).networkSynchronized(
                    StreamCodec.composite(
                            ByteBufCodecs.INT, Vec3i::getX,
                            ByteBufCodecs.INT, Vec3i::getY,
                            ByteBufCodecs.INT, Vec3i::getZ,
                            Vec3i::new
                    )
            )
    );

    public static final DataComponentType<Boolean> RULER_EDGE_MODE = register(
            "ruler_edge_mode",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<MaterialListComponent> MATERIAL_LIST = register(
            "material_list",
            builder -> builder
                    .persistent(MaterialListComponent.CODEC)
                    .networkSynchronized(MaterialListComponent.STREAM_CODEC)
    );

    public static final DataComponentType<TelephoneHandsetComponent> TELEPHONE_HANDSET_SOURCE = register(
            "schematic_bounds",
            builder -> builder.persistent(TelephoneHandsetComponent.CODEC).networkSynchronized(TelephoneHandsetComponent.STREAM_CODEC)
    );

    public static final DataComponentType<Integer> STRUCTURE_WAND_SETTINGS = register(
            "structure_wand_settings",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
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
