package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.block.ModBlocks;
import net.kukuuuu.tradingstalls.block.entity.OwnedInventoryBlockEntity;
import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.kukuuuu.tradingstalls.shop.TradeOfferData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

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

    private final Container templates;
    private final TradingBlockEntity tradingBlock;
    private final ContainerLevelAccess context;
    private final ContainerData properties;

    public MerchantScreenHandler(int syncId, Inventory playerInventory, ShopScreenData data) {
        this(syncId, playerInventory, new SimpleContainer(OwnedInventoryBlockEntity.INVENTORY_SIZE),
                new SimpleContainer(TEMPLATE_SLOT_COUNT), null, ContainerLevelAccess.NULL,
                createClientProperties(data));
    }

    public MerchantScreenHandler(int syncId, Inventory playerInventory, TradingBlockEntity tradingBlock) {
        this(syncId, playerInventory, tradingBlock.getInventory(), createTemplates(tradingBlock), tradingBlock,
                ContainerLevelAccess.create(tradingBlock.getLevel(), tradingBlock.getBlockPos()),
                new SimpleContainerData(2));
    }

    private MerchantScreenHandler(
            int syncId,
            Inventory playerInventory,
            Container stock,
            Container templates,
            TradingBlockEntity tradingBlock,
            ContainerLevelAccess context,
            ContainerData properties
    ) {
        super(ModScreenHandlers.MERCHANT, syncId);
        checkContainerSize(stock, OwnedInventoryBlockEntity.INVENTORY_SIZE);
        checkContainerSize(templates, TEMPLATE_SLOT_COUNT);
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
        addDataSlots(properties);
        updateProperties();
    }

    private static ContainerData createClientProperties(ShopScreenData data) {
        SimpleContainerData properties = new SimpleContainerData(2);
        properties.set(1, data.villageConnected() ? 1 : 0);
        return properties;
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
    public void clicked(int slotIndex, int button, ClickType actionType, Player player) {
        if (slotIndex >= 0 && slotIndex < TEMPLATE_SLOT_COUNT) {
            if (actionType == ClickType.PICKUP) {
                ItemStack cursor = getCarried();
                templates.setItem(slotIndex, cursor.isEmpty()
                        ? ItemStack.EMPTY
                        : cursor.copyWithCount(Math.min(cursor.getCount(), cursor.getMaxStackSize())));
            }
            return;
        }
        super.clicked(slotIndex, button, actionType, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (tradingBlock == null || !tradingBlock.isOwner(player)) {
            return false;
        }
        if (id >= SAVE_BUTTON_BASE && id < SAVE_BUTTON_BASE + TradingBlockEntity.OFFER_COUNT) {
            int offerIndex = id - SAVE_BUTTON_BASE;
            ItemStack payment = templates.getItem(offerIndex * 2);
            ItemStack product = templates.getItem(offerIndex * 2 + 1);
            if (payment.isEmpty() || product.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.trading-stalls.invalid_offer"), true);
                return false;
            }
            tradingBlock.setOffer(offerIndex, payment, product);
            broadcastChanges();
            return true;
        }
        if (id >= CLEAR_BUTTON_BASE && id < CLEAR_BUTTON_BASE + TradingBlockEntity.OFFER_COUNT) {
            int offerIndex = id - CLEAR_BUTTON_BASE;
            tradingBlock.clearOffer(offerIndex);
            templates.setItem(offerIndex * 2, ItemStack.EMPTY);
            templates.setItem(offerIndex * 2 + 1, ItemStack.EMPTY);
            broadcastChanges();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < TEMPLATE_SLOT_COUNT || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (slotIndex < PLAYER_START) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, STOCK_START, PLAYER_START, false)) {
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
        return tradingBlock == null
                || tradingBlock.isOwner(player) && stillValid(context, player, ModBlocks.TRADING_BLOCK);
    }

    @Override
    public void broadcastChanges() {
        updateProperties();
        super.broadcastChanges();
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
