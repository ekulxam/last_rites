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

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.core.HolderGetter
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.data.DataProvider
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.tags.BlockItemTagAppender
import net.minecraft.data.tags.TagAppender
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import survivalblock.last_rites.mixin.FabricTagsProviderAccessor
import survivalblock.last_rites.mixin.RecipeProviderAccessor

fun createDatagenBuilder(pack : FabricDataGenerator.Pack, function : DatagenBuilder.() -> Unit): DatagenBuilder {
    val builder = DatagenBuilder(pack)
    builder.function()
    return builder
}

@Suppress("UNCHECKED_CAST")
fun FabricTagsProvider.BlockTagsProvider.builder(key : TagKey<Block>) : BlockItemTagAppender<Block> {
    return (this as FabricTagsProviderAccessor.BlockTagsProviderAccessor).`last_rites$invokeBuilder`(key)
}

@Suppress("UNCHECKED_CAST")
fun FabricTagsProvider.ItemTagsProvider.builder(key : TagKey<Item>) : BlockItemTagAppender<Item> {
    return (this as FabricTagsProviderAccessor.ItemTagsProviderAccessor).`last_rites$invokeBuilder`(key)
}

@Suppress("UNCHECKED_CAST", "unused")
fun <T : Any> FabricTagsProvider<T>.builder(key : TagKey<T>) : TagAppender<T> {
    return (this as FabricTagsProviderAccessor<T>).`last_rites$invokeBuilder`(key)
}

@Suppress("unused")
open class DatagenBuilder(val pack : FabricDataGenerator.Pack) {

    open fun models(blocks : (BlockModelGenerators.() -> Unit)? = null, items : (ItemModelGenerators.() -> Unit)? = null): FabricModelProvider {
        return provider { output, _ ->
            object : FabricModelProvider(output) {
                override fun generateBlockStateModels(blockModelGenerators : BlockModelGenerators) {
                    if (blocks != null) {
                        blockModelGenerators.blocks()
                    }
                }

                override fun generateItemModels(itemModelGenerators : ItemModelGenerators) {
                    if (items != null) {
                        itemModelGenerators.items()
                    }
                }
            }
        }
    }

    open fun lang(code : String, translations : FabricLanguageProvider.TranslationBuilder.(registryLookup: HolderLookup.Provider) -> Unit): FabricLanguageProvider {
        return provider { output, registriesFuture ->
            object : FabricLanguageProvider(output, code, registriesFuture) {
                override fun generateTranslations(
                    registryLookup: HolderLookup.Provider,
                    builder: TranslationBuilder
                ) {
                    builder.translations(registryLookup)
                }
            }
        }
    }

    open fun lang(translations : FabricLanguageProvider.TranslationBuilder.(registryLookup: HolderLookup.Provider) -> Unit): FabricLanguageProvider {
        return lang("en_us", translations)
    }

    open fun blockLootTables(blocks : FabricBlockLootSubProvider.() -> Unit): FabricBlockLootSubProvider {
        return provider { output, registriesFuture ->
            object : FabricBlockLootSubProvider(output, registriesFuture) {
                override fun generate() {
                    blocks()
                }
            }
        }
    }

    open fun recipes(name : String, providerSquared : RecipeProviderProvider): FabricRecipeProvider {
        return provider { output, registriesFuture ->
            object : FabricRecipeProvider(output, registriesFuture) {
                override fun createRecipeProvider(
                    registries: HolderLookup.Provider,
                    output: RecipeOutput
                ): RecipeProvider {
                    return providerSquared.provide(registries, output)
                }

                override fun getName(): String {
                    return name
                }
            }
        }
    }

    open fun recipes(name : String, builder : RecipeProvider.(registries: HolderLookup.Provider, items : HolderGetter<Item>, output: RecipeOutput) -> Unit): FabricRecipeProvider {
        return recipes(name) { registries, output ->
            object : RecipeProvider(registries, output) {
                override fun buildRecipes() {
                    @Suppress("CAST_NEVER_SUCCEEDS")
                    builder(
                        this.registries,
                        (this as RecipeProviderAccessor).`last_rites$getItems`(),
                        this.output
                    )
                }
            }
        }
    }

