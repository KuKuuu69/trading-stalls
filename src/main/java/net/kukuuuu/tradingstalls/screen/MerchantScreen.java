package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.kukuuuu.tradingstalls.shop.ShopStatus;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class MerchantScreen extends HandledScreen<MerchantScreenHandler> {
    public MerchantScreen(MerchantScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 252;
        backgroundHeight = 166;
        playerInventoryTitleX = 82;
        playerInventoryTitleY = 73;
    }

    @Override
    protected void init() {
        super.init();
        for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
            int row = offer;
            int buttonY = y + 18 + offer * 20;
            addDrawableChild(ButtonWidget.builder(Text.literal("S"), button -> clickHandlerButton(
                            MerchantScreenHandler.SAVE_BUTTON_BASE + row))
                    .dimensions(x + 52, buttonY, 13, 18)
                    .build());
            addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> clickHandlerButton(
                            MerchantScreenHandler.CLEAR_BUTTON_BASE + row))
                    .dimensions(x + 66, buttonY, 13, 18)
                    .build());
        }
    }

    private void clickHandlerButton(int id) {
        if (client != null && client.interactionManager != null) {
            client.interactionManager.clickButton(handler.syncId, id);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFFC6C6C6);
        context.fill(x + 3, y + 3, x + backgroundWidth - 3, y + backgroundHeight - 3, 0xFF8B8B8B);
        context.fill(x + 5, y + 5, x + backgroundWidth - 5, y + backgroundHeight - 5, 0xFFC6C6C6);
        for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
            drawSlotFrame(context, 8, 18 + offer * 20);
            drawSlotFrame(context, 30, 18 + offer * 20);
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlotFrame(context, 82 + column * 18, 18 + row * 18);
            }
        }
        drawPlayerSlotFrames(context, 82, 84);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
        context.drawText(textRenderer, Text.translatable("screen.trading-stalls.stock"), 82, 7, 0x404040, false);
        context.drawText(textRenderer, statusText(handler.getShopStatus()), 8, 144, statusColor(handler.getShopStatus()), false);
    }

    private void drawSlotFrame(DrawContext context, int slotX, int slotY) {
        context.fill(x + slotX - 1, y + slotY - 1, x + slotX + 17, y + slotY + 17, 0xFF373737);
        context.fill(x + slotX, y + slotY, x + slotX + 16, y + slotY + 16, 0xFF8B8B8B);
    }

    private void drawPlayerSlotFrames(DrawContext context, int startX, int startY) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlotFrame(context, startX + column * 18, startY + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlotFrame(context, startX + column * 18, startY + 58);
        }
    }

    static Text statusText(int statusIndex) {
        ShopStatus[] statuses = ShopStatus.values();
        ShopStatus status = statuses[Math.max(0, Math.min(statusIndex, statuses.length - 1))];
        return Text.translatable("status.trading-stalls." + status.name().toLowerCase());
    }

    static int statusColor(int statusIndex) {
        return statusIndex == ShopStatus.READY.ordinal() ? 0x167A16 : 0xA02020;
    }
}
