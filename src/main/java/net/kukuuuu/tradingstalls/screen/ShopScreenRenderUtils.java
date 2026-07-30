package net.kukuuuu.tradingstalls.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class ShopScreenRenderUtils {
    private static final int STACK_OVERLAY_Y_OFFSET = -1;

    private ShopScreenRenderUtils() {
    }

    static void redrawStacksWithRaisedOverlay(
            GuiGraphics context,
            Font textRenderer,
            Iterable<Slot> slots,
            int screenX,
            int screenY
    ) {
        for (Slot slot : slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            int itemX = screenX + slot.x;
            int itemY = screenY + slot.y;
            context.renderItem(stack, itemX, itemY);
            context.renderItemDecorations(textRenderer, stack, itemX, itemY + STACK_OVERLAY_Y_OFFSET);
        }
    }
}