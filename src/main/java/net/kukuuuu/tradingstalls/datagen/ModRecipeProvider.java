package net.kukuuuu.tradingstalls.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }



    @Override
    public String getName() {
        return "Trading Stalls Recipes";
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
        var itemLookup = registryLookup.getOrThrow(RegistryKeys.ITEM);

        return new RecipeGenerator(registryLookup, exporter) {
            @Override
            public void generate() {
                ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.MISC, ModBlocks.CASH_DRAWER)
                        .pattern("   ")
                        .pattern("LCL")
                        .pattern("PPP")
                        .input('L', Items.STRIPPED_OAK_LOG)
                        .input('C', Items.COPPER_INGOT)
                        .input('P', Items.OAK_PLANKS)
                        .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                        .offerTo(exporter);

                ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.MISC, ModBlocks.TRADING_BLOCK)
                        .pattern("   ")
                        .pattern("GRG")
                        .pattern("LLL")
                        .input('G', Items.GOLD_NUGGET)
                        .input('R', Items.RED_CARPET)
                        .input('L', Items.STRIPPED_OAK_LOG)
                        .criterion(hasItem(Items.GOLD_NUGGET), conditionsFromItem(Items.GOLD_NUGGET))
                        .offerTo(exporter);
            }
        };
    }
}