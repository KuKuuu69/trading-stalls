package net.kukuuuu.tradingstalls.block.entity;

import net.kukuuuu.tradingstalls.shop.InventoryUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

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
