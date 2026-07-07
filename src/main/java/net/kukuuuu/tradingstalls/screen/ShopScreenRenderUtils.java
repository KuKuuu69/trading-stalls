package net.kukuuuu.tradingstalls.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

final class ShopScreenRenderUtils {
    private static final int STACK_OVERLAY_Y_OFFSET = -1;

    private ShopScreenRenderUtils() {
    }

    static void redrawStacksWithRaisedOverlay(
            DrawContext context,
            TextRenderer textRenderer,
            Iterable<Slot> slots,
            int screenX,
            int screenY
    ) {
        for (Slot slot : slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) {
                continue;
            }
            int itemX = screenX + slot.x;
            int itemY = screenY + slot.y;
            context.drawItem(stack, itemX, itemY);
            context.drawItemInSlot(textRenderer, stack, itemX, itemY + STACK_OVERLAY_Y_OFFSET);
        }
    }
}
