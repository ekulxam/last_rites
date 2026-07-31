package survivalblock.last_rites.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalblock.last_rites.common.LastRites;
import survivalblock.last_rites.common.block.entity.CanopicUrnBlockEntity;
import survivalblock.last_rites.common.init.LastRitesAttachmentTypes;

import java.util.List;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(
            method = {
                    "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;",
                    "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemInstance;)Ljava/util/List;"
            }, at = @At("HEAD")
    )
    private static void dropExperience(CallbackInfoReturnable<List<ItemStack>> cir, @Local(argsOnly = true, name = "level") ServerLevel level, @Local(argsOnly = true, name = "pos") BlockPos pos, @Local(argsOnly = true, name = "blockEntity")BlockEntity blockEntity) {
        if (blockEntity instanceof CanopicUrnBlockEntity canopicUrnBlockEntity) {
            ExperienceOrb.award(level, Vec3.atCenterOf(pos), canopicUrnBlockEntity.getExperience());
            if (canopicUrnBlockEntity.getOwnerFromLevel(level) instanceof ServerPlayer player) {
                LastRites.changeDissonance(player, -1);
            }
        }
    }
}
