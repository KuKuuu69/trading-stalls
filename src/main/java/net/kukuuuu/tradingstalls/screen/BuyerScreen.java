package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.TradingStalls;
import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.kukuuuu.tradingstalls.shop.OfferAvailability;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BuyerScreen extends HandledScreen<BuyerScreenHandler> {
    private static final Identifier BACKGROUND_TEXTURE =
            Identifier.of(TradingStalls.MOD_ID, "textures/gui/buyer_trading.png");
    private static final Identifier TRADE_CARD_TEXTURE =
            Identifier.of(TradingStalls.MOD_ID, "textures/gui/trade_card.png");

    public BuyerScreen(BuyerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 276;
        backgroundHeight = 166;
        playerInventoryTitleX = BuyerScreenHandler.PLAYER_INVENTORY_X;
        playerInventoryTitleY = BuyerScreenHandler.PLAYER_INVENTORY_Y - 11;
    }

    private void clickOffer(int offer) {
        if (client != null && client.interactionManager != null) {
            client.interactionManager.clickButton(handler.syncId, offer);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        ShopScreenRenderUtils.redrawStacksWithRaisedOverlay(context, textRenderer, handler.slots, x, y);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight,
                backgroundWidth, backgroundHeight);
        for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
            if (handler.getOfferAvailability(offer) == OfferAvailability.UNCONFIGURED) {
                continue;
            }
            int rowY = BuyerScreenHandler.OFFER_START_Y + offer * BuyerScreenHandler.OFFER_ROW_HEIGHT;
            context.drawTexture(TRADE_CARD_TEXTURE, x + BuyerScreenHandler.OFFER_CARD_X, y + rowY,
                    0, 0, BuyerScreenHandler.OFFER_CARD_WIDTH, BuyerScreenHandler.OFFER_CARD_HEIGHT,
                    BuyerScreenHandler.OFFER_CARD_WIDTH, BuyerScreenHandler.OFFER_CARD_HEIGHT);
            drawOfferStatus(context, offer, rowY);
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
                int rowY = y + BuyerScreenHandler.OFFER_START_Y + offer * BuyerScreenHandler.OFFER_ROW_HEIGHT;
                if (isInsideTradeCard(mouseX, mouseY, rowY)
                        && handler.getOfferAvailability(offer) != OfferAvailability.UNCONFIGURED) {
                    clickOffer(offer);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isInsideTradeCard(double mouseX, double mouseY, int rowY) {
        return mouseX >= x + BuyerScreenHandler.OFFER_CARD_X
                && mouseX < x + BuyerScreenHandler.OFFER_CARD_X + BuyerScreenHandler.OFFER_CARD_WIDTH
                && mouseY >= rowY
                && mouseY < rowY + BuyerScreenHandler.OFFER_CARD_HEIGHT;
    }

    private void drawOfferStatus(DrawContext context, int offer, int rowY) {
        OfferAvailability availability = handler.getOfferAvailability(offer);
        if (availability != OfferAvailability.UNCONFIGURED) {
            int color = availability == OfferAvailability.AVAILABLE ? 0xFF2AAA35 : 0xFFB53A3A;
            context.fill(x + BuyerScreenHandler.OFFER_CARD_X + 1, y + rowY + 2,
                    x + BuyerScreenHandler.OFFER_CARD_X + 3, y + rowY + BuyerScreenHandler.OFFER_CARD_HEIGHT - 2,
                    color);
        }
        if (handler.getSelectedOffer() == offer) {
            drawBorder(context, x + BuyerScreenHandler.OFFER_CARD_X, y + rowY,
                    BuyerScreenHandler.OFFER_CARD_WIDTH, BuyerScreenHandler.OFFER_CARD_HEIGHT, 0xFFFFFFFF);
        }
    }

    private void drawBorder(DrawContext context, int left, int top, int width, int height, int color) {
        context.fill(left, top, left + width, top + 1, color);
        context.fill(left, top + height - 1, left + width, top + height, color);
        context.fill(left, top, left + 1, top + height, color);
        context.fill(left + width - 1, top, left + width, top + height, color);
    }

}
