package net.kukuuuu.tradingstalls.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShopScreenData(BlockPos pos, boolean villageConnected) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ShopScreenData> PACKET_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ShopScreenData::pos,
            ByteBufCodecs.BOOL,
            ShopScreenData::villageConnected,
            ShopScreenData::new
    );
}