    open fun recipes(builder : RecipeProvider.(registries: HolderLookup.Provider, items : HolderGetter<Item>, output: RecipeOutput) -> Unit): FabricRecipeProvider {
        return recipes("Recipes", builder)
    }

    open fun <T : DataProvider> provider(factory : FabricDataGenerator.Pack.RegistryDependentFactory<T>): T {
        return this.pack.addProvider(factory)
    }

    open fun tags(tagProviders : (TagBuilder.() -> Unit)): TagBuilder {
        val builder = TagBuilder(this.pack)
        builder.tagProviders()
        return builder
    }

    fun interface RecipeProviderProvider {
        fun provide(registries: HolderLookup.Provider, output: RecipeOutput): RecipeProvider
    }

    open class TagBuilder(val pack : FabricDataGenerator.Pack) {
        protected var blocks : FabricTagsProvider.BlockTagsProvider? = null

        open fun blockEntities(adder : FabricTagsProvider.BlockEntityTypeTagsProvider.(registries : HolderLookup.Provider) -> Unit): FabricTagsProvider.BlockEntityTypeTagsProvider {
            return provider { output, registriesFuture ->
                object : FabricTagsProvider.BlockEntityTypeTagsProvider(output, registriesFuture) {
                    override fun addTags(registries: HolderLookup.Provider) {
                        adder(registries)
                    }
                }
            }
        }

        open fun blocks(adder : FabricTagsProvider.BlockTagsProvider.(registries : HolderLookup.Provider) -> Unit): FabricTagsProvider.BlockTagsProvider {
            val provider = provider { output, registriesFuture ->
                object : FabricTagsProvider.BlockTagsProvider(output, registriesFuture) {
                    override fun addTags(registries: HolderLookup.Provider) {
                        adder(registries)
                    }
                }
            }
            this.blocks = provider
            return provider
        }

        open fun damageTypes(adder : FabricTagsProvider<DamageType>.(registries : HolderLookup.Provider) -> Unit): FabricTagsProvider<DamageType> {
            return other(Registries.DAMAGE_TYPE, adder)
        }

        open fun entities(adder : FabricTagsProvider.EntityTypeTagsProvider.(registries : HolderLookup.Provider) -> Unit): FabricTagsProvider.EntityTypeTagsProvider {
            return provider { output, registriesFuture ->
                object : FabricTagsProvider.EntityTypeTagsProvider(output, registriesFuture) {
                    override fun addTags(registries: HolderLookup.Provider) {
                        adder(registries)
                    }
                }
            }
        }

        open fun fluids(adder : FabricTagsProvider.FluidTagsProvider.(registries : HolderLookup.Provider) -> Unit): FabricTagsProvider.FluidTagsProvider {
            return provider { output, registriesFuture ->
                object : FabricTagsProvider.FluidTagsProvider(output, registriesFuture) {
                    override fun addTags(registries: HolderLookup.Provider) {
                        adder(registries)
                    }
                }
            }
        }

        open fun items(adder : FabricTagsProvider.ItemTagsProvider.(registries : HolderLookup.Provider) -> Unit): FabricTagsProvider.ItemTagsProvider {
            return provider { output, registriesFuture ->
                object : FabricTagsProvider.ItemTagsProvider(output, registriesFuture, this.blocks) {
                    override fun addTags(registries: HolderLookup.Provider) {
                        adder(registries)
                    }
                }
            }
        }

        open fun <T : Any> other(key : ResourceKey<out Registry<T>>, adder : FabricTagsProvider<T>.(registries : HolderLookup.Provider) -> Unit): FabricTagsProvider<T> {
            return provider { output, registriesFuture ->
                object : FabricTagsProvider<T>(output, key, registriesFuture) {
                    override fun addTags(registries: HolderLookup.Provider) {
                        adder(registries)
                    }
                }
            }
        }

        open fun <P : FabricTagsProvider<T>, T : Any> provider(factory : FabricDataGenerator.Pack.RegistryDependentFactory<P>): P {
            return this.pack.addProvider(factory)
        }
    }
}