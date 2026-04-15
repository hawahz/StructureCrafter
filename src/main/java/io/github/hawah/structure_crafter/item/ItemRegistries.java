package io.github.hawah.structure_crafter.item;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.block.BlockRegistry;
import io.github.hawah.structure_crafter.item.blackboard.Blackboard;
import io.github.hawah.structure_crafter.item.structure_wand.StructureWand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

@EventBusSubscriber
public class ItemRegistries {
    public static final DeferredRegister.Items ITEM = DeferredRegister.createItems(StructureCrafter.MODID);
    public static final DeferredItem<StructureWand> STRUCTURE_WAND = ITEM.register("structure_wand", StructureWand::new);
    public static final DeferredItem<Blackboard> BLACKBOARD = ITEM.register("blackboard", Blackboard::new);
    public static final DeferredItem<MaterialList> MATERIAL_LIST = ITEM.register("material_list", MaterialList::new);
    public static final DeferredItem<TelephoneHandset> TELEPHONE_HANDSET = ITEM.register("telephone_handset", TelephoneHandset::new);
    public static final DeferredItem<RulerItem> RULER_ITEM = register("ruler", RulerItem::new);
    public static final DeferredItem<BlockItem> TELEPHONE_BLOCK_ITEM = ITEM.register("phone_booth", () -> new PriorityBlockItem(BlockRegistry.TELEPHONE_BLOCK.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEM.register(eventBus);
    }

    public static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> supply) {
        return ITEM.register(name, () -> supply.apply(new Item.Properties()));
    }
    @SubscribeEvent
    public static void registerCapability(RegisterCapabilitiesEvent event) {
    }
}
