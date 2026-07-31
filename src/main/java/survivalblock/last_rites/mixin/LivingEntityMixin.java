package survivalblock.last_rites.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import survivalblock.last_rites.common.block.CanopicUrnBlock;
import survivalblock.last_rites.common.block.entity.CanopicUrnBlockEntity;
import survivalblock.last_rites.common.init.LastRitesAttachmentTypes;
import survivalblock.last_rites.common.init.LastRitesBlocks;
import survivalblock.last_rites.common.init.LastRitesGameRules;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @WrapMethod(method = "dropAllDeathLoot")
    private void wrapAndProduceUrn(ServerLevel level, DamageSource source, Operation<Void> original) {
        if (!((LivingEntity) (Object) this instanceof ServerPlayer serverPlayer)) {
            original.call(level, source);
            return;
        }

        final BlockPos blockPosition = this.blockPosition();
        BlockPos pos = blockPosition;
        if (!level.isInValidBounds(pos)) {
            original.call(level, source);
            return;
        }

        boolean failedPositionSearch = false;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            BlockPos below = pos.below();
            int maxSearch = 1000;
            while (true) {
                maxSearch--;
                if (maxSearch < 0) {
                    failedPositionSearch = true;
                    break;
                }

                if (!level.isInValidBounds(below)) {
                    failedPositionSearch = true;
                    break;
                }

                if (!level.getBlockState(below).isAir()) {
                    break;
                }

                pos = below;
                below = below.below();
            }
        } else if (state.getFluidState().is(FluidTags.WATER)) {
            BlockPos above = pos.above();
            int maxSearch = 1000;
            while (true) {
                maxSearch--;
                if (maxSearch < 0) {
                    failedPositionSearch = true;
                    break;
                }

                if (!level.isInValidBounds(above)) {
                    failedPositionSearch = true;
                    break;
                }

                pos = above;
                above = above.above();

                state = level.getBlockState(above);

                if (state.isAir()) {
                    pos = above;
                    break;
                }

                if (!state.getFluidState().is(FluidTags.WATER)) {
                    failedPositionSearch = true;
                    break;
                }
            }
        }

        if (failedPositionSearch) {
            pos = blockPosition;
            for (BlockPos mutable : BlockPos.betweenClosed(pos.offset(-3, -3, -3), pos.offset(3, 3, 3))) {
                if (!level.isInValidBounds(mutable)) {
                    continue;
                }

                if (level.getBlockState(mutable).isAir()) {
                    pos = mutable.immutable();
                    failedPositionSearch = false;
                    break;
                }
            }

            if (failedPositionSearch) {
                // failed again somehow lol
                original.call(level, source);
                return;
            }
        }

        try {
            level.setAttached(LastRitesAttachmentTypes.PRODUCING_URN, true);
            original.call(level, source);
            List<ItemStack> stacks = level.getAttachedOrElse(LastRitesAttachmentTypes.CAPTURED_STACKS, ImmutableList.of());
            level.setBlock(
                    pos,
                    LastRitesBlocks.CANOPIC_URN.defaultBlockState()
                            .setValue(CanopicUrnBlock.VARIANT, Util.getRandom(CanopicUrnBlock.VARIANT.getPossibleValues(), level.getRandom())),
                    Block.UPDATE_ALL
            );

            level.setAttached(LastRitesAttachmentTypes.PRODUCING_URN, false);
            level.removeAttached(LastRitesAttachmentTypes.CAPTURED_STACKS);

            if (level.getBlockEntity(pos) instanceof CanopicUrnBlockEntity blockEntity) {
                blockEntity.populate(
                        serverPlayer.getUUID(),
                        level.getGameRules().get(LastRitesGameRules.URNS_STORE_EXPERIENCE) ? serverPlayer.totalExperience : 0,
                        stacks
                );
            } else {
                for (ItemStack stack : stacks) {
                    this.spawnAtLocation(level, stack);
                }
            }
        } finally {
            level.setAttached(LastRitesAttachmentTypes.PRODUCING_URN, false);
            level.removeAttached(LastRitesAttachmentTypes.CAPTURED_STACKS);
        }
    }

    @WrapWithCondition(method = "dropAllDeathLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)V"))
    private boolean dropWhenISaySo(LivingEntity instance, ServerLevel level, Entity killer) {
        return !(instance instanceof ServerPlayer) || !level.getGameRules().get(LastRitesGameRules.URNS_STORE_EXPERIENCE);
    }
}
