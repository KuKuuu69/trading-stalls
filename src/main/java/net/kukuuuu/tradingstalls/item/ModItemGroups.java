package net.kukuuuu.tradingstalls.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.kukuuuu.tradingstalls.TradingStalls;
import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {
    public static final CreativeModeTab TRADING_STALLS_BLOCK_GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(TradingStalls.MOD_ID, "trading_stalls"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModBlocks.TRADING_BLOCK))
                    .title(Component.translatable("itemgroup.trading-stalls.trading_stalls"))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(ModBlocks.TRADING_BLOCK);
                        entries.accept(ModBlocks.CASH_DRAWER);
                    }).build());

    public static void registerItemGroups() {

        TradingStalls.LOGGER.info("Registering Item Groups " + TradingStalls.MOD_ID);

    }
}
