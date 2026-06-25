package net.kukuuuu.tradingstalls.shop;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public final class InventoryUtils {
    private InventoryUtils() {
    }

    public static int countMatching(Inventory inventory, ItemStack template) {
        int count = 0;
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (ItemStack.areItemsAndComponentsEqual(stack, template)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean canInsertFully(Inventory inventory, ItemStack stack) {
        int remaining = stack.getCount();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack existing = inventory.getStack(slot);
            if (existing.isEmpty()) {
                remaining -= Math.min(inventory.getMaxCount(stack), stack.getMaxCount());
            } else if (ItemStack.areItemsAndComponentsEqual(existing, stack)) {
                remaining -= Math.max(0, Math.min(inventory.getMaxCount(existing), existing.getMaxCount()) - existing.getCount());
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    public static void insertFully(Inventory inventory, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < inventory.size() && !remaining.isEmpty(); slot++) {
            ItemStack existing = inventory.getStack(slot);
            if (!existing.isEmpty() && ItemStack.areItemsAndComponentsEqual(existing, remaining)) {
                int capacity = Math.min(inventory.getMaxCount(existing), existing.getMaxCount()) - existing.getCount();
                int moved = Math.min(capacity, remaining.getCount());
                if (moved > 0) {
                    existing.increment(moved);
                    remaining.decrement(moved);
                    inventory.markDirty();
                }
            }
        }
        for (int slot = 0; slot < inventory.size() && !remaining.isEmpty(); slot++) {
            if (inventory.getStack(slot).isEmpty()) {
                int moved = Math.min(remaining.getCount(), Math.min(inventory.getMaxCount(remaining), remaining.getMaxCount()));
                inventory.setStack(slot, remaining.split(moved));
            }
        }
        if (!remaining.isEmpty()) {
            throw new IllegalStateException("Inventory capacity changed during an atomic shop transaction");
        }
    }

    public static void removeMatching(Inventory inventory, ItemStack template, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inventory.size() && remaining > 0; slot++) {
            ItemStack existing = inventory.getStack(slot);
            if (ItemStack.areItemsAndComponentsEqual(existing, template)) {
                int removed = Math.min(remaining, existing.getCount());
                inventory.removeStack(slot, removed);
                remaining -= removed;
            }
        }
        if (remaining > 0) {
            throw new IllegalStateException("Shop stock changed during an atomic transaction");
        }
    }

}
