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

import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class SoulImmolationRecipeBuilder implements RecipeBuilder {
    @Nullable
	private Ingredient ingredient;
    private int ingredientCount = 1;
    @Nullable
    private ItemStackTemplate result;
    @Nullable
    private Float chance;
	private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();

	public SoulImmolationRecipeBuilder() {
    }

    public SoulImmolationRecipeBuilder ingredient(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public SoulImmolationRecipeBuilder ingredientCount(int ingredientCount) {
        this.ingredientCount = ingredientCount;
        return this;
    }

    public SoulImmolationRecipeBuilder result(ItemStackTemplate result) {
        this.result = result;
        return this;
    }

    public SoulImmolationRecipeBuilder chance(float chance) {
        this.chance = chance;
        return this;
    }

    @Override
	public SoulImmolationRecipeBuilder unlockedBy(final String name, final Criterion<?> criterion) {
		this.advancementBuilder.unlockedBy(name, criterion);
		return this;
	}

    @ApiStatus.Obsolete
    @Override
    public SoulImmolationRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(Objects.requireNonNull(this.result));
    }

    @Override
    public void save(final RecipeOutput output, final ResourceKey<Recipe<?>> id) {
        SoulImmolationRecipe recipe = new SoulImmolationRecipe(
                new Recipe.CommonInfo(true),
                Objects.requireNonNull(this.ingredient),
                this.ingredientCount,
                Objects.requireNonNull(this.result),
                Objects.requireNonNull(this.chance)
        );
        SoulImmolationRecipe.COUNT_CODEC.encodeStart(JsonOps.INSTANCE, this.ingredientCount).getOrThrow();
        SoulImmolationRecipe.CHANCE_CODEC.encodeStart(JsonOps.INSTANCE, this.chance).getOrThrow();
		output.accept(id, recipe, this.advancementBuilder.build(output, id, RecipeCategory.MISC));
	}
}
