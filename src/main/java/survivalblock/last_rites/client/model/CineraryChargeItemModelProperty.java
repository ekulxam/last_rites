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
import survivalblock.last_rites.common.component.item.CineraryBladeComponent;
import survivalblock.last_rites.common.init.LastRitesAttachmentTypes;
import survivalblock.last_rites.common.init.LastRitesDataComponentTypes;

public final class CineraryChargeItemModelProperty implements RangeSelectItemModelProperty {
    public static final CineraryChargeItemModelProperty INSTANCE = new CineraryChargeItemModelProperty();
    public static final MapCodec<CineraryChargeItemModelProperty> CODEC = MapCodec.unit(INSTANCE);

    private CineraryChargeItemModelProperty() {
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return CODEC;
    }

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        return stack.getOrDefault(LastRitesDataComponentTypes.CINERARY_BLADE, CineraryBladeComponent.DEFAULT).getState() / 3F;
    }
}
