package net.kukuuuu.tradingstalls.shop;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class TradeOfferData {
    private final ItemStack payment;
    private final ItemStack product;

    public TradeOfferData(ItemStack payment, ItemStack product) {
        this.payment = copyTemplate(payment);
        this.product = copyTemplate(product);
    }

    public static TradeOfferData empty() {
        return new TradeOfferData(ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public ItemStack payment() {
        return payment.copy();
    }

    public ItemStack product() {
        return product.copy();
    }

    public boolean isEnabled() {
        return !payment.isEmpty() && !product.isEmpty();
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.store("Payment", ItemStack.OPTIONAL_CODEC, payment);
        nbt.store("Product", ItemStack.OPTIONAL_CODEC, product);
        return nbt;
    }

    public static TradeOfferData fromNbt(Optional<CompoundTag> nbt) {
        ItemStack payment = nbt.flatMap(n -> n.read("Payment", ItemStack.OPTIONAL_CODEC)).orElse(ItemStack.EMPTY);
        ItemStack product = nbt.flatMap(n -> n.read("Product", ItemStack.OPTIONAL_CODEC)).orElse(ItemStack.EMPTY);
        return new TradeOfferData(payment, product);
    }

    private static ItemStack copyTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
    }
}