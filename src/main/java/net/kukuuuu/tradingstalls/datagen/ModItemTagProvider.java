package net.kukuuuu.tradingstalls.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public static final TagKey<Item> VILLAGER_SELLABLE = TagKey.create(
            BuiltInRegistries.ITEM.key(),
            Identifier.fromNamespaceAndPath("trading-stalls", "villager_sellable")
    );

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider wrapperLookup) {
        valueLookupBuilder(VILLAGER_SELLABLE)
                .add(Items.COAL, Items.CHARCOAL, Items.RAW_IRON, Items.RAW_COPPER, Items.RAW_GOLD)
                .add(Items.EMERALD, Items.LAPIS_LAZULI, Items.DIAMOND, Items.QUARTZ, Items.AMETHYST_SHARD)
                .add(Items.COPPER_NUGGET, Items.IRON_NUGGET, Items.GOLD_NUGGET)
                .add(Items.IRON_INGOT, Items.COPPER_INGOT, Items.GOLD_INGOT)
                .add(Items.STICK, Items.FLINT, Items.WHEAT, Items.BONE, Items.BONE_MEAL)
                .add(Items.STRING, Items.FEATHER, Items.LEATHER, Items.RABBIT_HIDE)
                .add(Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS, Items.NAUTILUS_SHELL)
                .add(Items.BOWL, Items.BRICK, Items.PAPER, Items.BOOK)
                .add(Items.FIREWORK_STAR, Items.GLASS_BOTTLE, Items.NETHER_WART, Items.REDSTONE)
                .add(Items.GLOWSTONE_DUST, Items.GUNPOWDER)
                .add(Items.SUGAR, Items.RABBIT_FOOT, Items.GLISTERING_MELON_SLICE)
                .add(Items.SPIDER_EYE, Items.PUFFERFISH, Items.MAGMA_CREAM, Items.GOLDEN_CARROT)

                .add(Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE)
                .add(Items.MELON_SLICE, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.CHORUS_FRUIT)
                .add(Items.CARROT, Items.POTATO, Items.BAKED_POTATO)
                .add(Items.BEETROOT, Items.DRIED_KELP)

                .add(Items.BEEF, Items.COOKED_BEEF)
                .add(Items.PORKCHOP, Items.COOKED_PORKCHOP)
                .add(Items.MUTTON, Items.COOKED_MUTTON)
                .add(Items.CHICKEN, Items.COOKED_CHICKEN)
                .add(Items.RABBIT, Items.COOKED_RABBIT)
                .add(Items.COD, Items.COOKED_COD)
                .add(Items.SALMON, Items.COOKED_SALMON)
                .add(Items.TROPICAL_FISH)

                .add(Items.BREAD, Items.COOKIE, Items.CAKE, Items.PUMPKIN_PIE)
                .add(Items.MUSHROOM_STEW, Items.BEETROOT_SOUP, Items.RABBIT_STEW);
    }
}