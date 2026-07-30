package survivalblock.last_rites.common.init;

import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.component.DataComponentType;
import survivalblock.atmosphere.registrar.delayed.DelayedDataComponentTypeRegistrant;
import survivalblock.last_rites.common.LastRites;
import survivalblock.last_rites.common.component.item.CineraryBladeComponent;

public final class LastRitesDataComponentTypes {
    private LastRitesDataComponentTypes() {
    }

    private static final DelayedDataComponentTypeRegistrant REGISTRANT = new DelayedDataComponentTypeRegistrant(LastRites.MOD_ID);
    public static final DataComponentType<CineraryBladeComponent> CINERARY_BLADE = REGISTRANT.register(
            "cinerary_blade",
            DataComponentType.<CineraryBladeComponent>builder()
                    .persistent(CineraryBladeComponent.CODEC)
                    .networkSynchronized(CineraryBladeComponent.STREAM_CODEC)
                    .ignoreSwapAnimation()
                    .build()
    );

    public static void init() {
        REGISTRANT.consumeAll();
        ItemComponentTooltipProviderRegistry.addFirst(CINERARY_BLADE);
    }
}
