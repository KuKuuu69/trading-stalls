package net.kukuuuu.tradingstalls.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.kukuuuu.tradingstalls.TradingStalls;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModScreenHandlers {
    public static ExtendedScreenHandlerType<MerchantScreenHandler, ShopScreenData> MERCHANT;
    public static ExtendedScreenHandlerType<BuyerScreenHandler, ShopScreenData> BUYER;
    public static ExtendedScreenHandlerType<CashDrawerScreenHandler, ShopScreenData> CASH_DRAWER;

    private ModScreenHandlers() {
    }

    public static void register() {
        MERCHANT = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(TradingStalls.MOD_ID, "merchant"),
                new ExtendedScreenHandlerType<>(MerchantScreenHandler::new, ShopScreenData.PACKET_CODEC)
        );
        BUYER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(TradingStalls.MOD_ID, "buyer"),
                new ExtendedScreenHandlerType<>(BuyerScreenHandler::new, ShopScreenData.PACKET_CODEC)
        );
        CASH_DRAWER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(TradingStalls.MOD_ID, "cash_drawer"),
                new ExtendedScreenHandlerType<>(CashDrawerScreenHandler::new, ShopScreenData.PACKET_CODEC)
        );
    }
}
