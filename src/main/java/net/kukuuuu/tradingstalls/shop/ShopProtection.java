package net.kukuuuu.tradingstalls.shop;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.kukuuuu.tradingstalls.block.entity.OwnedInventoryBlockEntity;
import net.minecraft.text.Text;

public final class ShopProtection {
    private ShopProtection() {
    }

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (blockEntity instanceof OwnedInventoryBlockEntity ownedBlock
                    && ownedBlock.hasOwner()
                    && !ownedBlock.isOwner(player)
                    && !player.isCreative()) {
                player.sendMessage(Text.translatable("message.trading-stalls.not_owner"), true);
                return false;
            }
            return true;
        });
    }
}
