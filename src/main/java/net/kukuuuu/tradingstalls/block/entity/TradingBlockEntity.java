package net.kukuuuu.tradingstalls.block.entity;

import net.kukuuuu.tradingstalls.shop.InventoryUtils;
import net.kukuuuu.tradingstalls.shop.OfferAvailability;
import net.kukuuuu.tradingstalls.shop.ShopStatus;
import net.kukuuuu.tradingstalls.shop.TradeOfferData;
import net.kukuuuu.tradingstalls.shop.VillagerShopVisits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.Optional;
import net.kukuuuu.tradingstalls.datagen.ModItemTagProvider;

import java.util.ArrayList;
import java.util.List;

public class TradingBlockEntity extends OwnedInventoryBlockEntity {
    public static final int OFFER_COUNT = 6;
    private static final int MAX_VILLAGER_EMERALD_COST = 5;

    private final List<TradeOfferData> offers = new ArrayList<>(OFFER_COUNT);
    private final VillagerShopVisits.State villagerVisits = new VillagerShopVisits.State();

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
        setChanged();
    }

    public void clearOffer(int index) {
        offers.set(index, TradeOfferData.empty());
        setChanged();
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, TradingBlockEntity tradingBlock) {
        if (world instanceof ServerLevel serverWorld) {
            tradingBlock.villagerVisits.tick(serverWorld, tradingBlock);
        }
    }

    public boolean isVillageConnected() {
        return level instanceof ServerLevel serverWorld
                && VillagerShopVisits.hasNearbyVillage(serverWorld, worldPosition);
    }

    public ShopStatus getShopStatus() {
        if (level == null || !hasOwner()) {
            return ShopStatus.MISSING_DRAWER;
        }

        int matchingDrawers = 0;
        boolean foundWrongOwner = false;
        for (Direction direction :new Direction[]{Direction.DOWN}) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof CashDrawerBlockEntity drawer) {
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
        return ItemStack.isSameItemSameComponents(required, suppliedPayment)
                && suppliedPayment.getCount() >= required.getCount();
    }

    public boolean executeTrade(int index, Container paymentInventory, int paymentSlot) {
        ItemStack suppliedPayment = paymentInventory.getItem(paymentSlot);
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
        paymentInventory.removeItem(paymentSlot, payment.getCount());
        syncInventoryChange();
        syncBlockEntity(drawer);
        return true;
    }

    public boolean hasVillagerOffer() {
        for (int index = 0; index < OFFER_COUNT; index++) {
            if (canVillagerBuy(index)) {
                return true;
            }
        }
        return false;
    }

    public boolean executeRandomVillagerTrade(RandomSource random) {
        List<Integer> offerIndexes = new ArrayList<>();
        for (int index = 0; index < OFFER_COUNT; index++) {
            if (canVillagerBuy(index)) {
                offerIndexes.add(index);
            }
        }
        if (offerIndexes.isEmpty()) {
            return false;
        }

        int offerIndex = offerIndexes.get(random.nextInt(offerIndexes.size()));
        SimpleContainer generatedPayment = new SimpleContainer(1);
        generatedPayment.setItem(0, offers.get(offerIndex).payment());
        return executeTrade(offerIndex, generatedPayment, 0);
    }

    private boolean canVillagerBuy(int index) {
        TradeOfferData offer = offers.get(index);
        return offer.isEnabled()
                && offer.payment().is(Items.EMERALD)
                && offer.payment().getCount() <= MAX_VILLAGER_EMERALD_COST
                && offer.product().is(ModItemTagProvider.VILLAGER_SELLABLE)
                && getOfferAvailability(index) == OfferAvailability.AVAILABLE;
    }

    private void syncInventoryChange() {
        setChanged();
        syncBlockEntity(this);
    }

    private void syncBlockEntity(OwnedInventoryBlockEntity blockEntity) {
        if (level != null) {
            level.sendBlockUpdated(
                    blockEntity.getBlockPos(),
                    blockEntity.getBlockState(),
                    blockEntity.getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    private CashDrawerBlockEntity findLinkedDrawer() {
        if (level == null || getShopStatus() != ShopStatus.READY) {
            return null;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof CashDrawerBlockEntity drawer
                    && drawer.isOwnedBy(getOwnerUuid())) {
                return drawer;
            }
        }
        return null;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);

        ValueOutput.TypedOutputList<CompoundTag> appender =
                view.list("Offers", CompoundTag.CODEC);

        for (TradeOfferData offer : offers) {
            appender.add(offer.toNbt());
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);

        offers.clear();

        view.list("Offers", CompoundTag.CODEC)
                .ifPresent(list -> {
                    for (CompoundTag compound : list) {
                        offers.add(TradeOfferData.fromNbt(Optional.of(compound)));
                    }
                });

        while (offers.size() < OFFER_COUNT) {
            offers.add(TradeOfferData.empty());
        }
    }
}
