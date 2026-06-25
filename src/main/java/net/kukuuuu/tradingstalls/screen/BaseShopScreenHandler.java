package net.kukuuuu.tradingstalls.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public abstract class BaseShopScreenHandler extends ScreenHandler {
    protected BaseShopScreenHandler(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    protected void addPlayerInventory(PlayerInventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, x + column * 18, y + 58));
        }
    }

    protected boolean canFullyInsertIntoSlots(ItemStack stack, int startIndex, int endIndex) {
        int remaining = stack.getCount();
        for (int index = startIndex; index < endIndex; index++) {
            Slot slot = slots.get(index);
            ItemStack existing = slot.getStack();
            if (existing.isEmpty() && slot.canInsert(stack)) {
                remaining -= slot.getMaxItemCount(stack);
            } else if (slot.canInsert(stack) && ItemStack.areItemsAndComponentsEqual(existing, stack)) {
                remaining -= Math.max(0, slot.getMaxItemCount(stack) - existing.getCount());
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    protected static class ReadOnlySlot extends Slot {
        protected ReadOnlySlot(net.minecraft.inventory.Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeItems(net.minecraft.entity.player.PlayerEntity playerEntity) {
            return false;
        }
    }
}
