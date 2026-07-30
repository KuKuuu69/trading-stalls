package net.kukuuuu.tradingstalls.screen;

import net.kukuuuu.tradingstalls.TradingStalls;
import net.kukuuuu.tradingstalls.block.entity.TradingBlockEntity;
import net.kukuuuu.tradingstalls.shop.ShopStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MerchantScreen extends AbstractContainerScreen<MerchantScreenHandler> {
    private static final Identifier BACKGROUND_TEXTURE =
            Identifier.fromNamespaceAndPath(TradingStalls.MOD_ID, "textures/gui/merchant_trading.png");
    private static final int VILLAGE_ICON_X = 254;
    private static final int STATUS_ICON_Y = 7;
    private static final int DRAWER_ICON_X = 266;
    private static final int ICON_SIZE = 7;

    public MerchantScreen(MerchantScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        imageWidth = 276;
        imageHeight = 166;
        inventoryLabelX = MerchantScreenHandler.PLAYER_INVENTORY_X;
        inventoryLabelY = MerchantScreenHandler.PLAYER_INVENTORY_Y - 11;
    }

    private void clickHandlerButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        ShopScreenRenderUtils.redrawStacksWithRaisedOverlay(context, font, menu.slots, leftPos, topPos);
        renderTooltip(context, mouseX, mouseY);
        drawStatusTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight,
                imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        drawVillageStatusIcon(context);
        drawDrawerStatusIcon(context);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
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
        int buttonY = topPos + yOffset + offer * MerchantScreenHandler.OFFER_ROW_HEIGHT;
        return mouseX >= leftPos + MerchantScreenHandler.SAVE_BUTTON_X
                && mouseX < leftPos + MerchantScreenHandler.SAVE_BUTTON_X + MerchantScreenHandler.BUTTON_WIDTH
                && mouseY >= buttonY
                && mouseY < buttonY + MerchantScreenHandler.BUTTON_HEIGHT;
    }

    private void drawDrawerStatusIcon(GuiGraphics context) {
        if (menu.getShopStatus() == ShopStatus.READY.ordinal()) {
            context.fill(DRAWER_ICON_X, STATUS_ICON_Y, DRAWER_ICON_X + 4, STATUS_ICON_Y + 4, 0xFF167A16);
            return;
        }
        drawRedX(context, DRAWER_ICON_X - 2, STATUS_ICON_Y - 2);
    }

    private void drawVillageStatusIcon(GuiGraphics context) {
        if (menu.isVillageConnected()) {
            context.fill(VILLAGE_ICON_X, STATUS_ICON_Y, VILLAGE_ICON_X + 4, STATUS_ICON_Y + 4, 0xFF167A16);
            return;
        }
        drawRedX(context, VILLAGE_ICON_X - 2, STATUS_ICON_Y - 2);
    }

    private void drawStatusTooltip(GuiGraphics context, int mouseX, int mouseY) {
        if (isMouseOverIcon(mouseX, mouseY, VILLAGE_ICON_X, STATUS_ICON_Y)) {
            context.setTooltipForNextFrame(font, Component.translatable(menu.isVillageConnected()
                    ? "tooltip.trading-stalls.village_connected"
                    : "tooltip.trading-stalls.no_nearby_village"), mouseX, mouseY);
            return;
        }
        if (isMouseOverIcon(mouseX, mouseY, DRAWER_ICON_X, STATUS_ICON_Y)) {
            context.setTooltipForNextFrame(font, statusText(menu.getShopStatus()), mouseX, mouseY);
        }
    }

    private boolean isMouseOverIcon(int mouseX, int mouseY, int iconX, int iconY) {
        return mouseX >= leftPos + iconX - 2
                && mouseX < leftPos + iconX - 2 + ICON_SIZE
                && mouseY >= topPos + iconY - 2
                && mouseY < topPos + iconY - 2 + ICON_SIZE;
    }

    private void drawRedX(GuiGraphics context, int left, int top) {
        int color = 0xFFA02020;
        context.fill(left, top, left + 2, top + 2, color);
        context.fill(left + 5, top, left + 7, top + 2, color);
        context.fill(left + 2, top + 2, left + 5, top + 5, color);
        context.fill(left, top + 5, left + 2, top + 7, color);
        context.fill(left + 5, top + 5, left + 7, top + 7, color);
    }

    static Component statusText(int statusIndex) {
        ShopStatus[] statuses = ShopStatus.values();
        ShopStatus status = statuses[Math.clamp(statusIndex, 0, statuses.length - 1)];
        return Component.translatable("status.trading-stalls." + status.name().toLowerCase());
    }

    static int statusColor(int statusIndex) {
        return statusIndex == ShopStatus.READY.ordinal() ? 0x167A16 : 0xA02020;
    }
}
