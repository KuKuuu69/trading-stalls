package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.TradingStalls;
import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.kukuuuu.tradingstalls.shop.OfferAvailability;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public class BuyerScreen extends AbstractContainerScreen<BuyerScreenHandler> {
    private static final Identifier BACKGROUND_TEXTURE =
            Identifier.fromNamespaceAndPath(TradingStalls.MOD_ID, "textures/gui/buyer_trading.png");
    private static final Identifier TRADE_CARD_TEXTURE =
            Identifier.fromNamespaceAndPath(TradingStalls.MOD_ID, "textures/gui/trade_card.png");

    public BuyerScreen(BuyerScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        imageWidth = 276;
        imageHeight = 166;
        inventoryLabelX = BuyerScreenHandler.PLAYER_INVENTORY_X;
        inventoryLabelY = BuyerScreenHandler.PLAYER_INVENTORY_Y - 11;
    }

    private void clickOffer(int offer) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, offer);
        }
    }

    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        ShopScreenRenderUtils.redrawStacksWithRaisedOverlay(context, font, menu.slots, leftPos, topPos);
        renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight,
                imageWidth, imageHeight);
        for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
            if (menu.getOfferAvailability(offer) == OfferAvailability.UNCONFIGURED) {
                continue;
            }
            int rowY = BuyerScreenHandler.OFFER_START_Y + offer * BuyerScreenHandler.OFFER_ROW_HEIGHT;
            context.blit(RenderPipelines.GUI_TEXTURED, TRADE_CARD_TEXTURE, leftPos + BuyerScreenHandler.OFFER_CARD_X, topPos + rowY,
                    0, 0, BuyerScreenHandler.OFFER_CARD_WIDTH, BuyerScreenHandler.OFFER_CARD_HEIGHT,
                    BuyerScreenHandler.OFFER_CARD_WIDTH, BuyerScreenHandler.OFFER_CARD_HEIGHT);
            drawOfferStatus(context, offer, rowY);
        }
    }

    @Override
    protected void renderLabels(@NonNull GuiGraphics context, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == 0) {
            for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
                int rowY = topPos + BuyerScreenHandler.OFFER_START_Y + offer * BuyerScreenHandler.OFFER_ROW_HEIGHT;
                if (isInsideTradeCard(click.x(), click.y(), rowY)
                        && menu.getOfferAvailability(offer) != OfferAvailability.UNCONFIGURED) {
                    clickOffer(offer);
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean isInsideTradeCard(double mouseX, double mouseY, int rowY) {
        return mouseX >= leftPos + BuyerScreenHandler.OFFER_CARD_X
                && mouseX < leftPos + BuyerScreenHandler.OFFER_CARD_X + BuyerScreenHandler.OFFER_CARD_WIDTH
                && mouseY >= rowY
                && mouseY < rowY + BuyerScreenHandler.OFFER_CARD_HEIGHT;
    }

    private void drawOfferStatus(GuiGraphics context, int offer, int rowY) {
        OfferAvailability availability = menu.getOfferAvailability(offer);
        if (availability != OfferAvailability.UNCONFIGURED) {
            int color = availability == OfferAvailability.AVAILABLE ? 0xFF2AAA35 : 0xFFB53A3A;
            context.fill(leftPos + BuyerScreenHandler.OFFER_CARD_X + 1, topPos + rowY + 2,
                    leftPos + BuyerScreenHandler.OFFER_CARD_X + 3, topPos + rowY + BuyerScreenHandler.OFFER_CARD_HEIGHT - 2,
                    color);
        }
        if (menu.getSelectedOffer() == offer) {
            drawBorder(context, leftPos + BuyerScreenHandler.OFFER_CARD_X, topPos + rowY,
                    BuyerScreenHandler.OFFER_CARD_WIDTH, BuyerScreenHandler.OFFER_CARD_HEIGHT, 0xFFFFFFFF);
        }
    }

    private void drawBorder(GuiGraphics context, int left, int top, int width, int height, int color) {
        context.fill(left, top, left + width, top + 1, color);
        context.fill(left, top + height - 1, left + width, top + height, color);
        context.fill(left, top, left + 1, top + height, color);
        context.fill(left + width - 1, top, left + width, top + height, color);
    }

}
