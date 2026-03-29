package io.github.hawah.structure_crafter.datagen;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModEnUsLangProvider extends LanguageProvider {

    public ModEnUsLangProvider(PackOutput output) {
        super(output, StructureCrafter.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ItemRegistries.BLACKBOARD.get(), "Blackboard");
        add(StructureCrafter.STAMP_WEAVER_TAB.getRegisteredName(), "Stamp Weaver Tab");
    }
}
