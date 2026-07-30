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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import survivalblock.last_rites.common.LastRites;

@SuppressWarnings("deprecation")
public class LastRitesAttachmentTypes {
    public static final AttachmentType<Integer> DISSONANCE = AttachmentRegistry.<Integer>builder()
            .initializer(() -> LastRites.MIN_DISSONANCE)
            .persistent(
                    Codec.INT.validate(integer -> {
                        if (integer > LastRites.MAX_DISSONANCE) {
                            return DataResult.error(() -> "Dissonance cannot be greater than " + LastRites.MAX_DISSONANCE + " (provided " + integer + ")!");
                        }
                        if (integer < LastRites.MIN_DISSONANCE) {
                            return DataResult.error(() -> "Dissonance cannot be less than " + LastRites.MIN_DISSONANCE + " (provided " + integer + ")!");
                        }
                        return DataResult.success(integer);
                    })
            )
            .copyOnDeath()
            .syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.all())
            .buildAndRegister(LastRites.id("dissonance"));

    public static void init() {
        // NO-OP
    }
}
