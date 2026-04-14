package io.github.hawah.structure_crafter.recipe;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.List;

public class RecipeHelper {
    public static void getResource(ServerLevel level) {
        RecipeManagerWarper recipeManager = RecipeManagerWarper.create(level);
        List<RecipeHolder<CraftingRecipe>> allCraftingRecipe = recipeManager.getAllRecipesFor(RecipeType.CRAFTING);
        RecipeHolder<CraftingRecipe> first = allCraftingRecipe.getFirst();
    }

//    public static List<ItemStack> applyReverse(List<ItemStack> need, RecipeHolder<CraftingRecipe> recipe, ServerLevel level) {
//        ItemStack resultItem = recipe.value().getResultItem(level.registryAccess());
//        Optional<ItemStack> anyMatchedNeed = need.stream().filter(itemStack -> itemStack.is(resultItem.getItem())).findFirst();
//        if (anyMatchedNeed.isEmpty())
//            return null;
//
//        ItemStack required = anyMatchedNeed.get();
//        int requiredCount = required.getCount();
//
//        int applyTimes = (int) Math.ceil(requiredCount/ (double) resultItem.getCount());
//
//        need.remove(required)
//
//
//    }

    public static boolean isBase(ItemStack itemStack) {
        return itemStack.is(ItemTags.LOGS) ||
                itemStack.is(ItemTags.STONE_CRAFTING_MATERIALS);
    }
}
