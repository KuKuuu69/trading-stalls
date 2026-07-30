package net.kukuuuu.tradingstalls.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }



    @Override
    public String getName() {
        return "Trading Stalls Recipes";
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
        var itemLookup = registryLookup.lookupOrThrow(Registries.ITEM);

        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                ShapedRecipeBuilder.shaped(itemLookup, RecipeCategory.MISC, ModBlocks.CASH_DRAWER)
                        .pattern("   ")
                        .pattern("LCL")
                        .pattern("PPP")
                        .define('L', Items.STRIPPED_OAK_LOG)
                        .define('C', Items.COPPER_INGOT)
                        .define('P', Items.OAK_PLANKS)
                        .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                        .save(output);

                ShapedRecipeBuilder.shaped(itemLookup, RecipeCategory.MISC, ModBlocks.TRADING_BLOCK)
                        .pattern("   ")
                        .pattern("GRG")
                        .pattern("LLL")
                        .define('G', Items.GOLD_NUGGET)
                        .define('R', Items.RED_CARPET)
                        .define('L', Items.STRIPPED_OAK_LOG)
                        .unlockedBy(getHasName(Items.GOLD_NUGGET), has(Items.GOLD_NUGGET))
                        .save(output);
            }
        };
    }
}