package io.github.hawah.structure_crafter.item;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.block.BlockRegistry;
import io.github.hawah.structure_crafter.item.blackboard.Blackboard;
import io.github.hawah.structure_crafter.item.structure_wand.StructureWand;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

@EventBusSubscriber
public class ItemRegistries {
    public static final DeferredRegister.Items ITEM = DeferredRegister.createItems(StructureCrafter.MODID);
    public static final DeferredItem<StructureWand> STRUCTURE_WAND = register("structure_wand", StructureWand::new);
    public static final DeferredItem<Blackboard> BLACKBOARD = register("blackboard", Blackboard::new);
    public static final DeferredItem<MaterialList> MATERIAL_LIST = register("material_list", MaterialList::new);
    public static final DeferredItem<TelephoneHandset> TELEPHONE_HANDSET = register("telephone_handset", TelephoneHandset::new);
    public static final DeferredItem<BlockItem> TELEPHONE_BLOCK_ITEM = registerPriorityBlockItem("phone_booth", BlockRegistry.TELEPHONE_BLOCK);

    public static void register(IEventBus eventBus) {
        ITEM.register(eventBus);
    }

    private static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> item) {
        return ITEM.register(name, (registryName) -> item.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))));
    }

    private static DeferredItem<BlockItem> registerPriorityBlockItem(String name, DeferredBlock<?> block) {
        return ITEM.register(
                name,
                (registryName) ->
                        new PriorityBlockItem(
                                block.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName)))
        );
    }
}
