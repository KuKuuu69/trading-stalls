package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.kukuuuu.tradingstalls.block.entity.CashDrawerBlockEntity;
import net.kukuuuu.tradingstalls.block.entity.OwnedInventoryBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class CashDrawerScreenHandler extends BaseShopScreenHandler {
    private static final int DRAWER_END = OwnedInventoryBlockEntity.INVENTORY_SIZE;
    private static final int PLAYER_START = DRAWER_END;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final CashDrawerBlockEntity drawer;
    private final ScreenHandlerContext context;

    public CashDrawerScreenHandler(int syncId, PlayerInventory playerInventory, ShopScreenData data) {
        this(syncId, playerInventory, new SimpleInventory(OwnedInventoryBlockEntity.INVENTORY_SIZE),
                null, ScreenHandlerContext.EMPTY);
    }

    public CashDrawerScreenHandler(int syncId, PlayerInventory playerInventory, CashDrawerBlockEntity drawer) {
        this(syncId, playerInventory, drawer.getInventory(), drawer,
                ScreenHandlerContext.create(drawer.getWorld(), drawer.getPos()));
    }

    private CashDrawerScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory inventory,
            CashDrawerBlockEntity drawer,
            ScreenHandlerContext context
    ) {
        super(ModScreenHandlers.CASH_DRAWER, syncId);
        checkSize(inventory, OwnedInventoryBlockEntity.INVENTORY_SIZE);
        this.drawer = drawer;
        this.context = context;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }
        addPlayerInventory(playerInventory, 8, 84);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (slotIndex < DRAWER_END) {
            if (!insertItem(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!insertItem(stack, 0, DRAWER_END, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        return original;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return drawer == null
                || drawer.isOwner(player) && canUse(context, player, ModBlocks.CASH_DRAWER);
    }
}
