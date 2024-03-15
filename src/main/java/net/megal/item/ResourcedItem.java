package net.megal.item;

import net.megal.ResourcedDataGen;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ResourcedItem {
    public final Item item;
    public final boolean hasDefaultName;
    public final @Nullable ModelType modelType;
    public final @Nullable List<RecipeInstance> recipes;

    private ResourcedItem(Item item, boolean hasDefaultName, @Nullable ModelType modelType, @Nullable List<RecipeInstance> recipes) {
        this.item = item;
        this.hasDefaultName = hasDefaultName;
        this.modelType = modelType;
        this.recipes = recipes;

        ResourcedDataGen.resourcedItems.add(this);
    }

    public static ResourcedItemBuilder builder(Item item) {
        return new ResourcedItemBuilder(item);
    }

    public static enum ModelType {
        GENERATED,
        HANDHELD,
        BOW
    }

    public static enum RecipeType {
        PACKING,
        UNPACKING,
        SWORD,
        SHOVEL,
        PICKAXE,
        AXE,
        HOE,
        BOW,
        SMELTING,
        SMOKING,
        BLASTING,
        SMITHING
    }

    public static class RecipeInstance {
        public final RecipeType recipeType;
        public final RecipeCategory category;
        public final Ingredient[] ingredients;

        private RecipeInstance(RecipeType recipeType, RecipeCategory category, Ingredient... ingredients) {
            this.recipeType = recipeType;
            this.category = category;
            this.ingredients = ingredients;
        }
    }
    public static class SmeltingRecipeInstance extends RecipeInstance {
        public final int ticks;
        public final float xp;

        private SmeltingRecipeInstance(RecipeType recipeType, RecipeCategory category, Ingredient input, int ticks, float xp) {
            super(recipeType, category, input);
            this.ticks = ticks;
            this.xp = xp;
        }
    }

    public static class ResourcedItemBuilder {
        private final Item item;
        private boolean hasDefaultName;
        private ModelType modelType;
        private final List<RecipeInstance> recipes = new ArrayList<>();

        ResourcedItemBuilder(Item item) {
            this.item = item;
        }

        public ResourcedItemBuilder defaultName() {
            this.hasDefaultName = true;
            return this;
        }

        public ResourcedItemBuilder model(ModelType modelType) {
            this.modelType = modelType;
            return this;
        }

        public ResourcedItemBuilder packing(RecipeCategory recipeCategory, Ingredient ingredient) {
            recipes.add(new RecipeInstance(RecipeType.PACKING, recipeCategory, ingredient));
            return this;
        }

        public ResourcedItemBuilder unpacking(RecipeCategory recipeCategory, Ingredient ingredient) {
            recipes.add(new RecipeInstance(RecipeType.UNPACKING, recipeCategory, ingredient));
            return this;
        }

        public ResourcedItemBuilder tool(RecipeType recipeType, RecipeCategory recipeCategory, Ingredient stick, Ingredient material) {
            recipes.add(new RecipeInstance(recipeType, recipeCategory, stick, material));
            return this;
        }

        public ResourcedItemBuilder bow(RecipeCategory recipeCategory, Ingredient stick, Ingredient string) {
            recipes.add(new RecipeInstance(RecipeType.BOW, recipeCategory, stick, string));
            return this;
        }

        public ResourcedItemBuilder smelting(RecipeType recipeType, RecipeCategory recipeCategory, Ingredient input, int ticks, float xp) {
            recipes.add(new SmeltingRecipeInstance(recipeType, recipeCategory, input, ticks, xp));
            return this;
        }

        public ResourcedItemBuilder smelting(RecipeCategory recipeCategory, Ingredient input, int ticks, float xp) {
            recipes.add(new SmeltingRecipeInstance(RecipeType.SMELTING, recipeCategory, input, ticks, xp));
            return this;
        }

        public ResourcedItemBuilder smoking(boolean hasSmelting, RecipeCategory recipeCategory, Ingredient input, int ticks, float xp) {
            if (hasSmelting) recipes.add(new SmeltingRecipeInstance(RecipeType.SMELTING, recipeCategory, input, ticks, xp));
            recipes.add(new SmeltingRecipeInstance(RecipeType.SMOKING, recipeCategory, input, ticks / 2, xp));
            return this;
        }

        public ResourcedItemBuilder blasting(boolean hasSmelting, RecipeCategory recipeCategory, Ingredient input, int ticks, float xp) {
            if (hasSmelting) recipes.add(new SmeltingRecipeInstance(RecipeType.SMELTING, recipeCategory, input, ticks, xp));
            recipes.add(new SmeltingRecipeInstance(RecipeType.BLASTING, recipeCategory, input, ticks / 2, xp));
            return this;
        }

        public ResourcedItemBuilder smithing(Ingredient template, RecipeCategory recipeCategory, Ingredient input, Ingredient material) {
            recipes.add(new RecipeInstance(RecipeType.SMITHING, recipeCategory, template, input, material));
            return this;
        }

        public ResourcedItem build() {
            return new ResourcedItem(item, hasDefaultName, modelType, recipes);
        }
    }
}
