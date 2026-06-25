package net.kukuuuu.tradingstalls.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CashDrawerScreen extends HandledScreen<CashDrawerScreenHandler> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");

    public CashDrawerScreen(CashDrawerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundHeight = 166;
        playerInventoryTitleY = 73;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, 71);
        context.drawTexture(TEXTURE, x, y + 71, 0, 126, backgroundWidth, 96);
    }
}
