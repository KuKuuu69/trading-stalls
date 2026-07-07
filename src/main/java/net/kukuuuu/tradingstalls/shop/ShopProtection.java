package net.kukuuuu.tradingstalls.shop;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.kukuuuu.tradingstalls.block.entity.OwnedInventoryBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public final class ShopProtection {
    private ShopProtection() {
    }

    public static void register() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.getBlockEntity(pos) instanceof OwnedInventoryBlockEntity ownedBlock
                    && !ShopAccess.canBreak(player, ownedBlock)) {
                if (!world.isClient) {
                    player.sendMessage(Text.translatable("message.trading-stalls.not_owner"), true);
                }
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (blockEntity instanceof OwnedInventoryBlockEntity ownedBlock
                    && !ShopAccess.canBreak(player, ownedBlock)) {
                player.sendMessage(Text.translatable("message.trading-stalls.not_owner"), true);
                return false;
            }
            return true;
        });
    }
}
