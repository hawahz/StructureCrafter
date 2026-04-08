package io.github.hawah.structure_crafter.item;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.block.BlockRegistry;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.HashItemComponent;
import io.github.hawah.structure_crafter.item.blackboard.Blackboard;
import io.github.hawah.structure_crafter.item.structure_wand.StructureWand;
import io.github.hawah.structure_crafter.util.HashItemHandler;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber
public class ItemRegistries {
    public static final DeferredRegister.Items ITEM = DeferredRegister.createItems(StructureCrafter.MODID);
    public static final DeferredItem<StructureWand> STRUCTURE_WAND = ITEM.register("structure_wand", StructureWand::new);
    public static final DeferredItem<Blackboard> BLACKBOARD = ITEM.register("blackboard", Blackboard::new);
    public static final DeferredItem<MaterialList> MATERIAL_LIST = ITEM.register("material_list", MaterialList::new);
    public static final DeferredItem<TelephoneHandset> TELEPHONE_HANDSET = ITEM.register("telephone_handset", TelephoneHandset::new);
    public static final DeferredItem<BlockItem> CONNECTOR_BLOCK_ITEM = ITEM.register("connector", () -> new PriorityBlockItem(BlockRegistry.CONNECTOR.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEM.register(eventBus);
    }

    @SubscribeEvent
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.ItemHandler.ITEM,
                (stack, access) ->{
                    HashItemComponent hashItemComponent = stack.get(DataComponentTypeRegistries.HASH_ITEM);
                    if (hashItemComponent == null) {
                        return null;
                    }
                    return new HashItemHandler(stack);
                },
                ItemRegistries.TELEPHONE_HANDSET
        );
    }
}
