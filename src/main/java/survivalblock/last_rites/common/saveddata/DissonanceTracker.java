package survivalblock.last_rites.common.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import survivalblock.last_rites.common.LastRites;
import survivalblock.last_rites.common.init.LastRitesAttachmentTypes;

import java.util.*;

public class DissonanceTracker extends SavedData {
    public static final Codec<Integer> DISSONANCE_CODEC = Codec.INT.validate(integer -> {
        if (integer > LastRites.MAX_DISSONANCE) {
            return DataResult.error(() -> "Dissonance cannot be greater than " + LastRites.MAX_DISSONANCE + " (provided " + integer + ")!");
        }
        if (integer < LastRites.MIN_DISSONANCE) {
            return DataResult.error(() -> "Dissonance cannot be less than " + LastRites.MIN_DISSONANCE + " (provided " + integer + ")!");
        }
        return DataResult.success(integer);
    });
    public static final Codec<DissonanceTracker> CODEC = Codec.unboundedMap(UUIDUtil.CODEC, DISSONANCE_CODEC)
            .xmap(DissonanceTracker::new, dissonanceTracker -> dissonanceTracker.dissonances);

    @SuppressWarnings("DataFlowIssue")
    private static final SavedDataType<DissonanceTracker> TYPE = new SavedDataType<>(
            LastRites.id("dissonance_tracker"),
            DissonanceTracker::new,
            CODEC,
            null
    );

    private final Map<UUID, Integer> dissonances = new HashMap<>();
    private final List<UUID> dirty = new ArrayList<>();

    public DissonanceTracker(Map<UUID, Integer> existing) {
        this.dissonances.putAll(existing);
    }

    public DissonanceTracker() {
    }

    public int getDissonance(UUID uuid) {
        return this.dissonances.computeIfAbsent(uuid, _ -> LastRites.MIN_DISSONANCE);
    }

    public void setDissonance(UUID uuid, int dissonance) {
        this.dissonances.put(uuid, Mth.clamp(dissonance, LastRites.MIN_DISSONANCE, LastRites.MAX_DISSONANCE));
    }

    public void changeDissonance(UUID uuid, int change) {
        Integer dissonance = this.dissonances.get(uuid);
        if (dissonance == null) {
            this.setDissonance(uuid, change);
        } else {
            this.setDissonance(uuid, dissonance + change);
        }
    }

    public void markDirty(ServerPlayer serverPlayer) {
        this.dirty.add(serverPlayer.getUUID());
    }

    public void serverTick(MinecraftServer server) {
        if (this.dirty.isEmpty()) {
            return;
        }

        for (UUID uuid : this.dirty) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);

            if (player == null) {
                continue;
            }

            player.setAttached(LastRitesAttachmentTypes.SYNCED_DISSONANCE, this.getDissonance(uuid));
        }
        
        this.dirty.clear();
    }

    public static DissonanceTracker get(ServerLevel serverLevel) {
        return get(serverLevel.getServer());
    }

    public static DissonanceTracker get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public static int getDissonance(ServerPlayer serverPlayer) {
        return get(serverPlayer.level()).getDissonance(serverPlayer.getUUID());
    }

    public static void changeDissonance(ServerPlayer serverPlayer, int change) {
        get(serverPlayer.level()).changeDissonance(serverPlayer.getUUID(), change);
    }
}
