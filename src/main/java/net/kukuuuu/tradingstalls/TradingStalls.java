package net.kukuuuu.tradingstalls;

import net.fabricmc.api.ModInitializer;

import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.kukuuuu.tradingstalls.item.ModItemGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradingStalls implements ModInitializer {
	public static final String MOD_ID = "trading-stalls";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItemGroups.registerItemGroups();
		ModBlocks.registerModBlocks();

	}
}