package net.kukuuuu.tradingstalls.shop;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class InventoryUtils {
    private InventoryUtils() {
    }

    public static int countMatching(Container inventory, ItemStack template) {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack, template)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean canInsertFully(Container inventory, ItemStack stack) {
        int remaining = stack.getCount();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (existing.isEmpty()) {
                remaining -= Math.min(inventory.getMaxStackSize(stack), stack.getMaxStackSize());
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                remaining -= Math.max(0, Math.min(inventory.getMaxStackSize(existing), existing.getMaxStackSize()) - existing.getCount());
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    public static void insertFully(Container inventory, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < inventory.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, remaining)) {
                int capacity = Math.min(inventory.getMaxStackSize(existing), existing.getMaxStackSize()) - existing.getCount();
                int moved = Math.min(capacity, remaining.getCount());
                if (moved > 0) {
                    existing.grow(moved);
                    remaining.shrink(moved);
                    inventory.setChanged();
                }
            }
        }
        for (int slot = 0; slot < inventory.getContainerSize() && !remaining.isEmpty(); slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                int moved = Math.min(remaining.getCount(), Math.min(inventory.getMaxStackSize(remaining), remaining.getMaxStackSize()));
                inventory.setItem(slot, remaining.split(moved));
            }
        }
        if (!remaining.isEmpty()) {
            throw new IllegalStateException("Inventory capacity changed during an atomic shop transaction");
        }
    }

    public static void removeMatching(Container inventory, ItemStack template, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (ItemStack.isSameItemSameComponents(existing, template)) {
                int removed = Math.min(remaining, existing.getCount());
                inventory.removeItem(slot, removed);
                remaining -= removed;
            }
        }
        if (remaining > 0) {
            throw new IllegalStateException("Shop stock changed during an atomic transaction");
        }
    }

}
