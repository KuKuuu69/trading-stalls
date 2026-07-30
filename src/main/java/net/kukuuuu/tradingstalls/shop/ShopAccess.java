package net.kukuuuu.tradingstalls.shop;

import net.kukuuuu.tradingstalls.block.entity.OwnedInventoryBlockEntity;
import net.minecraft.world.entity.player.Player;

public final class ShopAccess {
    private ShopAccess() {
    }

    public static boolean canBreak(Player player, OwnedInventoryBlockEntity blockEntity) {
        if (player.isCreative()) {
            return true;
        }
        if (blockEntity.hasOwner()) {
            return blockEntity.isOwner(player);
        }
        return !blockEntity.hasStoredItems();
    }
}
