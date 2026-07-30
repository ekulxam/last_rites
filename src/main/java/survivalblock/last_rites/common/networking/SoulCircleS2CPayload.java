package survivalblock.last_rites.common.networking;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

    public void sendToAllIncludingSelf(Entity self) {
        if (self instanceof ServerPlayer serverPlayer) {
            this.sendToAllIncludingSelf(serverPlayer);
            return;
        }
        PlayerLookup.tracking(self).forEach(player -> ServerPlayNetworking.send(player, this));
    }

    public void sendToAllIncludingSelf(ServerPlayer self) {
        ServerPlayNetworking.send(self, this);
        PlayerLookup.tracking(self).forEach(player -> ServerPlayNetworking.send(player, this));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
