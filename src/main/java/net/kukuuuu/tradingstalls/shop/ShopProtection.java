package net.kukuuuu.tradingstalls.shop;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.kukuuuu.tradingstalls.block.entity.OwnedInventoryBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

public final class ShopProtection {
    private ShopProtection() {
    }

    public static void register() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.getBlockEntity(pos) instanceof OwnedInventoryBlockEntity ownedBlock
                    && !ShopAccess.canBreak(player, ownedBlock)) {
                if (!world.isClientSide()) {
                    player.displayClientMessage(Component.translatable("message.trading-stalls.not_owner"), true);
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (blockEntity instanceof OwnedInventoryBlockEntity ownedBlock
                    && !ShopAccess.canBreak(player, ownedBlock)) {
                player.displayClientMessage(Component.translatable("message.trading-stalls.not_owner"), true);
                return false;
            }
            return true;
        });
    }
}
