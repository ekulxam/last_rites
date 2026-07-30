package survivalblock.last_rites.common.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import survivalblock.last_rites.common.LastRites;

public record SoulCircleS2CPayload(int count, float playerYaw, Vec3 pos) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, SoulCircleS2CPayload> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, payload -> payload.count,
            ByteBufCodecs.FLOAT, payload -> payload.playerYaw,
            Vec3.STREAM_CODEC, payload -> payload.pos,
            SoulCircleS2CPayload::new
    );
    public static final Type<SoulCircleS2CPayload> ID = new Type<>(LastRites.id("soul_circle_s2c"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
