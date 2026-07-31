package survivalblock.last_rites.common.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.gamerules.GameRule;
import survivalblock.atmosphere.registrar.delayed.DelayedRegistrant;
import survivalblock.last_rites.common.LastRites;

public final class LastRitesGameRules {
    private LastRitesGameRules() {
    }

    private static final DelayedRegistrant<GameRule<?>> REGISTRANT = new DelayedRegistrant<>(LastRites.MOD_ID, BuiltInRegistries.GAME_RULE);

    public static final GameRule<Boolean> URNS_STORE_EXPERIENCE = REGISTRANT.register(
            "urns_store_experience",
            GameRuleBuilder.forBoolean(true).build()
    );

    public static void init() {
        REGISTRANT.consumeAll();
    }
}
