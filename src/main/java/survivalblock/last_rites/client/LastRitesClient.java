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
package survivalblock.last_rites.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import survivalblock.last_rites.client.model.CineraryChargeItemModelProperty;
import survivalblock.last_rites.client.model.DissonanceItemModelProperty;
import survivalblock.last_rites.common.LastRites;
import survivalblock.last_rites.common.networking.SoulCircleS2CPayload;

public class LastRitesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RangeSelectItemModelProperties.ID_MAPPER.put(LastRites.id("dissonance"), DissonanceItemModelProperty.CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(LastRites.id("cinerary_charge"), CineraryChargeItemModelProperty.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(SoulCircleS2CPayload.ID, (payload, context) -> {
            ClientLevel world = context.client().level;
            if (world == null) {
                return;
            }

            RandomSource random = world.getRandom();
            int particles = payload.count();
            float yaw = payload.playerYaw();
            float anglePer = 360F / particles;
            Vec3 vec3d = payload.pos();
            for (int i = 0; i < particles; i++) {
                float angle = (anglePer * i) + yaw;
                Vec3 particlePos = vec3d.add(new Vec3(Math.cos(angle), 0, Math.sin(angle)));
                world.addParticle(ParticleTypes.SOUL, false, false, particlePos.x, particlePos.y, particlePos.z, 0, Mth.nextFloat(random, 0.1F, 0.3F), 0);
            }
        });
    }
}
