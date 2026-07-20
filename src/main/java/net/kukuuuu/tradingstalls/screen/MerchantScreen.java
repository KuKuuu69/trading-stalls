package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.TradingStalls;
import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.kukuuuu.tradingstalls.shop.ShopStatus;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MerchantScreen extends HandledScreen<MerchantScreenHandler> {
    private static final Identifier BACKGROUND_TEXTURE =
            Identifier.of(TradingStalls.MOD_ID, "textures/gui/merchant_trading.png");
    private static final int VILLAGE_ICON_X = 254;
    private static final int STATUS_ICON_Y = 7;
    private static final int DRAWER_ICON_X = 266;
    private static final int ICON_SIZE = 7;

    public MerchantScreen(MerchantScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 276;
        backgroundHeight = 166;
        playerInventoryTitleX = MerchantScreenHandler.PLAYER_INVENTORY_X;
        playerInventoryTitleY = MerchantScreenHandler.PLAYER_INVENTORY_Y - 11;
    }

    private void clickHandlerButton(int id) {
        if (client != null && client.interactionManager != null) {
            client.interactionManager.clickButton(handler.syncId, id);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        ShopScreenRenderUtils.redrawStacksWithRaisedOverlay(context, textRenderer, handler.slots, x, y);
        drawMouseoverTooltip(context, mouseX, mouseY);
        drawStatusTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight,
                backgroundWidth, backgroundHeight);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        drawVillageStatusIcon(context);
        drawDrawerStatusIcon(context);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            for (int offer = 0; offer < TradingBlockEntity.OFFER_COUNT; offer++) {
                if (isInsideButton(click.x(), click.y(), offer, MerchantScreenHandler.SAVE_BUTTON_Y_OFFSET)) {
                    clickHandlerButton(MerchantScreenHandler.SAVE_BUTTON_BASE + offer);
                    return true;
                }
                if (isInsideButton(click.x(), click.y(), offer, MerchantScreenHandler.CLEAR_BUTTON_Y_OFFSET)) {
                    clickHandlerButton(MerchantScreenHandler.CLEAR_BUTTON_BASE + offer);
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean isInsideButton(double mouseX, double mouseY, int offer, int yOffset) {
        int buttonY = y + yOffset + offer * MerchantScreenHandler.OFFER_ROW_HEIGHT;
        return mouseX >= x + MerchantScreenHandler.SAVE_BUTTON_X
                && mouseX < x + MerchantScreenHandler.SAVE_BUTTON_X + MerchantScreenHandler.BUTTON_WIDTH
                && mouseY >= buttonY
                && mouseY < buttonY + MerchantScreenHandler.BUTTON_HEIGHT;
    }

    private void drawDrawerStatusIcon(DrawContext context) {
        if (handler.getShopStatus() == ShopStatus.READY.ordinal()) {
            context.fill(DRAWER_ICON_X, STATUS_ICON_Y, DRAWER_ICON_X + 4, STATUS_ICON_Y + 4, 0xFF167A16);
            return;
        }
        drawRedX(context, DRAWER_ICON_X - 2, STATUS_ICON_Y - 2);
    }

    private void drawVillageStatusIcon(DrawContext context) {
        if (handler.isVillageConnected()) {
            context.fill(VILLAGE_ICON_X, STATUS_ICON_Y, VILLAGE_ICON_X + 4, STATUS_ICON_Y + 4, 0xFF167A16);
            return;
        }
        drawRedX(context, VILLAGE_ICON_X - 2, STATUS_ICON_Y - 2);
    }

    private void drawStatusTooltip(DrawContext context, int mouseX, int mouseY) {
        if (isMouseOverIcon(mouseX, mouseY, VILLAGE_ICON_X, STATUS_ICON_Y)) {
            context.drawTooltip(textRenderer, Text.translatable(handler.isVillageConnected()
                    ? "tooltip.trading-stalls.village_connected"
                    : "tooltip.trading-stalls.no_nearby_village"), mouseX, mouseY);
            return;
        }
        if (isMouseOverIcon(mouseX, mouseY, DRAWER_ICON_X, STATUS_ICON_Y)) {
            context.drawTooltip(textRenderer, statusText(handler.getShopStatus()), mouseX, mouseY);
        }
    }

    private boolean isMouseOverIcon(int mouseX, int mouseY, int iconX, int iconY) {
        return mouseX >= x + iconX - 2
                && mouseX < x + iconX - 2 + ICON_SIZE
                && mouseY >= y + iconY - 2
                && mouseY < y + iconY - 2 + ICON_SIZE;
    }

    private void drawRedX(DrawContext context, int left, int top) {
        int color = 0xFFA02020;
        context.fill(left, top, left + 2, top + 2, color);
        context.fill(left + 5, top, left + 7, top + 2, color);
        context.fill(left + 2, top + 2, left + 5, top + 5, color);
        context.fill(left, top + 5, left + 2, top + 7, color);
        context.fill(left + 5, top + 5, left + 7, top + 7, color);
    }

    static Text statusText(int statusIndex) {
        ShopStatus[] statuses = ShopStatus.values();
        ShopStatus status = statuses[Math.clamp(statusIndex, 0, statuses.length - 1)];
        return Text.translatable("status.trading-stalls." + status.name().toLowerCase());
    }

    static int statusColor(int statusIndex) {
        return statusIndex == ShopStatus.READY.ordinal() ? 0x167A16 : 0xA02020;
    }
}
