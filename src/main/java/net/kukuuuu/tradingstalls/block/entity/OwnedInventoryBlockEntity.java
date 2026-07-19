package net.kukuuuu.tradingstalls.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public abstract class OwnedInventoryBlockEntity extends BlockEntity {
    public static final int INVENTORY_SIZE = 27;

    private final SimpleInventory inventory = new SimpleInventory(INVENTORY_SIZE);
    private UUID ownerUuid;

    protected OwnedInventoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory.addListener(ignored -> markDirty());
    }

    public SimpleInventory getInventory() {
        return inventory;
    }

    public boolean hasStoredItems() {
        for (int slot = 0; slot < inventory.size(); slot++) {
            if (!inventory.getStack(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOwner() {
        return ownerUuid != null;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public boolean isOwner(PlayerEntity player) {
        return ownerUuid != null && ownerUuid.equals(player.getUuid());
    }

    public boolean isOwnedBy(UUID uuid) {
        return ownerUuid != null && ownerUuid.equals(uuid);
    }

    public void setOwner(PlayerEntity player) {
        setOwner(player.getUuid());
    }

    public void setOwner(UUID uuid) {
        if (ownerUuid == null) {
            ownerUuid = uuid;
            markDirty();
        }
    }

    @Override
    protected void writeData(WriteView view) {
        view.putNullable("Owner", Uuids.CODEC, ownerUuid);

        inventory.toDataList(
                view.getListAppender("Inventory", ItemStack.CODEC)
        );
    }

    @Override
    protected void readData(ReadView view) {
        ownerUuid = view.read("Owner", Uuids.CODEC).orElse(null);

        view.getOptionalTypedListView("Inventory", ItemStack.CODEC)
                .ifPresent(inventory::readDataList);
    }

}
