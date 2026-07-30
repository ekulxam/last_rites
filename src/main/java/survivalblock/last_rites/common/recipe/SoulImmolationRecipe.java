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
package survivalblock.last_rites.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import survivalblock.last_rites.common.init.LastRitesRecipes;

public record SoulImmolationRecipe(Recipe.CommonInfo commonInfo, Ingredient ingredient, int ingredientCount, ItemStackTemplate result, float chance) implements Recipe<SingleRecipeInput> {
    public static final int MAX_COUNT = 99;

    public static final Codec<Float> CHANCE_CODEC = Codec.FLOAT.validate(f -> {
        if (f > 1.0) {
            return DataResult.error(() -> "Soul Immolation success chance cannot be greater than 1 (provided " + f + ")!");
        } else if (f <= 0.0) {
            return DataResult.error(() -> "Soul Immolation success chance cannot be less than or equal to 0 (provided " + f + ")!");
        }
        return DataResult.success(f);
    });
    public static final Codec<Integer> COUNT_CODEC = Codec.INT.validate(i -> {
        if (i > MAX_COUNT) {
            return DataResult.error(() -> "Soul Immolation ingredient count cannot be greater than + " + MAX_COUNT +  " (provided " + i + ")!");
        } else if (i <= 0) {
            return DataResult.error(() -> "Soul Immolation ingredient count cannot be less than or equal to 0 (provided " + i + ")!");
        }
        return DataResult.success(i);
    });

    public static final MapCodec<SoulImmolationRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                    CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
                    COUNT_CODEC.optionalFieldOf("ingredient_count", 1).forGetter(recipe -> recipe.ingredientCount),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                    CHANCE_CODEC.fieldOf("chance").forGetter(recipe -> recipe.chance)
            ).apply(instance, SoulImmolationRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SoulImmolationRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.ingredient,
            ByteBufCodecs.VAR_INT, recipe -> recipe.ingredientCount,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
            ByteBufCodecs.FLOAT, recipe -> recipe.chance,
            SoulImmolationRecipe::new
    );

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return this.result.create();
    }

    @Override
    public boolean showNotification() {
        return this.commonInfo.showNotification();
    }

    @Override
    public String group() {
        return LastRitesRecipes.SOUL_IMMOLATION_ID;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return LastRitesRecipes.SOUL_IMMOLATION_SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return LastRitesRecipes.SOUL_IMMOLATION;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return LastRitesRecipes.SOUL_IMMOLATION_CATEGORY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
