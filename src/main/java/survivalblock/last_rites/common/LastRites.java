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
package survivalblock.last_rites.common;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import survivalblock.last_rites.common.init.*;
import survivalblock.last_rites.common.networking.SoulCircleS2CPayload;

public class LastRites implements ModInitializer {
	public static final String MOD_ID = "last_rites";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final int MIN_DISSONANCE = 0;
    public static final int MAX_DISSONANCE = 15 - 1;

	@Override
	public void onInitialize() {
        LastRitesAttachmentTypes.init();
        LastRitesRecipes.init();
        LastRitesGameRules.init();
        LastRitesBlocks.init();
        LastRitesBlockEntityTypes.init();
        LastRitesDataComponentTypes.init();
        LastRitesItems.init();
        LastRitesStatusEffects.init();
        CommandRegistrationCallback.EVENT.register(LastRitesCommands.INSTANCE);

        PayloadTypeRegistry.clientboundPlay().register(SoulCircleS2CPayload.ID, SoulCircleS2CPayload.PACKET_CODEC);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

    public static void changeDissonance(ServerPlayer serverPlayer, int change) {
        serverPlayer.setAttached(
                LastRitesAttachmentTypes.DISSONANCE,
                Mth.clamp(
                        serverPlayer.getAttachedOrElse(LastRitesAttachmentTypes.DISSONANCE, MIN_DISSONANCE) + change,
                        MIN_DISSONANCE,
                        MAX_DISSONANCE
                )
        );
    }
}
