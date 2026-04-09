package io.github.hawah.structure_crafter.datagen;

import io.github.hawah.structure_crafter.item.ItemRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class ModRecipeGenerator extends RecipeProvider implements IConditionBuilder {
    public ModRecipeGenerator(PackOutput p_248933_, CompletableFuture<HolderLookup.Provider> p_323846_) {
        super(p_248933_, p_323846_);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ItemRegistries.BLACKBOARD)
                .pattern("xxx")
                .pattern("ooo")
                .pattern("xxx")
                .define('x', ItemTags.WOODEN_SLABS)
                .define('o', Items.BLACK_WOOL)
                .unlockedBy("has_black_wool", has(Items.BLACK_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ItemRegistries.STRUCTURE_WAND)
                .pattern(" ow")
                .pattern(" do")
                .pattern("s  ")
                .define('s', Items.STICK)
                .define('o', Items.OBSIDIAN)
                .define('d', Items.DIAMOND)
                .define('w', Items.WHITE_WOOL)
                .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ItemRegistries.MATERIAL_LIST)
                .pattern(" i ")
                .pattern(" p ")
                .pattern("   ")
                .define('i', Items.IRON_NUGGET)
                .define('p', Items.PAPER)
                .unlockedBy("has_wand", has(ItemRegistries.STRUCTURE_WAND))
                .save(recipeOutput);


    }
}
