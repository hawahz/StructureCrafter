package io.github.hawah.structure_crafter.datagen;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.datagen.lang.LangData;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModEnUsLangProvider extends LanguageProvider {

    public ModEnUsLangProvider(PackOutput output) {
        super(output, StructureCrafter.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ItemRegistries.BLACKBOARD.get(), "Blackboard");
        add("itemGroup.structure_crafter", "Structure Crafter");
        add(ItemRegistries.STRUCTURE_WAND.get(), "Structure Wand");

        genLang(this);
    }

    public static void genLang(LanguageProvider pvd) {
        for (LangData lang : LangData.values()) {
            pvd.add(lang.key, lang.def);
        }
    }
}
