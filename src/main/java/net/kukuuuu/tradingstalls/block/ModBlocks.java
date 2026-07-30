package net.kukuuuu.tradingstalls.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.kukuuuu.tradingstalls.TradingStalls;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.Function;

public class ModBlocks {

    public static final Block TRADING_BLOCK = registerBlock("trading_block",
            TradingBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(2f, 3600000f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.WOOD));

    public static final Block CASH_DRAWER = registerBlock("cash_drawer",
            CashDrawerBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(2f, 3600000f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.WOOD));

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TradingStalls.MOD_ID, name));
        Block block = factory.apply(settings.setId(blockKey));
        registerBlockItem(name, blockKey, block);
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    private static void registerBlockItem(String name, ResourceKey<Block> blockKey, Block block) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TradingStalls.MOD_ID, name));
        Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(block, new Item.Properties().setId(itemKey)));
    }

    public static void registerModBlocks() {
        TradingStalls.LOGGER.info("Registering Mod Blocks for " + TradingStalls.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(ModBlocks.TRADING_BLOCK);
            entries.accept(ModBlocks.CASH_DRAWER);
        });
    }
}