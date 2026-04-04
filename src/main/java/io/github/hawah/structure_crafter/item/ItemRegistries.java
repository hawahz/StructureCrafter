package io.github.hawah.structure_crafter.item;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.item.blackboard.Blackboard;
import io.github.hawah.structure_crafter.item.structure_wand.StructureWand;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistries {
    public static final DeferredRegister.Items ITEM = DeferredRegister.createItems(StructureCrafter.MODID);
    public static final DeferredItem<StructureWand> STRUCTURE_WAND = ITEM.register("structure_wand", StructureWand::new);
    public static final DeferredItem<Blackboard> BLACKBOARD = ITEM.register("blackboard", Blackboard::new);
    public static final DeferredItem<MaterialList> MATERIAL_LIST = ITEM.register("material_list", MaterialList::new);

    public static void register(IEventBus eventBus) {
        ITEM.register(eventBus);
    }
}
