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
package survivalblock.last_rites

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.advancements.predicates.StatePropertiesPredicate
import net.minecraft.client.data.models.BlockModelGenerators.plainVariant
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ModelLocationUtils
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeProvider.getHasName
import net.minecraft.references.BlockIds
import net.minecraft.references.BlockItemIds
import net.minecraft.references.ItemIds
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SnowLayerBlock
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import survivalblock.last_rites.common.LastRites
import survivalblock.last_rites.common.block.CanopicUrnBlock
import survivalblock.last_rites.common.init.LastRitesBlocks
import survivalblock.last_rites.common.init.LastRitesItems
import survivalblock.last_rites.common.init.LastRitesStatusEffects
import survivalblock.last_rites.common.init.LastRitesTags
import survivalblock.last_rites.common.recipe.SoulImmolationRecipeBuilder

/**
 * Inspired by Glim's DataGenerationBuilder
 * https://git.greenhouse.lgbt/Modding/glim/src/branch/26.1/src/client/kotlin/lgbt/greenhouse/glim/client/datagen/GlimDatagenBuilders.kt
 */
class LastRitesDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        createDatagenBuilder(pack) {
            models(
                blocks = {
                    val textures = TextureMapping.cube(LastRitesBlocks.SOUL_ASH)
                    val snowModel = plainVariant(ModelTemplates.CUBE_ALL.create(LastRitesBlocks.SOUL_ASH, textures) { t, u -> /* sneakily forget to register */ })
                    this.registerSimpleFlatItemModel(LastRitesItems.SOUL_ASH)
                    this.blockStateOutput
                        .accept(
                            MultiVariantGenerator.dispatch(LastRitesBlocks.SOUL_ASH)
                                .with(
                                    PropertyDispatch.initial(BlockStateProperties.LAYERS)
                                        .generate { level ->
                                            if (level < 8) {
                                                plainVariant(
                                                    ModelLocationUtils.getModelLocation(
                                                        LastRitesBlocks.SOUL_ASH,
                                                        "_height" + level * 2
                                                    )
                                                )
                                            } else {
                                                snowModel // what is this
                                            }
                                        }
                                )
                        )
                    this.blockStateOutput
                        .accept(
                            MultiVariantGenerator.dispatch(LastRitesBlocks.CANOPIC_URN)
                                .with(
                                    PropertyDispatch.initial(CanopicUrnBlock.VARIANT)
                                        .generate { variant ->
                                            plainVariant(
                                                ModelLocationUtils.getModelLocation(
                                                    LastRitesBlocks.CANOPIC_URN,
                                                    "_$variant"
                                                )
                                            )
                                        }
                                )

                        )
                },
                items = {
                    for (i in 0..4) {
                        createFlatItemModel(LastRitesItems.AMETHYST_ORB, "_$i", ModelTemplates.FLAT_ITEM)
                    }
                    for (i in 0..3) {
                        createFlatItemModel(LastRitesItems.CINERARY_BLADE, "_$i", ModelTemplates.FLAT_HANDHELD_ITEM)
                    }
                    generateFlatItem(LastRitesItems.CANOPIC_KEY, ModelTemplates.FLAT_HANDHELD_ITEM)
                }
            )
            lang { lookup ->
                add(LastRitesItems.AMETHYST_ORB, "Amethyst Orb")
                add(LastRitesStatusEffects.DISSONANCE.value(), "Dissonance")
                add(LastRitesBlocks.SOUL_ASH, "Soul Ash")
                add(LastRitesTags.SOUL_IMMOLATORS, "Soul Immolators")
                add(LastRitesTags.SOUL_ASH_INGREDIENTS, "Soul Ash Ingredients")
                add("last_rites.itemGroup.last_rites", "Last Rites")

                add(LastRitesItems.CANOPIC_KEY, "Canopic Key")

                add(LastRitesItems.CINERARY_BLADE, "Cinerary Blade")
                add("component.last_rites.cinerary_blade.overcharged", "⚠ Overcharged ⚠")
                add("component.last_rites.cinerary_blade.charge", "Soul Ash Charges: %s")
                add("component.last_rites.cinerary_blade.hitsUntilDecrement", "Hits Until Charge Decrement: %s")

                add(LastRitesBlocks.CANOPIC_URN, "Cinerary Urn")

                // commands
                add(
                    "commands.lastrites.dissonance.get.success",
                    $$"%1$s has a dissonance level of %2$s"
                )
                add(
                    "commands.lastrites.dissonance.set.success",
                    $$"Set dissonance level of %1$s to %2$s"
                )
                add(
                    "commands.lastrites.dissonance.clear.success",
                    $$"Removed all dissonance levels from %1$s"
                )
                add(
                    "commands.lastrites.dissonance.fail.not_player",
                    $$"Non-player %1$s does not have dissonance"
                )
            }
            blockLootTables {
                this.add(LastRitesBlocks.SOUL_ASH) { block ->
                    LootTable.lootTable()
                        .withPool(
                            LootPool.lootPool()
                                .add(
                                    AlternativesEntry.alternatives(
                                        AlternativesEntry.alternatives(
                                            SnowLayerBlock.LAYERS.possibleValues
                                        ) { layers ->
                                            LootItem.lootTableItem(LastRitesItems.SOUL_ASH)
                                                .`when`(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                        .setProperties(
                                                            StatePropertiesPredicate.Builder.properties()
                                                                .hasProperty(SnowLayerBlock.LAYERS, layers)
                                                        )
                                                )
                                                .apply(
                                                    SetItemCountFunction.setCount(
                                                        ConstantValue.exactly(
                                                            layers.toFloat()
                                                        )
                                                    )
                                                )
                                        }
                                    )
                                )
                        )
                }
            }
            recipes { _, items, output ->
                shaped(RecipeCategory.MISC, LastRitesItems.AMETHYST_ORB)
                    .pattern("XXX")
                    .pattern("X_X")
                    .pattern("XXX")
                    .define('X', Items.AMETHYST_SHARD)
                    .define('_', Blocks.SOUL_SOIL)
                    .unlockedBy(getHasName(Items.AMETHYST_SHARD), has(Items.AMETHYST_SHARD))
                    .unlockedBy(getHasName(Blocks.SOUL_SOIL), has(Blocks.SOUL_SOIL))
                    .save(output)
                SoulImmolationRecipeBuilder()
                    .ingredient(Ingredient.of(items.getOrThrow(LastRitesTags.SOUL_ASH_INGREDIENTS)))
                    .ingredientCount(1)
                    .result(ItemStackTemplate(LastRitesItems.SOUL_ASH))
                    .chance(0.11F)
                    .unlockedBy("has_soul_ash_ingredient", has(LastRitesTags.SOUL_ASH_INGREDIENTS))
                    .save(output)
                shaped(RecipeCategory.COMBAT, LastRitesItems.CINERARY_BLADE)
                    .pattern(" O*")
                    .pattern("O*O")
                    .pattern("/O ")
                    .define('O', LastRitesItems.SOUL_ASH)
                    .define('*', Items.DIAMOND)
                    .define('/', Items.BREEZE_ROD)
                    .unlockedBy(getHasName(LastRitesItems.SOUL_ASH), has(LastRitesItems.SOUL_ASH))
                    .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
                    .unlockedBy(getHasName(Items.BREEZE_ROD), has(Items.BREEZE_ROD))
                    .save(output)
            }
            tags {
                blocks {
                    builder(LastRitesTags.SOUL_IMMOLATORS)
                        .add(BlockItemIds.SOUL_CAMPFIRE)
                        .add(BlockIds.SOUL_FIRE)
                    builder(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)
                        .add(LastRitesBlocks.Ids.SOUL_ASH)
                    builder(BlockTags.MINEABLE_WITH_SHOVEL)
                        .add(LastRitesBlocks.Ids.SOUL_ASH)
                    builder(BlockTags.MANGROVE_ROOTS_CAN_GROW_THROUGH)
                        .add(LastRitesBlocks.Ids.SOUL_ASH)
                }
                items {
                    builder(LastRitesTags.SOUL_ASH_INGREDIENTS)
                        .add(ItemIds.ROTTEN_FLESH)
                        .add(ItemIds.MUTTON)
                        .add(ItemIds.RABBIT)
                        .add(ItemIds.BEEF)
                        .add(ItemIds.CHICKEN)
                        .add(ItemIds.COD)
                        .add(ItemIds.SALMON)
                        .add(ItemIds.PORKCHOP)
                    builder(ItemTags.SWORDS)
                        .add(LastRitesItems.CINERARY_BLADE.builtInRegistryHolder().key())
                }
            }
        }
    }

    override fun getEffectiveModId(): String {
        return LastRites.MOD_ID
    }
}