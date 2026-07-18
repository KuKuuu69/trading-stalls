package net.kukuuuu.tradingstalls.shop;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

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

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        nbt.put("Payment", payment.toNbtAllowEmpty(registries));
        nbt.put("Product", product.toNbtAllowEmpty(registries));
        return nbt;
    }

    public static TradeOfferData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        ItemStack payment = ItemStack.fromNbtOrEmpty(registries, nbt.getCompound("Payment"));
        ItemStack product = ItemStack.fromNbtOrEmpty(registries, nbt.getCompound("Product"));
        return new TradeOfferData(payment, product);
    }

    private static ItemStack copyTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(Math.min(stack.getCount(), stack.getMaxCount()));
    }
}
