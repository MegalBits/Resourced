package net.megal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.loader.impl.util.StringUtil;
import net.megal.item.ResourcedItem;
import net.minecraft.data.client.*;
import net.minecraft.data.server.recipe.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import static net.megal.item.ResourcedItem.*;

import java.util.*;

public class ResourcedDataGen implements DataGeneratorEntrypoint {
	public static List<ResourcedItem> resourcedItems = new ArrayList<>();
	public static List<Item> generatableNames = new ArrayList<>();
	public static Multimap<ModelType, Item> generatableModels = HashMultimap.create();
	public static Multimap<RecipeType, Pair<Item, RecipeInstance>> generatableRecipes = HashMultimap.create();

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		resourcedItems.forEach(resourcedItem -> {
			if (resourcedItem.hasDefaultName) generatableNames.add(resourcedItem.item);

			if (resourcedItem.recipes != null) {
				resourcedItem.recipes.forEach(recipeInstance -> {
					generatableRecipes.put(recipeInstance.recipeType, new Pair<>(resourcedItem.item, recipeInstance));
				});
			}
		});

		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ResourcedNameProvider::new);
		pack.addProvider(ResourcedRecipeProvider::new);
	}

	public static class ResourcedNameProvider extends FabricLanguageProvider {
		protected ResourcedNameProvider(FabricDataOutput dataOutput) {
			super(dataOutput);
		}

		@Override
		public void generateTranslations(TranslationBuilder translationBuilder) {
			generatableNames.forEach(item -> {
				Identifier identifier = Registries.ITEM.getId(item);
				StringBuilder nameBuilder = new StringBuilder();

				Arrays.stream(identifier.getPath().replace("_", "& ").split("&")).forEach(word -> nameBuilder.append(StringUtil.capitalize(word)));
				translationBuilder.add(item, nameBuilder.toString());
			});
		}
	}

	public static class ResourcedModelProvider extends FabricModelProvider {
		public static final Model BOW = item("bow");

		public ResourcedModelProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {}

		@Override
		public void generateItemModels(ItemModelGenerator itemModelGenerator) {
			generatableModels.get(ModelType.GENERATED).forEach(item -> {
				itemModelGenerator.register(item, Models.GENERATED);
			});

			generatableModels.get(ModelType.HANDHELD).forEach(item -> {
				itemModelGenerator.register(item, Models.HANDHELD);
			});

			generatableModels.get(ModelType.BOW).forEach(item -> {
				BOW.upload(ModelIds.getItemModelId(item), TextureMap.layer0(item), itemModelGenerator.writer, this::createBowJson);
				itemModelGenerator.register(item, "_pulling_0", BOW);
				itemModelGenerator.register(item, "_pulling_1", BOW);
				itemModelGenerator.register(item, "_pulling_2", BOW);
			});
		}

		public final JsonObject createBowJson(Identifier id, Map<TextureKey, Identifier> textures) {
			JsonObject jsonObject = BOW.createJson(id, textures);
			JsonArray jsonArray = new JsonArray();

			for(int i = 0; i < 3; i++) {
				JsonObject jsonObject2 = new JsonObject();
				JsonObject jsonObject3 = new JsonObject();
				jsonObject3.addProperty("pulling", 1);
				if (i == 1 || i == 2) jsonObject3.addProperty("pull", i == 1 ? 0.65 : 0.9);
				jsonObject2.add("predicate", jsonObject3);
				jsonObject2.addProperty("model", id.withSuffixedPath("_pulling_" + i).toString());
				jsonArray.add(jsonObject2);
			}

			jsonObject.add("overrides", jsonArray);
			return jsonObject;
		}

		private static Model item(String parent) {
			return new Model(Optional.of(new Identifier("item/" + parent)), Optional.empty());
		}
	}

	public static class ResourcedRecipeProvider extends FabricRecipeProvider {
		public ResourcedRecipeProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generate(RecipeExporter exporter) {
			generatableRecipes.get(RecipeType.PACKING).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				RecipeInstance recipeInstance = recipePair.getRight();

				ShapedRecipeJsonBuilder.create(recipeInstance.category, output)
						.pattern("###")
						.pattern("###")
						.pattern("###")
						.input('#', recipeInstance.ingredients[0])
						.offerTo(exporter, "pack_" + getItemPath(output));
			});

			generatableRecipes.get(RecipeType.UNPACKING).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				RecipeInstance recipeInstance = recipePair.getRight();

				ShapelessRecipeJsonBuilder.create(recipeInstance.category, output, 9)
						.input(recipeInstance.ingredients[0])
						.offerTo(exporter, "unpack_" + getItemPath(output));
			});

			generatableRecipes.get(RecipeType.SWORD).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				RecipeInstance recipeInstance = recipePair.getRight();

				ShapedRecipeJsonBuilder.create(recipeInstance.category, output)
						.pattern("#")
						.pattern("#")
						.pattern("/")
						.input('/', recipeInstance.ingredients[0])
						.input('#', recipeInstance.ingredients[1])
						.offerTo(exporter, getItemPath(output));
			});

			generatableRecipes.get(RecipeType.SHOVEL).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				RecipeInstance recipeInstance = recipePair.getRight();

				ShapedRecipeJsonBuilder.create(recipeInstance.category, output)
						.pattern("#")
						.pattern("/")
						.pattern("/")
						.input('/', recipeInstance.ingredients[0])
						.input('#', recipeInstance.ingredients[1])
						.offerTo(exporter, getItemPath(output));
			});

			generatableRecipes.get(RecipeType.PICKAXE).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				RecipeInstance recipeInstance = recipePair.getRight();

				ShapedRecipeJsonBuilder.create(recipeInstance.category, output)
						.pattern("###")
						.pattern(" / ")
						.pattern(" / ")
						.input('/', recipeInstance.ingredients[0])
						.input('#', recipeInstance.ingredients[1])
						.offerTo(exporter, getItemPath(output));
			});

			generatableRecipes.get(RecipeType.AXE).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				RecipeInstance recipeInstance = recipePair.getRight();

				ShapedRecipeJsonBuilder.create(recipeInstance.category, output)
						.pattern("##")
						.pattern("#/")
						.pattern(" /")
						.input('/', recipeInstance.ingredients[0])
						.input('#', recipeInstance.ingredients[1])
						.offerTo(exporter, getItemPath(output));
			});

			generatableRecipes.get(RecipeType.HOE).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				RecipeInstance recipeInstance = recipePair.getRight();

				ShapedRecipeJsonBuilder.create(recipeInstance.category, output)
						.pattern("##")
						.pattern(" /")
						.pattern(" /")
						.input('/', recipeInstance.ingredients[0])
						.input('#', recipeInstance.ingredients[1])
						.offerTo(exporter, getItemPath(output));
			});

			generatableRecipes.get(RecipeType.BOW).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				RecipeInstance recipeInstance = recipePair.getRight();

				ShapedRecipeJsonBuilder.create(recipeInstance.category, output)
						.pattern(" /s")
						.pattern("/ s")
						.pattern(" /s")
						.input('/', recipeInstance.ingredients[0])
						.input('s', recipeInstance.ingredients[1])
						.offerTo(exporter, getItemPath(output));
			});

			generatableRecipes.get(RecipeType.SMELTING).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				SmeltingRecipeInstance recipeInstance = (SmeltingRecipeInstance) recipePair.getRight();

				CookingRecipeJsonBuilder.createSmelting(recipeInstance.ingredients[0], recipeInstance.category, output, recipeInstance.xp, recipeInstance.ticks)
						.offerTo(exporter, getItemPath(output) + "_from_smelting");
			});

			generatableRecipes.get(RecipeType.SMOKING).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				SmeltingRecipeInstance recipeInstance = (SmeltingRecipeInstance) recipePair.getRight();

				CookingRecipeJsonBuilder.createSmoking(recipeInstance.ingredients[0], recipeInstance.category, output, recipeInstance.xp, recipeInstance.ticks)
						.offerTo(exporter, getItemPath(output) + "_from_smoking");
			});

			generatableRecipes.get(RecipeType.BLASTING).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				SmeltingRecipeInstance recipeInstance = (SmeltingRecipeInstance) recipePair.getRight();

				CookingRecipeJsonBuilder.createBlasting(recipeInstance.ingredients[0], recipeInstance.category, output, recipeInstance.xp, recipeInstance.ticks)
						.offerTo(exporter, getItemPath(output) + "_from_blasting");
			});

			generatableRecipes.get(RecipeType.SMITHING).forEach(recipePair -> {
				Item output = recipePair.getLeft();
				RecipeInstance recipeInstance = recipePair.getRight();

				SmithingTransformRecipeJsonBuilder.create(recipeInstance.ingredients[0], recipeInstance.ingredients[1], recipeInstance.ingredients[2], recipeInstance.category, output)
						.offerTo(exporter, getItemPath(output));
			});
		}
	}
}
