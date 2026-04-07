package io.github.hawah.structure_crafter.datagen;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.block.BlockRegistry;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, StructureCrafter.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleBlockItem(BlockRegistry.CONNECTOR.get());
        basicItem(ItemRegistries.TELEPHONE_HANDSET.get());
    }
}
