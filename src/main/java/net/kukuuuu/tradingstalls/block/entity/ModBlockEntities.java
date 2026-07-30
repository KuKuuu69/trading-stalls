package net.kukuuuu.tradingstalls.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.kukuuuu.tradingstalls.TradingStalls;
import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static BlockEntityType<TradingBlockEntity> TRADING_BLOCK_ENTITY;
    public static BlockEntityType<CashDrawerBlockEntity> CASH_DRAWER_BLOCK_ENTITY;

    public static void register() {
        TRADING_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(TradingStalls.MOD_ID, "trading_block"),
                FabricBlockEntityTypeBuilder.create(TradingBlockEntity::new, ModBlocks.TRADING_BLOCK).build()
        );
        CASH_DRAWER_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(TradingStalls.MOD_ID, "cash_drawer"),
                FabricBlockEntityTypeBuilder.create(CashDrawerBlockEntity::new, ModBlocks.CASH_DRAWER).build()
        );
    }
}
