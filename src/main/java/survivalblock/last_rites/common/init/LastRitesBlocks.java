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

import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import survivalblock.atmosphere.registrar.delayed.DelayedBlockRegistrant;
import survivalblock.last_rites.common.LastRites;
import survivalblock.last_rites.common.block.SoulAshLayerBlock;

public class LastRitesBlocks {
    private LastRitesBlocks() {
    }

    private static final DelayedBlockRegistrant REGISTRANT = new DelayedBlockRegistrant(LastRites.MOD_ID);

    @SuppressWarnings("deprecation")
    public static final Block SOUL_ASH = REGISTRANT.register(
            Ids.SOUL_ASH.block().identifier().getPath(),
            SoulAshLayerBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .replaceable()
                    .forceSolidOff()
                    .strength(0.1F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.SOUL_SOIL)
                    .isViewBlocking((statex, _, _) -> statex.getValue(SnowLayerBlock.LAYERS) >= 8)
                    .pushReaction(PushReaction.DESTROY)
    );

    public static void init() {
        REGISTRANT.consumeAll();
    }

    public static final class Ids {
        private Ids() {
        }

        public static final BlockItemId SOUL_ASH = create("soul_ash");

        private static BlockItemId create(String name) {
            Identifier id = LastRites.id(name);
            return BlockItemId.create(id, id);
        }
    }
}
