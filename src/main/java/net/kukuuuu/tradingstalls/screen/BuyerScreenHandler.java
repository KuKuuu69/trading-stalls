package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.kukuuuu.tradingstalls.shop.OfferAvailability;
import net.kukuuuu.tradingstalls.shop.TradeOfferData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

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

    private final Inventory templates;
    private final SimpleInventory paymentInventory;
    private final SimpleInventory resultInventory;
    private final TradingBlockEntity tradingBlock;
    private final ScreenHandlerContext context;
    private final PropertyDelegate properties;

    public BuyerScreenHandler(int syncId, PlayerInventory playerInventory, ShopScreenData data) {
        this(syncId, playerInventory, new SimpleInventory(TEMPLATE_SLOT_COUNT), null,
                ScreenHandlerContext.EMPTY, new ArrayPropertyDelegate(8));
    }

    public BuyerScreenHandler(int syncId, PlayerInventory playerInventory, TradingBlockEntity tradingBlock) {
        this(syncId, playerInventory, createTemplates(tradingBlock), tradingBlock,
                ScreenHandlerContext.create(tradingBlock.getWorld(), tradingBlock.getPos()),
                new ArrayPropertyDelegate(8));
    }

    private BuyerScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory templates,
            TradingBlockEntity tradingBlock,
            ScreenHandlerContext context,
            PropertyDelegate properties
    ) {
        super(ModScreenHandlers.BUYER, syncId);
        this.templates = templates;
        this.tradingBlock = tradingBlock;
        this.context = context;
        this.properties = properties;
        this.paymentInventory = new SimpleInventory(1);
        this.resultInventory = new SimpleInventory(1);

        for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
            int slotY = OFFER_START_Y + offer * OFFER_ROW_HEIGHT + OFFER_SLOT_Y_OFFSET;
            addSlot(new ReadOnlySlot(templates, offer * 2, OFFER_PAYMENT_X, slotY));
            addSlot(new ReadOnlySlot(templates, offer * 2 + 1, OFFER_PRODUCT_X, slotY));
        }
        addSlot(new Slot(paymentInventory, 0, PAYMENT_X, PAYMENT_Y));
        addSlot(new OutputSlot(resultInventory, 0, OUTPUT_X, OUTPUT_Y));
        addPlayerInventory(playerInventory, PLAYER_INVENTORY_X, PLAYER_INVENTORY_Y);
        addProperties(properties);

        paymentInventory.addListener(ignored -> refreshOutput());
        updateProperties();
        refreshOutput();
    }

    private static Inventory createTemplates(TradingBlockEntity tradingBlock) {
        SimpleInventory templates = new SimpleInventory(TEMPLATE_SLOT_COUNT);
        for (int offerIndex = 0; offerIndex < TradingBlockEntity.OFFER_COUNT; offerIndex++) {
            TradeOfferData offer = tradingBlock.getOffer(offerIndex);
            templates.setStack(offerIndex * 2, offer.payment());
            templates.setStack(offerIndex * 2 + 1, offer.product());
        }
        return templates;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id < 0 || id >= TradingBlockEntity.OFFER_COUNT) {
            return false;
        }

        ItemStack requiredPayment = tradingBlock != null
                ? tradingBlock.getOffer(id).payment()
                : ItemStack.EMPTY;

        ItemStack currentPayment = paymentInventory.getStack(0);

        // If the payment slot has something that doesn't match this offer, return it to the player first.
        if (!currentPayment.isEmpty() && !ItemStack.areItemsAndComponentsEqual(currentPayment, requiredPayment)) {
            if (!player.giveItemStack(currentPayment.copy())) {
                player.dropItem(currentPayment.copy(), false);
            }
            paymentInventory.setStack(0, ItemStack.EMPTY);
            currentPayment = ItemStack.EMPTY;
        }

        // Auto-fill the payment slot from the player's inventory, up to the required amount.
        if (!requiredPayment.isEmpty()) {
            int needed = requiredPayment.getCount() - currentPayment.getCount();
            if (needed > 0) {
                withdrawFromPlayer(player, requiredPayment, needed);
            }
        }

        properties.set(1, id);
        refreshOutput();
        sendContentUpdates();
        return true;
    }

    private void withdrawFromPlayer(PlayerEntity player, ItemStack template, int amount) {
        PlayerInventory playerInv = player.getInventory();
        int remaining = amount;
        for (int slot = 0; slot < playerInv.size() && remaining > 0; slot++) {
            ItemStack stack = playerInv.getStack(slot);
            if (ItemStack.areItemsAndComponentsEqual(stack, template) && !stack.isEmpty()) {
                int taken = Math.min(remaining, stack.getCount());
                ItemStack current = paymentInventory.getStack(0);
                if (current.isEmpty()) {
                    paymentInventory.setStack(0, stack.split(taken));
                } else {
                    current.increment(taken);
                    stack.decrement(taken);
                }
                remaining -= taken;
                playerInv.markDirty();
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
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
            insertItem(moving, PLAYER_START, PLAYER_END, true);
            refreshOutput();
            return product;
        }

        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (slotIndex == PAYMENT_SLOT) {
            if (!insertItem(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= PLAYER_START) {
            if (!insertItem(stack, PAYMENT_SLOT, PAYMENT_SLOT + 1, false)) {
                if (slotIndex < PLAYER_MAIN_END) {
                    if (!insertItem(stack, PLAYER_MAIN_END, PLAYER_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!insertItem(stack, PLAYER_START, PLAYER_MAIN_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        refreshOutput();
        return original;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return tradingBlock == null || canUse(context, player, ModBlocks.TRADING_BLOCK);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        dropInventory(player, paymentInventory);
    }

    @Override
    public void sendContentUpdates() {
        syncTemplatesFromBlock();
        updateProperties();
        refreshOutput();
        super.sendContentUpdates();
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

    private boolean canTakeOutput(PlayerEntity player) {
        return tradingBlock != null
                && canUse(player)
                && tradingBlock.canTrade(getSelectedOffer(), paymentInventory.getStack(0));
    }

    private void completeTrade(PlayerEntity player) {
        if (canTakeOutput(player)) {
            tradingBlock.executeTrade(getSelectedOffer(), paymentInventory, 0);
            refreshOutput();
        }
    }

    private void refreshOutput() {
        if (tradingBlock == null) {
            return;
        }
        ItemStack payment = paymentInventory.getStack(0);
        resultInventory.setStack(0, tradingBlock.canTrade(getSelectedOffer(), payment)
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
        if (!ItemStack.areEqual(templates.getStack(slot), stack)) {
            templates.setStack(slot, stack);
        }
    }

    private class OutputSlot extends Slot {
        private OutputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity player) {
            return canTakeOutput(player);
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            completeTrade(player);
            super.onTakeItem(player, stack);
        }
    }
}
