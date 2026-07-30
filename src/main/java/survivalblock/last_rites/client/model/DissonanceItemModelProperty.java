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
package survivalblock.last_rites.client.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import survivalblock.last_rites.common.init.LastRitesAttachmentTypes;

public final class DissonanceItemModelProperty implements RangeSelectItemModelProperty {
    public static final DissonanceItemModelProperty INSTANCE = new DissonanceItemModelProperty();
    public static final MapCodec<DissonanceItemModelProperty> CODEC = MapCodec.unit(INSTANCE);

    private DissonanceItemModelProperty() {
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return CODEC;
    }

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        if (owner != null && owner.asLivingEntity() instanceof Player player) {
            return player.getAttachedOrElse(LastRitesAttachmentTypes.DISSONANCE, 0) / 4F;
        }
        return 0;
    }
}
