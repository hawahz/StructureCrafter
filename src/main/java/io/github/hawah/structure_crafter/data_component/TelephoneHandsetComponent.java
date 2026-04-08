package io.github.hawah.structure_crafter.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record TelephoneHandsetComponent(BlockPos pos, ResourceKey<Level> dimension) {

    public static final Codec<TelephoneHandsetComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(TelephoneHandsetComponent::pos),
                    ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(TelephoneHandsetComponent::dimension)
            ).apply(instance, TelephoneHandsetComponent::new));

    public static final StreamCodec<ByteBuf, TelephoneHandsetComponent> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TelephoneHandsetComponent::pos,
            ResourceKey.streamCodec(Registries.DIMENSION), TelephoneHandsetComponent::dimension,
            TelephoneHandsetComponent::new
    );
    public static final TelephoneHandsetComponent EMPTY = new TelephoneHandsetComponent(BlockPos.ZERO, Level.OVERWORLD);
}
