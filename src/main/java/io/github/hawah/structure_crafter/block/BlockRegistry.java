package io.github.hawah.structure_crafter.block;

import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(StructureCrafter.MODID);
    public static final DeferredBlock<TelephoneBlock> TELEPHONE_BLOCK = (DeferredBlock<TelephoneBlock>) BLOCKS.register("phone_booth", ()->new TelephoneBlock());

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
