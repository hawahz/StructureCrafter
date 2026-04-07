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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConnectorBlockEntity>> CONNECTOR = BLOCK_ENTITY_TYPES.register("connector", () -> BlockEntityType.Builder.of(ConnectorBlockEntity::new, BlockRegistry.CONNECTOR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BlockEntityRegistry.CONNECTOR.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof ConnectorBlockEntity connectorBlockEntity)
                        return connectorBlockEntity.itemHandler;
                    return null;
                }
        );
    }
}
