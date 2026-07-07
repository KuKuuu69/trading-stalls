package net.kukuuuu.tradingstalls.shop;

import net.kukuuuu.tradingstalls.block.entity.OwnedInventoryBlockEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class ShopAccess {
    private ShopAccess() {
    }

    public static boolean canBreak(PlayerEntity player, OwnedInventoryBlockEntity blockEntity) {
        if (player.isCreative()) {
            return true;
        }
        if (blockEntity.hasOwner()) {
            return blockEntity.isOwner(player);
        }
        return !blockEntity.hasStoredItems();
    }
}
