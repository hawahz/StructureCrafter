package io.github.hawah.structure_crafter.datagen;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class StampWeaverItemModelProvider extends ItemModelProvider {

    public StampWeaverItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, StructureCrafter.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ItemRegistries.BLACKBOARD.get());
        basicItem(ItemRegistries.STRUCTURE_WAND.get());
    }
}
