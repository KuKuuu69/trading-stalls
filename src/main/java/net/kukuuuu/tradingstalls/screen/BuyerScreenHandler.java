package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.kukuuuu.tradingstalls.shop.OfferAvailability;
import net.kukuuuu.tradingstalls.shop.TradeOfferData;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BuyerScreenHandler extends BaseShopScreenHandler {
    public static final int OFFER_CARD_X = 5;
    public static final int OFFER_CARD_WIDTH = 88;
    public static final int OFFER_CARD_HEIGHT = 20;
    public static final int OFFER_PAYMENT_X = 73;
    public static final int OFFER_PRODUCT_X = 11;
    public static final int OFFER_START_Y = 18;
    public static final int OFFER_ROW_HEIGHT = 20;
    public static final int OFFER_SLOT_Y_OFFSET = 2;
    public static final int PAYMENT_X = 153;
    public static final int PAYMENT_Y = 37;
    public static final int OUTPUT_X = 210;
    public static final int OUTPUT_Y = 38;
    public static final int PLAYER_INVENTORY_X = 108;
    public static final int PLAYER_INVENTORY_Y = 84;

    private static final int TEMPLATE_SLOT_COUNT = TradingBlockEntity.OFFER_COUNT * 2;
    private static final int PAYMENT_SLOT = TEMPLATE_SLOT_COUNT;
    private static final int OUTPUT_SLOT = PAYMENT_SLOT + 1;
    private static final int PLAYER_START = OUTPUT_SLOT + 1;
    private static final int PLAYER_MAIN_END = PLAYER_START + 27;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final Container templates;
    private final SimpleContainer paymentInventory;
    private final SimpleContainer resultInventory;
    private final TradingBlockEntity tradingBlock;
    private final ContainerLevelAccess context;
    private final ContainerData properties;

    public BuyerScreenHandler(int syncId, Inventory playerInventory, ShopScreenData data) {
        this(syncId, playerInventory, new SimpleContainer(TEMPLATE_SLOT_COUNT), null,
                ContainerLevelAccess.NULL, new SimpleContainerData(8));
    }

    public BuyerScreenHandler(int syncId, Inventory playerInventory, TradingBlockEntity tradingBlock) {
        this(syncId, playerInventory, createTemplates(tradingBlock), tradingBlock,
                ContainerLevelAccess.create(tradingBlock.getLevel(), tradingBlock.getBlockPos()),
                new SimpleContainerData(8));
    }

    private BuyerScreenHandler(
            int syncId,
            Inventory playerInventory,
            Container templates,
            TradingBlockEntity tradingBlock,
            ContainerLevelAccess context,
            ContainerData properties
    ) {
        super(ModScreenHandlers.BUYER, syncId);
        this.templates = templates;
        this.tradingBlock = tradingBlock;
        this.context = context;
        this.properties = properties;
        this.paymentInventory = new SimpleContainer(1);
        this.resultInventory = new SimpleContainer(1);

        for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
            int slotY = OFFER_START_Y + offer * OFFER_ROW_HEIGHT + OFFER_SLOT_Y_OFFSET;
            addSlot(new ReadOnlySlot(templates, offer * 2, OFFER_PAYMENT_X, slotY));
            addSlot(new ReadOnlySlot(templates, offer * 2 + 1, OFFER_PRODUCT_X, slotY));
        }
        addSlot(new Slot(paymentInventory, 0, PAYMENT_X, PAYMENT_Y));
        addSlot(new OutputSlot(resultInventory, 0, OUTPUT_X, OUTPUT_Y));
        addPlayerInventory(playerInventory, PLAYER_INVENTORY_X, PLAYER_INVENTORY_Y);
        addDataSlots(properties);

        paymentInventory.addListener(ignored -> refreshOutput());
        updateProperties();
        refreshOutput();
    }

    private static Container createTemplates(TradingBlockEntity tradingBlock) {
        SimpleContainer templates = new SimpleContainer(TEMPLATE_SLOT_COUNT);
        for (int offerIndex = 0; offerIndex < TradingBlockEntity.OFFER_COUNT; offerIndex++) {
            TradeOfferData offer = tradingBlock.getOffer(offerIndex);
            templates.setItem(offerIndex * 2, offer.payment());
            templates.setItem(offerIndex * 2 + 1, offer.product());
        }
        return templates;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= TradingBlockEntity.OFFER_COUNT) {
            return false;
        }
        properties.set(1, id);
        refreshOutput();
        broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < TEMPLATE_SLOT_COUNT || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }
        if (slotIndex == OUTPUT_SLOT) {
            if (tradingBlock == null || !canTakeOutput(player)) {
                return ItemStack.EMPTY;
            }
            ItemStack product = tradingBlock.getOffer(getSelectedOffer()).product();
            if (!canFullyInsertIntoSlots(product, PLAYER_START, PLAYER_END)
                    || !tradingBlock.executeTrade(getSelectedOffer(), paymentInventory, 0)) {
                return ItemStack.EMPTY;
            }
            ItemStack moving = product.copy();
            moveItemStackTo(moving, PLAYER_START, PLAYER_END, true);
            refreshOutput();
            return product;
        }

        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (slotIndex == PAYMENT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= PLAYER_START) {
            if (!moveItemStackTo(stack, PAYMENT_SLOT, PAYMENT_SLOT + 1, false)) {
                if (slotIndex < PLAYER_MAIN_END) {
                    if (!moveItemStackTo(stack, PLAYER_MAIN_END, PLAYER_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, PLAYER_START, PLAYER_MAIN_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        refreshOutput();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return tradingBlock == null || stillValid(context, player, ModBlocks.TRADING_BLOCK);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        clearContainer(player, paymentInventory);
    }

    @Override
    public void broadcastChanges() {
        syncTemplatesFromBlock();
        updateProperties();
        refreshOutput();
        super.broadcastChanges();
    }

    public int getShopStatus() {
        return properties.get(0);
    }

    public int getSelectedOffer() {
        return properties.get(1);
    }

    public boolean isOfferAvailable(int offerIndex) {
        return getOfferAvailability(offerIndex) == OfferAvailability.AVAILABLE;
    }

    public OfferAvailability getOfferAvailability(int offerIndex) {
        OfferAvailability[] values = OfferAvailability.values();
        int availability = properties.get(2 + offerIndex);
        return values[Math.max(0, Math.min(availability, values.length - 1))];
    }

    private boolean canTakeOutput(Player player) {
        return tradingBlock != null
                && stillValid(player)
                && tradingBlock.canTrade(getSelectedOffer(), paymentInventory.getItem(0));
    }

    private void completeTrade(Player player) {
        if (canTakeOutput(player)) {
            tradingBlock.executeTrade(getSelectedOffer(), paymentInventory, 0);
            refreshOutput();
        }
    }

    private void refreshOutput() {
        if (tradingBlock == null) {
            return;
        }
        ItemStack payment = paymentInventory.getItem(0);
        resultInventory.setItem(0, tradingBlock.canTrade(getSelectedOffer(), payment)
                ? tradingBlock.getOffer(getSelectedOffer()).product()
                : ItemStack.EMPTY);
    }

    private void updateProperties() {
        if (tradingBlock == null) {
            return;
        }
        properties.set(0, tradingBlock.getShopStatus().ordinal());
        for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
            properties.set(2 + offer, tradingBlock.getOfferAvailability(offer).ordinal());
        }
    }

    private void syncTemplatesFromBlock() {
        if (tradingBlock == null) {
            return;
        }
        for (int offerIndex = 0; offerIndex < TradingBlockEntity.OFFER_COUNT; offerIndex++) {
            TradeOfferData offer = tradingBlock.getOffer(offerIndex);
            setTemplateIfChanged(offerIndex * 2, offer.payment());
            setTemplateIfChanged(offerIndex * 2 + 1, offer.product());
        }
    }

    private void setTemplateIfChanged(int slot, ItemStack stack) {
        if (!ItemStack.matches(templates.getItem(slot), stack)) {
            templates.setItem(slot, stack);
        }
    }

    private class OutputSlot extends Slot {
        private OutputSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return canTakeOutput(player);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            completeTrade(player);
            super.onTake(player, stack);
        }
    }
}
