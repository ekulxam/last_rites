/*
 * Copyright (c) 2026-present ekulxam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package survivalblock.last_rites.common.init;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import survivalblock.atmosphere.registrar.delayed.DelayedStatusEffectRegistrant;
import survivalblock.last_rites.common.LastRites;

public final class LastRitesStatusEffects {
    private LastRitesStatusEffects() {
    }

    private static final DelayedStatusEffectRegistrant REGISTRANT = new DelayedStatusEffectRegistrant(LastRites.MOD_ID);

    public static final Holder.Reference<MobEffect> DISSONANCE = REGISTRANT.registerReference(
            "dissonance",
            new StatusEffect(MobEffectCategory.HARMFUL, 0x64DBE3)
    );

    public static void init() {
        REGISTRANT.consumeAll();
    }

    public static class StatusEffect extends MobEffect {
        protected StatusEffect(MobEffectCategory category, int color) {
            super(category, color);
        }

        @SuppressWarnings("unused")
        protected StatusEffect(MobEffectCategory category, int color, ParticleOptions particleOptions) {
            super(category, color, particleOptions);
        }
    }
}
