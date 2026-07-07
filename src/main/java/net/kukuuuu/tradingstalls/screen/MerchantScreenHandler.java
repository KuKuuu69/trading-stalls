package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.kukuuuu.tradingstalls.block.entity.OwnedInventoryBlockEntity;
import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
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
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class MerchantScreenHandler extends BaseShopScreenHandler {
    public static final int SAVE_BUTTON_BASE = 0;
    public static final int CLEAR_BUTTON_BASE = 100;
    public static final int OFFER_PAYMENT_X = 53;
    public static final int OFFER_PRODUCT_X = 11;
    public static final int OFFER_START_Y = 22;
    public static final int OFFER_ROW_HEIGHT = 23;
    public static final int SAVE_BUTTON_X = 77;
    public static final int SAVE_BUTTON_Y_OFFSET = 22;
    public static final int CLEAR_BUTTON_X = 77;
    public static final int CLEAR_BUTTON_Y_OFFSET = 31;
    public static final int BUTTON_WIDTH = 9;
    public static final int BUTTON_HEIGHT = 7;
    public static final int STOCK_X = 108;
    public static final int STOCK_Y = 18;
    public static final int PLAYER_INVENTORY_X = 108;
    public static final int PLAYER_INVENTORY_Y = 84;

    private static final int TEMPLATE_SLOT_COUNT = TradingBlockEntity.OFFER_COUNT * 2;
    private static final int STOCK_START = TEMPLATE_SLOT_COUNT;
    private static final int PLAYER_START = STOCK_START + OwnedInventoryBlockEntity.INVENTORY_SIZE;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final Inventory templates;
    private final TradingBlockEntity tradingBlock;
    private final ScreenHandlerContext context;
    private final PropertyDelegate properties;

    public MerchantScreenHandler(int syncId, PlayerInventory playerInventory, ShopScreenData data) {
        this(syncId, playerInventory, new SimpleInventory(OwnedInventoryBlockEntity.INVENTORY_SIZE),
                new SimpleInventory(TEMPLATE_SLOT_COUNT), null, ScreenHandlerContext.EMPTY,
                createClientProperties(data));
    }

    public MerchantScreenHandler(int syncId, PlayerInventory playerInventory, TradingBlockEntity tradingBlock) {
        this(syncId, playerInventory, tradingBlock.getInventory(), createTemplates(tradingBlock), tradingBlock,
                ScreenHandlerContext.create(tradingBlock.getWorld(), tradingBlock.getPos()),
                new ArrayPropertyDelegate(2));
    }

    private MerchantScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory stock,
            Inventory templates,
            TradingBlockEntity tradingBlock,
            ScreenHandlerContext context,
            PropertyDelegate properties
    ) {
        super(ModScreenHandlers.MERCHANT, syncId);
        checkSize(stock, OwnedInventoryBlockEntity.INVENTORY_SIZE);
        checkSize(templates, TEMPLATE_SLOT_COUNT);
        this.templates = templates;
        this.tradingBlock = tradingBlock;
        this.context = context;
        this.properties = properties;

        for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
            int y = OFFER_START_Y + offer * OFFER_ROW_HEIGHT;
            addSlot(new ReadOnlySlot(templates, offer * 2, OFFER_PAYMENT_X, y));
            addSlot(new ReadOnlySlot(templates, offer * 2 + 1, OFFER_PRODUCT_X, y));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(stock, column + row * 9, STOCK_X + column * 18, STOCK_Y + row * 18));
            }
        }
        addPlayerInventory(playerInventory, PLAYER_INVENTORY_X, PLAYER_INVENTORY_Y);
        addProperties(properties);
        updateProperties();
    }

    private static PropertyDelegate createClientProperties(ShopScreenData data) {
        ArrayPropertyDelegate properties = new ArrayPropertyDelegate(2);
        properties.set(1, data.villageConnected() ? 1 : 0);
        return properties;
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
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < TEMPLATE_SLOT_COUNT) {
            if (actionType == SlotActionType.PICKUP) {
                ItemStack cursor = getCursorStack();
                templates.setStack(slotIndex, cursor.isEmpty()
                        ? ItemStack.EMPTY
                        : cursor.copyWithCount(Math.min(cursor.getCount(), cursor.getMaxCount())));
            }
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (tradingBlock == null || !tradingBlock.isOwner(player)) {
            return false;
        }
        if (id >= SAVE_BUTTON_BASE && id < SAVE_BUTTON_BASE + TradingBlockEntity.OFFER_COUNT) {
            int offerIndex = id - SAVE_BUTTON_BASE;
            ItemStack payment = templates.getStack(offerIndex * 2);
            ItemStack product = templates.getStack(offerIndex * 2 + 1);
            if (payment.isEmpty() || product.isEmpty()) {
                player.sendMessage(Text.translatable("message.trading-stalls.invalid_offer"), true);
                return false;
            }
            tradingBlock.setOffer(offerIndex, payment, product);
            sendContentUpdates();
            return true;
        }
        if (id >= CLEAR_BUTTON_BASE && id < CLEAR_BUTTON_BASE + TradingBlockEntity.OFFER_COUNT) {
            int offerIndex = id - CLEAR_BUTTON_BASE;
            tradingBlock.clearOffer(offerIndex);
            templates.setStack(offerIndex * 2, ItemStack.EMPTY);
            templates.setStack(offerIndex * 2 + 1, ItemStack.EMPTY);
            sendContentUpdates();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        if (slotIndex < TEMPLATE_SLOT_COUNT || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (slotIndex < PLAYER_START) {
            if (!insertItem(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!insertItem(stack, STOCK_START, PLAYER_START, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        return original;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return tradingBlock == null
                || tradingBlock.isOwner(player) && canUse(context, player, ModBlocks.TRADING_BLOCK);
    }

    @Override
    public void sendContentUpdates() {
        updateProperties();
        super.sendContentUpdates();
    }

    public int getShopStatus() {
        return properties.get(0);
    }

    public boolean isVillageConnected() {
        return properties.get(1) != 0;
    }

    private void updateProperties() {
        if (tradingBlock != null) {
            properties.set(0, tradingBlock.getShopStatus().ordinal());
            properties.set(1, tradingBlock.isVillageConnected() ? 1 : 0);
        }
    }
}
