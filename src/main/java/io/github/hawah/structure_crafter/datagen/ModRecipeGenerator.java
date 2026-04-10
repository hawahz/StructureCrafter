package io.github.hawah.structure_crafter.datagen;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class ModRecipeGenerator extends RecipeProvider {


    protected ModRecipeGenerator(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> items = this.registries.lookupOrThrow(Registries.ITEM);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.TOOLS, ItemRegistries.BLACKBOARD)
                .pattern("xxx")
                .pattern("ooo")
                .pattern("xxx")
                .define('x', ItemTags.WOODEN_SLABS)
                .define('o', Items.BLACK_WOOL)
                .unlockedBy("has_black_wool", has(Items.BLACK_WOOL))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.TOOLS, ItemRegistries.STRUCTURE_WAND)
                .pattern(" ow")
                .pattern(" do")
                .pattern("s  ")
                .define('s', Items.STICK)
                .define('o', Items.OBSIDIAN)
                .define('d', Items.DIAMOND)
                .define('w', Items.WHITE_WOOL)
                .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.TOOLS, ItemRegistries.MATERIAL_LIST)
                .pattern(" i ")
                .pattern(" p ")
                .pattern("   ")
                .define('i', Items.IRON_NUGGET)
                .define('p', Items.PAPER)
                .unlockedBy("has_wand", has(ItemRegistries.STRUCTURE_WAND))
                .save(output);
    }

    public static class Runner extends RecipeProvider.Runner {
        // Get the parameters from the `GatherDataEvent`s.
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new ModRecipeGenerator(provider, output);
        }

        @Override
        public String getName() {
            return StructureCrafter.MODID;
        }
    }
}
