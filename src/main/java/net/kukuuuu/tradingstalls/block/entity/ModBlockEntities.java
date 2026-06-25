package net.kukuuuu.tradingstalls.block.entity;

import net.kukuuuu.tradingstalls.TradingStalls;
import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static BlockEntityType<TradingBlockEntity> TRADING_BLOCK_ENTITY;
    public static BlockEntityType<CashDrawerBlockEntity> CASH_DRAWER_BLOCK_ENTITY;

    public static void register() {
        TRADING_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(TradingStalls.MOD_ID, "trading_block"),
                BlockEntityType.Builder.create(TradingBlockEntity::new, ModBlocks.TRADING_BLOCK).build()
        );
        CASH_DRAWER_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(TradingStalls.MOD_ID, "cash_drawer"),
                BlockEntityType.Builder.create(CashDrawerBlockEntity::new, ModBlocks.CASH_DRAWER).build()
        );
    }
}
