package io.github.hawah.structure_crafter.recipe;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.*;

import java.util.List;

public class RecipeManagerWarper {
    private final RecipeManager recipeManager;

    public RecipeManagerWarper(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    public static RecipeManagerWarper create(ServerLevel serverLevel) {
        return new RecipeManagerWarper(serverLevel.getRecipeManager());
    }

    public <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> getAllRecipesFor(RecipeType<T> recipeType) {
        return recipeManager.getAllRecipesFor(recipeType);
    }
}
