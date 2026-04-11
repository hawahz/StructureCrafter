package io.github.hawah.structure_crafter.block;

import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class BlockRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(StructureCrafter.MODID);
    public static final DeferredBlock<TelephoneBlock> TELEPHONE_BLOCK = BLOCKS.register(
            "phone_booth",
            (re) -> new TelephoneBlock(BlockBehaviour.Properties.of().strength(1.5f).setId(ResourceKey.create(Registries.BLOCK, re)))
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
    private static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, T> block) {
        return BLOCKS.register(name, identifier -> block.apply(
                BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, identifier)))
        );
    }
}
