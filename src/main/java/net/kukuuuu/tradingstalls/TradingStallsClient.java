package net.kukuuuu.tradingstalls;

import net.fabricmc.api.ClientModInitializer;
import net.kukuuuu.tradingstalls.screen.BuyerScreen;
import net.kukuuuu.tradingstalls.screen.CashDrawerScreen;
import net.kukuuuu.tradingstalls.screen.MerchantScreen;
import net.kukuuuu.tradingstalls.screen.ModScreenHandlers;
import net.minecraft.client.gui.screens.MenuScreens;

public class TradingStallsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModScreenHandlers.MERCHANT, MerchantScreen::new);
        MenuScreens.register(ModScreenHandlers.BUYER, BuyerScreen::new);
        MenuScreens.register(ModScreenHandlers.CASH_DRAWER, CashDrawerScreen::new);

    }
}
