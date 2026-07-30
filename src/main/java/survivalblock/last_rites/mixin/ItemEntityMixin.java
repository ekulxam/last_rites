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
package survivalblock.last_rites.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import survivalblock.last_rites.common.LastRites;
import survivalblock.last_rites.common.init.LastRitesRecipes;
import survivalblock.last_rites.common.init.LastRitesTags;
import survivalblock.last_rites.common.recipe.SoulImmolationRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @WrapOperation(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;onDestroyed(Lnet/minecraft/world/entity/item/ItemEntity;)V"))
    private void soulImmolation(ItemStack instance, ItemEntity itemEntity, Operation<Void> original, @Local(argsOnly = true)ServerLevel serverLevel) {
        if (!this.getInBlockState().is(LastRitesTags.SOUL_IMMOLATORS)) {
            original.call(instance, itemEntity);
            return;
        }

        ItemStack stack = instance.copy();
        original.call(instance, itemEntity);

        SingleRecipeInput input = null;
        Optional<RecipeHolder<SoulImmolationRecipe>> maybeRecipe = null;
        final RandomSource random = itemEntity.getRandom();
        final List<ItemStack> recipeResults = new ArrayList<>();
        int limit = stack.getCount();
        while (true) {
            if (stack.isEmpty()) {
                break;
            }

            limit--;
            if (limit < 0) {
                //noinspection ConstantValue, OptionalAssignedToNull
                LastRites.LOGGER.warn(
                        "Tried to soul-immolate too many times! What are you doing? ItemStack: {}, recipeResults: {}, last input: {}, last recipe: {}",
                        stack,
                        Arrays.toString(recipeResults.toArray()),
                        input == null ? "null" : input,
                        maybeRecipe == null ? "null" : maybeRecipe.isEmpty() ? "empty" : maybeRecipe.get()
                );
                break;
            }

            input = new SingleRecipeInput(stack);
            maybeRecipe = serverLevel.getServer().getRecipeManager().getRecipeFor(LastRitesRecipes.SOUL_IMMOLATION, input, serverLevel, (RecipeHolder<SoulImmolationRecipe>) null);
            if (maybeRecipe.isEmpty()) {
                break;
            }

            RecipeHolder<SoulImmolationRecipe> recipeHolder = maybeRecipe.get();
            SoulImmolationRecipe recipe = recipeHolder.value();

            int ingredientCount = recipe.ingredientCount();
            if (stack.getCount() < ingredientCount) {
                break;
            }

            if (random.nextFloat() <= recipe.chance()) {
                ItemStack recipeResult = recipe.assemble(input);
                if (recipeResult.isItemEnabled(serverLevel.enabledFeatures())) {
                    recipeResults.add(recipeResult);
                }
            }
            stack.shrink(ingredientCount);
        }

        final List<ItemStack> results = new ArrayList<>();
        for (ItemStack resultStack : recipeResults) {
            if (results.isEmpty()) {
                results.add(resultStack);
                continue;
            }

            for (ItemStack other : results) {
                if (!ItemStack.isSameItemSameComponents(resultStack, other)) {
                    continue;
                }

                int transferableItemCount = Math.min(resultStack.getCount(), SoulImmolationRecipe.MAX_COUNT - other.getCount());
                resultStack.shrink(transferableItemCount);
                other.grow(transferableItemCount);

                if (resultStack.isEmpty()) {
                    break;
                }
            }

            if (!resultStack.isEmpty()) {
                results.add(resultStack);
            }
        }

        Vec3 position = this.position();
        for (ItemStack realResultStacks : results) {
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, position.x, position.y, position.z, realResultStacks));
        }
    }
}
