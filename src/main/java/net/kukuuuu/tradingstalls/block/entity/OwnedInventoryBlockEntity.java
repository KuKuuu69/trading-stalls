package net.kukuuuu.tradingstalls.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
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
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        if (ownerUuid != null) {
            nbt.putUuid("Owner", ownerUuid);
        }
        nbt.put("Inventory", inventory.toNbtList(registries));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        ownerUuid = nbt.containsUuid("Owner") ? nbt.getUuid("Owner") : null;
        inventory.readNbtList(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), registries);
    }
}
