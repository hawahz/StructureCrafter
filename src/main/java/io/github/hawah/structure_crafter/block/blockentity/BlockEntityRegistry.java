package io.github.hawah.structure_crafter.block.blockentity;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.block.BlockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber
public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, StructureCrafter.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TelephoneBlockEntity>> TELEPHONE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("connector", () -> new BlockEntityType<>(
                    TelephoneBlockEntity::new,
                    false,
                    BlockRegistry.TELEPHONE_BLOCK.get()
            ));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                BlockEntityRegistry.TELEPHONE_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof TelephoneBlockEntity telephoneBlockEntity) {
                        telephoneBlockEntity.setDirty();
                        return telephoneBlockEntity.itemHandler;
                    }
                    return null;
                }
        );
    }
}
