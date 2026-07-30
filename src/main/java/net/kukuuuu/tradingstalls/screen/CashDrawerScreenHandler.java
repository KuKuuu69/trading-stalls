package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.kukuuuu.tradingstalls.block.entity.CashDrawerBlockEntity;
import net.kukuuuu.tradingstalls.block.entity.OwnedInventoryBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class CashDrawerScreenHandler extends BaseShopScreenHandler {
    private static final int DRAWER_END = OwnedInventoryBlockEntity.INVENTORY_SIZE;
    private static final int PLAYER_START = DRAWER_END;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final CashDrawerBlockEntity drawer;
    private final ContainerLevelAccess context;

    public CashDrawerScreenHandler(int syncId, Inventory playerInventory, ShopScreenData data) {
        this(syncId, playerInventory, new SimpleContainer(OwnedInventoryBlockEntity.INVENTORY_SIZE),
                null, ContainerLevelAccess.NULL);
    }

    public CashDrawerScreenHandler(int syncId, Inventory playerInventory, CashDrawerBlockEntity drawer) {
        this(syncId, playerInventory, drawer.getInventory(), drawer,
                ContainerLevelAccess.create(Objects.requireNonNull(drawer.getLevel()), drawer.getBlockPos()));
    }

    private CashDrawerScreenHandler(
            int syncId,
            Inventory playerInventory,
            Container inventory,
            CashDrawerBlockEntity drawer,
            ContainerLevelAccess context
    ) {
        super(ModScreenHandlers.CASH_DRAWER, syncId);
        checkContainerSize(inventory, OwnedInventoryBlockEntity.INVENTORY_SIZE);
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
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (slotIndex < DRAWER_END) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, DRAWER_END, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return drawer == null
                || drawer.isOwner(player) && stillValid(context, player, ModBlocks.CASH_DRAWER);
    }
}
