package net.kukuuuu.tradingstalls;

import net.fabricmc.api.ClientModInitializer;
import net.kukuuuu.tradingstalls.screen.BuyerScreen;
import net.kukuuuu.tradingstalls.screen.CashDrawerScreen;
import net.kukuuuu.tradingstalls.screen.MerchantScreen;
import net.kukuuuu.tradingstalls.screen.ModScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class TradingStallsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.MERCHANT, MerchantScreen::new);
        HandledScreens.register(ModScreenHandlers.BUYER, BuyerScreen::new);
        HandledScreens.register(ModScreenHandlers.CASH_DRAWER, CashDrawerScreen::new);

    }
}
