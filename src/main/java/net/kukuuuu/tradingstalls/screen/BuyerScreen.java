package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.kukuuuu.tradingstalls.shop.OfferAvailability;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class BuyerScreen extends HandledScreen<BuyerScreenHandler> {
    private final ButtonWidget[] offerButtons = new ButtonWidget[TradingBlockEntity.OFFER_COUNT];

    public BuyerScreen(BuyerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 214;
        backgroundHeight = 232;
        playerInventoryTitleX = 40;
        playerInventoryTitleY = 139;
    }

    @Override
    protected void init() {
        super.init();
        for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
            int offerIndex = offer;
            offerButtons[offer] = addDrawableChild(ButtonWidget.builder(Text.literal(Integer.toString(offer + 1)),
                            button -> clickOffer(offerIndex))
                    .dimensions(x + 8, y + 18 + offer * 20, 28, 18)
                    .build());
        }
    }

    private void clickOffer(int offer) {
        if (client != null && client.interactionManager != null) {
            client.interactionManager.clickButton(handler.syncId, offer);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (int offer = 0; offer < offerButtons.length; offer++) {
            offerButtons[offer].active = handler.getOfferAvailability(offer) != OfferAvailability.UNCONFIGURED;
            offerButtons[offer].setMessage(Text.literal((handler.getSelectedOffer() == offer ? "> " : "") + (offer + 1)));
        }
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFFC6C6C6);
        context.fill(x + 3, y + 3, x + backgroundWidth - 3, y + backgroundHeight - 3, 0xFF8B8B8B);
        context.fill(x + 5, y + 5, x + backgroundWidth - 5, y + backgroundHeight - 5, 0xFFC6C6C6);
        for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
            drawSlotFrame(context, 40, 18 + offer * 20);
            drawSlotFrame(context, 62, 18 + offer * 20);
        }
        drawSlotFrame(context, 112, 48);
        drawSlotFrame(context, 166, 48);
        drawPlayerSlotFrames(context, 40, 150);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
        context.drawText(textRenderer, Text.translatable("screen.trading-stalls.pay"), 108, 36, 0x404040, false);
        context.drawText(textRenderer, Text.literal(">"), 142, 52, 0x404040, false);
        context.drawText(textRenderer, Text.translatable("screen.trading-stalls.receive"), 158, 36, 0x404040, false);
        context.drawText(textRenderer, MerchantScreen.statusText(handler.getShopStatus()), 100, 76,
                MerchantScreen.statusColor(handler.getShopStatus()), false);

        for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
            OfferAvailability availability = handler.getOfferAvailability(offer);
            context.drawText(textRenderer, availabilityMarker(availability), 84, 23 + offer * 20,
                    availabilityColor(availability), false);
        }

        OfferAvailability selectedAvailability = handler.getOfferAvailability(handler.getSelectedOffer());
        context.drawText(textRenderer, availabilityText(selectedAvailability), 100, 96,
                availabilityColor(selectedAvailability), false);
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

    private Text availabilityMarker(OfferAvailability availability) {
        return Text.literal(switch (availability) {
            case AVAILABLE -> "OK";
            case UNCONFIGURED -> "-";
            default -> "X";
        });
    }

    private Text availabilityText(OfferAvailability availability) {
        return Text.translatable("availability.trading-stalls." + availability.name().toLowerCase());
    }

    private int availabilityColor(OfferAvailability availability) {
        return switch (availability) {
            case AVAILABLE -> 0x167A16;
            case UNCONFIGURED -> 0x606060;
            default -> 0xA02020;
        };
    }
}
