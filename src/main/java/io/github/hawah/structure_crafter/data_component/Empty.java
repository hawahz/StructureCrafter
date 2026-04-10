package io.github.hawah.structure_crafter.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record Empty() {
    public static final Codec<Empty> CODEC = MapCodec.unitCodec(Empty::new);

}
