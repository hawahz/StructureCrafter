package io.github.hawah.structure_crafter.datagen;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModZhCnLangProvider extends LanguageProvider {

    public ModZhCnLangProvider(PackOutput output) {
        super(output, StructureCrafter.MODID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        add(ItemRegistries.BLACKBOARD.get(), "黑板");
    }
}
