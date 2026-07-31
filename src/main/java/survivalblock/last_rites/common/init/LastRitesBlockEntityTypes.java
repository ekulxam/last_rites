package survivalblock.last_rites.common.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import survivalblock.atmosphere.registrar.delayed.DelayedBlockEntityTypeRegistrant;
import survivalblock.last_rites.common.LastRites;
import survivalblock.last_rites.common.block.entity.CanopicUrnBlockEntity;

public final class LastRitesBlockEntityTypes {
    private LastRitesBlockEntityTypes() {
    }

    private static final DelayedBlockEntityTypeRegistrant REGISTRANT = new DelayedBlockEntityTypeRegistrant(LastRites.MOD_ID);
    public static final BlockEntityType<CanopicUrnBlockEntity> CINERARY_URN =
            REGISTRANT.register(
                    "canopic_urn",
                    FabricBlockEntityTypeBuilder.create(
                            CanopicUrnBlockEntity::new,
                            LastRitesBlocks.CANOPIC_URN
                    ).build()
            );

    public static void init() {
        REGISTRANT.consumeAll();
    }
}
