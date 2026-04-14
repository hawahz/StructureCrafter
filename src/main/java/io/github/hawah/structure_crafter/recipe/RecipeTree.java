package io.github.hawah.structure_crafter.recipe;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.List;

public class RecipeTree {

    public final List<Node> nextNodes = new ArrayList<>();
    public final List<Integer> cost = new ArrayList<>();
    public final ItemStack rootNeed;
    public int depth = 0;
    public static final int MAX_DEPTH = 3;
    private final ServerLevel serverLevel;

    public RecipeTree(ItemStack rootNeed, ServerLevel serverLevel) {
        this.rootNeed = rootNeed;
        this.serverLevel = serverLevel;
    }

    public static RecipeTree create(ItemStack rootNeed, ServerLevel serverLevel) {
        return new RecipeTree(rootNeed, serverLevel);
    }

    public RecipeTree init() {
        if (depth != 0 || !nextNodes.isEmpty()) {
            return this;
        }
        RecipeManagerWarper recipeManager = RecipeManagerWarper.create(serverLevel);
        for (RecipeHolder<CraftingRecipe> craftingRecipeRecipeHolder : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!craftingRecipeRecipeHolder.value().getResultItem(serverLevel.registryAccess()).is(rootNeed.getItem())) {
                continue;
            }
            Node.parse(craftingRecipeRecipeHolder.value(), serverLevel, this).immutable();
        }
        depth = 1;
        return this;
    }

    public RecipeTree grow() {
        if (depth != 1)
            throw new RuntimeException();
        for (Node tail : this.nextNodes) {
            tail.grow(serverLevel, this);
        }
        return this;
    }


    public static class Node {
        private boolean immutable = false;
        protected final ItemStack result;
        protected final int depth;
        protected final Node prev;
        protected final List<Ingredient> ingredients = new ArrayList<>();
        protected final List<Node> nextNodes = new ArrayList<>();

        private Node(ItemStack result, int depth, Node prev) {
            this.result = result;
            this.depth = depth;
            this.prev = prev;
            if (prev != null)
                prev.nextNodes.add(this);
        }

        public static Node create(ItemStack result, Node prev) {
            return new Node(result, prev.depth + 1, prev);
        }

        public static Node create(ItemStack result) {
            return new Node(result, 1, null);
        }

        public static void parse(CraftingRecipe recipe, ServerLevel serverLevel, Node prev) {
            ItemStack resultItem = recipe.getResultItem(serverLevel.registryAccess());
            Node node = create(resultItem, prev);
            node.ingredients.addAll(recipe.getIngredients());
        }

        public static Node parse(CraftingRecipe recipe, ServerLevel serverLevel, RecipeTree prev) {
            ItemStack resultItem = recipe.getResultItem(serverLevel.registryAccess());
            Node node = create(resultItem);
            prev.nextNodes.add(node);
            node.ingredients.addAll(recipe.getIngredients());
            return node;
        }

        public void grow(ServerLevel serverLevel, RecipeTree root) {
            if (root.depth >= RecipeTree.MAX_DEPTH || immutable)
                return;
            for (RecipeHolder<CraftingRecipe> craftingRecipeRecipeHolder : RecipeManagerWarper.create(serverLevel).getAllRecipesFor(RecipeType.CRAFTING)) {
                if (this.ingredients.stream().noneMatch(ingredient -> ingredient.test(craftingRecipeRecipeHolder.value().getResultItem(serverLevel.registryAccess())))) {
                    continue;
                }
                parse(craftingRecipeRecipeHolder.value(), serverLevel, this);
            }
            if (this.nextNodes.isEmpty()) {
                return;
            }
            root.depth = Math.max(root.depth, this.depth + 1);
            this.nextNodes.forEach(node -> node.grow(serverLevel, root));
            this.immutable();
        }

        public void immutable() {
            this.immutable = true;
        }
    }
}
