package io.github.hawah.structure_crafter.block;

import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(StructureCrafter.MODID);
    public static final DeferredBlock<ConnectorBlock> CONNECTOR = (DeferredBlock<ConnectorBlock>) BLOCKS.register("connector", ()->new ConnectorBlock());

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
