package io.github.hawah.structure_crafter.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StructureSelectorComponent(boolean active, boolean sign, BlockPos pos0, BlockPos pos1) {
    public static final Codec<StructureSelectorComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("active").forGetter(StructureSelectorComponent::active),
                    Codec.BOOL.fieldOf("sign").forGetter(StructureSelectorComponent::sign),
                    BlockPos.CODEC.fieldOf("pos0").forGetter(StructureSelectorComponent::pos0),
                    BlockPos.CODEC.fieldOf("pos1").forGetter(StructureSelectorComponent::pos1)
            ).apply(instance, StructureSelectorComponent::new)
    );

    public static final StreamCodec<ByteBuf, StructureSelectorComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, StructureSelectorComponent::active,
            ByteBufCodecs.BOOL, StructureSelectorComponent::sign,
            BlockPos.STREAM_CODEC, StructureSelectorComponent::pos0,
            BlockPos.STREAM_CODEC, StructureSelectorComponent::pos1,
            StructureSelectorComponent::new
    );
}
