package net.kukuuuu.tradingstalls.block.entity;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class OwnedInventoryBlockEntity extends BlockEntity {
    public static final int INVENTORY_SIZE = 27;

    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);
    private UUID ownerUuid;

    protected OwnedInventoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory.addListener(ignored -> setChanged());
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public boolean hasStoredItems() {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) {
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

    public boolean isOwner(Player player) {
        return ownerUuid != null && ownerUuid.equals(player.getUUID());
    }

    public boolean isOwnedBy(UUID uuid) {
        return ownerUuid != null && ownerUuid.equals(uuid);
    }

    public void setOwner(Player player) {
        setOwner(player.getUUID());
    }

    public void setOwner(UUID uuid) {
        if (ownerUuid == null) {
            ownerUuid = uuid;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        view.storeNullable("Owner", UUIDUtil.AUTHLIB_CODEC, ownerUuid);

        inventory.storeAsItemList(
                view.list("Inventory", ItemStack.CODEC)
        );
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        ownerUuid = view.read("Owner", UUIDUtil.AUTHLIB_CODEC).orElse(null);

        view.list("Inventory", ItemStack.CODEC)
                .ifPresent(inventory::fromItemList);
    }

}
