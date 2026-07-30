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

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import survivalblock.last_rites.common.LastRites;

public final class LastRitesTags {
    private LastRitesTags() {
    }

    public static final TagKey<Block> SOUL_IMMOLATORS = TagKey.create(Registries.BLOCK, LastRites.id("soul_immolators"));
    public static final TagKey<Item> SOUL_ASH_INGREDIENTS = TagKey.create(Registries.ITEM, LastRites.id("soul_ash_ingredients"));
}
