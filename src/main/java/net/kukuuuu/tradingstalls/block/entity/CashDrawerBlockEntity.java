package net.kukuuuu.tradingstalls.block.entity;

import net.kukuuuu.tradingstalls.shop.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class CashDrawerBlockEntity extends OwnedInventoryBlockEntity {
    public CashDrawerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CASH_DRAWER_BLOCK_ENTITY, pos, state);
    }

    public boolean canAccept(ItemStack payment) {
        return InventoryUtils.canInsertFully(getInventory(), payment);
    }

    public void accept(ItemStack payment) {
        InventoryUtils.insertFully(getInventory(), payment);
    }
}
