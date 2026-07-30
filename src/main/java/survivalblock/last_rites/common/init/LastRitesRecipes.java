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

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import survivalblock.atmosphere.registrar.delayed.DelayedRegistrant;
import survivalblock.last_rites.common.LastRites;
import survivalblock.last_rites.common.recipe.SoulImmolationRecipe;

public class LastRitesRecipes {
    private static final DelayedRegistrant<RecipeType<?>> RECIPE_TYPE_REGISTRANT = new DelayedRegistrant<>(LastRites.MOD_ID, BuiltInRegistries.RECIPE_TYPE);
    private static final DelayedRegistrant<RecipeBookCategory> RECIPE_BOOK_CATEGORY_REGISTRANT = new DelayedRegistrant<>(LastRites.MOD_ID, BuiltInRegistries.RECIPE_BOOK_CATEGORY);
    private static final DelayedRegistrant<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTRANT = new DelayedRegistrant<>(LastRites.MOD_ID, BuiltInRegistries.RECIPE_SERIALIZER);

    public static final String SOUL_IMMOLATION_ID;
    public static final RecipeType<SoulImmolationRecipe> SOUL_IMMOLATION;
    public static final RecipeBookCategory SOUL_IMMOLATION_CATEGORY;
    public static final RecipeSerializer<SoulImmolationRecipe> SOUL_IMMOLATION_SERIALIZER;

    public static void init() {
        RECIPE_TYPE_REGISTRANT.consumeAll();
        RECIPE_BOOK_CATEGORY_REGISTRANT.consumeAll();
        RECIPE_SERIALIZER_REGISTRANT.consumeAll();
    }

    static {
        String soulImmolation = "soul_immolation";
        SOUL_IMMOLATION_ID = LastRites.id(soulImmolation).toString();
        SOUL_IMMOLATION = RECIPE_TYPE_REGISTRANT.register(
                soulImmolation,
                new RecipeType<>() {
                    @Override
                    public String toString() {
                        return SOUL_IMMOLATION_ID;
                    }
                }
        );
        SOUL_IMMOLATION_CATEGORY = RECIPE_BOOK_CATEGORY_REGISTRANT.register(
                soulImmolation,
                new RecipeBookCategory()
        );
        SOUL_IMMOLATION_SERIALIZER = RECIPE_SERIALIZER_REGISTRANT.register(
                soulImmolation,
                new RecipeSerializer<>(SoulImmolationRecipe.MAP_CODEC, SoulImmolationRecipe.STREAM_CODEC)
        );
    }
}
