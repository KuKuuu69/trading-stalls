package net.kukuuuu.tradingstalls.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public abstract class BaseShopScreenHandler extends AbstractContainerMenu {
    protected BaseShopScreenHandler(MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    protected void addPlayerInventory(Inventory inventory, int x, int y) {
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
            ItemStack existing = slot.getItem();
            if (existing.isEmpty() && slot.mayPlace(stack)) {
                remaining -= slot.getMaxStackSize(stack);
            } else if (slot.mayPlace(stack) && ItemStack.isSameItemSameComponents(existing, stack)) {
                remaining -= Math.max(0, slot.getMaxStackSize(stack) - existing.getCount());
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    protected static class ReadOnlySlot extends Slot {
        protected ReadOnlySlot(net.minecraft.world.Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(net.minecraft.world.entity.player.@NonNull Player playerEntity) {
            return false;
        }
    }
}
