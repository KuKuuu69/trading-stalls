package net.kukuuuu.tradingstalls;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.kukuuuu.tradingstalls.datagen.ModBlockTagProvider;
import net.kukuuuu.tradingstalls.datagen.ModLootTableProvider;
import net.kukuuuu.tradingstalls.datagen.ModRecipeProvider;

public class TradingStallsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModBlockTagProvider::new);
		pack.addProvider(ModLootTableProvider::new);
		pack.addProvider(ModRecipeProvider::new);


	}
}
