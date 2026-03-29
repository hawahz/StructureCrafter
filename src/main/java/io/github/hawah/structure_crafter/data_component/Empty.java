package io.github.hawah.structure_crafter.data_component;

import com.mojang.serialization.Codec;

public record Empty() {
    public static final Codec<Empty> CODEC = Codec.unit(Empty::new);

}
