package net.kukuuuu.tradingstalls.block.entity;

import net.kukuuuu.tradingstalls.shop.InventoryUtils;
import net.kukuuuu.tradingstalls.shop.OfferAvailability;
import net.kukuuuu.tradingstalls.shop.ShopStatus;
import net.kukuuuu.tradingstalls.shop.TradeOfferData;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class TradingBlockEntity extends OwnedInventoryBlockEntity {
    public static final int OFFER_COUNT = 6;

    private final List<TradeOfferData> offers = new ArrayList<>(OFFER_COUNT);

    public TradingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRADING_BLOCK_ENTITY, pos, state);
        for (int index = 0; index < OFFER_COUNT; index++) {
            offers.add(TradeOfferData.empty());
        }
    }

    public TradeOfferData getOffer(int index) {
        return offers.get(index);
    }

    public void setOffer(int index, ItemStack payment, ItemStack product) {
        offers.set(index, new TradeOfferData(payment, product));
        markDirty();
    }

    public void clearOffer(int index) {
        offers.set(index, TradeOfferData.empty());
        markDirty();
    }

    public ShopStatus getShopStatus() {
        if (world == null || !hasOwner()) {
            return ShopStatus.MISSING_DRAWER;
        }

        int matchingDrawers = 0;
        boolean foundWrongOwner = false;
        for (Direction direction : Direction.values()) {
            if (world.getBlockEntity(pos.offset(direction)) instanceof CashDrawerBlockEntity drawer) {
                if (drawer.isOwnedBy(getOwnerUuid())) {
                    matchingDrawers++;
                } else {
                    foundWrongOwner = true;
                }
            }
        }

        if (matchingDrawers > 1) {
            return ShopStatus.MULTIPLE_DRAWERS;
        }
        if (matchingDrawers == 1) {
            return ShopStatus.READY;
        }
        return foundWrongOwner ? ShopStatus.WRONG_OWNER : ShopStatus.MISSING_DRAWER;
    }

    public boolean isOfferAvailable(int index) {
        return getOfferAvailability(index) == OfferAvailability.AVAILABLE;
    }

    public OfferAvailability getOfferAvailability(int index) {
        TradeOfferData offer = offers.get(index);
        if (!offer.isEnabled()) {
            return OfferAvailability.UNCONFIGURED;
        }
        if (getShopStatus() != ShopStatus.READY) {
            return OfferAvailability.DRAWER_UNAVAILABLE;
        }
        CashDrawerBlockEntity drawer = findLinkedDrawer();
        if (drawer == null) {
            return OfferAvailability.DRAWER_UNAVAILABLE;
        }
        if (InventoryUtils.countMatching(getInventory(), offer.product()) < offer.product().getCount()) {
            return OfferAvailability.OUT_OF_STOCK;
        }
        if (!drawer.canAccept(offer.payment())) {
            return OfferAvailability.DRAWER_FULL;
        }
        return OfferAvailability.AVAILABLE;
    }

    public boolean canTrade(int index, ItemStack suppliedPayment) {
        if (!isOfferAvailable(index)) {
            return false;
        }
        ItemStack required = offers.get(index).payment();
        return ItemStack.areItemsAndComponentsEqual(required, suppliedPayment)
                && suppliedPayment.getCount() >= required.getCount();
    }

    public boolean executeTrade(int index, Inventory paymentInventory, int paymentSlot) {
        ItemStack suppliedPayment = paymentInventory.getStack(paymentSlot);
        if (!canTrade(index, suppliedPayment)) {
            return false;
        }

        TradeOfferData offer = offers.get(index);
        CashDrawerBlockEntity drawer = findLinkedDrawer();
        if (drawer == null) {
            return false;
        }

        ItemStack payment = offer.payment();
        ItemStack product = offer.product();
        InventoryUtils.removeMatching(getInventory(), product, product.getCount());
        drawer.accept(payment);
        paymentInventory.removeStack(paymentSlot, payment.getCount());
        markDirty();
        return true;
    }

    private CashDrawerBlockEntity findLinkedDrawer() {
        if (world == null || getShopStatus() != ShopStatus.READY) {
            return null;
        }
        for (Direction direction : Direction.values()) {
            if (world.getBlockEntity(pos.offset(direction)) instanceof CashDrawerBlockEntity drawer
                    && drawer.isOwnedBy(getOwnerUuid())) {
                return drawer;
            }
        }
        return null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        NbtList offerList = new NbtList();
        for (TradeOfferData offer : offers) {
            offerList.add(offer.toNbt(registries));
        }
        nbt.put("Offers", offerList);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        NbtList offerList = nbt.getList("Offers", NbtElement.COMPOUND_TYPE);
        for (int index = 0; index < OFFER_COUNT; index++) {
            offers.set(index, index < offerList.size()
                    ? TradeOfferData.fromNbt(offerList.getCompound(index), registries)
                    : TradeOfferData.empty());
        }
    }
}
