package net.kukuuuu.tradingstalls.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.kukuuuu.tradingstalls.TradingStalls;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModBlocks {

    public static final Block TRADING_BLOCK = registerBlock("trading_block",
            TradingBlock::new,
            AbstractBlock.Settings.create()
                    .strength(2f, 3600000f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.WOOD));

    public static final Block CASH_DRAWER = registerBlock("cash_drawer",
            CashDrawerBlock::new,
            AbstractBlock.Settings.create()
                    .strength(2f, 3600000f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.WOOD));

    private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(TradingStalls.MOD_ID, name));
        Block block = factory.apply(settings.registryKey(blockKey));
        registerBlockItem(name, blockKey, block);
        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static void registerBlockItem(String name, RegistryKey<Block> blockKey, Block block) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(TradingStalls.MOD_ID, name));
        Registry.register(Registries.ITEM, itemKey,
                new BlockItem(block, new Item.Settings().registryKey(itemKey)));
    }

    public static void registerModBlocks() {
        TradingStalls.LOGGER.info("Registering Mod Blocks for " + TradingStalls.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(ModBlocks.TRADING_BLOCK);
            entries.add(ModBlocks.CASH_DRAWER);
        });
    }
}