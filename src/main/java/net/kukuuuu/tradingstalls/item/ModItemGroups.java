package net.kukuuuu.tradingstalls.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.kukuuuu.tradingstalls.TradingStalls;
import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup TRADING_STALLS_BLOCK_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(TradingStalls.MOD_ID, "trading_stalls"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModBlocks.TRADING_BLOCK))
                    .displayName(Text.translatable("itemgroup.trading-stalls.trading_stalls"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.TRADING_BLOCK);
                        entries.add(ModBlocks.CASH_DRAWER);
                    }).build());

    public static void registerItemGroups() {

        TradingStalls.LOGGER.info("Registering Item Groups " + TradingStalls.MOD_ID);

    }
}
