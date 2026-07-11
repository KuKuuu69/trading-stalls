package net.kukuuuu.tradingstalls.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.CASH_DRAWER)
                .pattern("   ")
                .pattern("LCL")
                .pattern("PPP")
                .input('L', net.minecraft.item.Items.STRIPPED_OAK_LOG)
                .input('C', net.minecraft.item.Items.COPPER_INGOT)
                .input('P', net.minecraft.item.Items.OAK_PLANKS)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.TRADING_BLOCK)
                .pattern("   ")
                .pattern("GRG")
                .pattern("LLL")
                .input('G', Items.GOLD_NUGGET)
                .input('R', Items.RED_CARPET)
                .input('L', Items.STRIPPED_OAK_LOG)
                .criterion(hasItem(Items.GOLD_NUGGET), conditionsFromItem(Items.GOLD_NUGGET))
                .offerTo(exporter);




    }
}
