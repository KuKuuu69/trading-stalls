package net.kukuuuu.tradingstalls.screen;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;

public record ShopScreenData(BlockPos pos, boolean villageConnected) {
    public static final PacketCodec<RegistryByteBuf, ShopScreenData> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            ShopScreenData::pos,
            PacketCodecs.BOOLEAN,
            ShopScreenData::villageConnected,
            ShopScreenData::new
    );
}
