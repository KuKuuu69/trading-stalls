package net.kukuuuu.tradingstalls.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CashDrawerScreen extends AbstractContainerScreen<CashDrawerScreenHandler> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

    public CashDrawerScreen(CashDrawerScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        imageHeight = 166;
        inventoryLabelY = 73;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, 71, 256, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos + 71, 0, 126, imageWidth, 96, 256, 256);
    }
}
