package survivalblock.last_rites.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalblock.last_rites.common.init.LastRitesAttachmentTypes;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin extends Level {
    protected ServerWorldMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void catchEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!this.getAttachedOrElse(LastRitesAttachmentTypes.PRODUCING_URN, false)) {
            return;
        }
        if (!(entity instanceof ItemEntity item)) {
            return;
        }

        List<ItemStack> captured = this.getAttachedOrElse(LastRitesAttachmentTypes.CAPTURED_STACKS, ImmutableList.of());
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builderWithExpectedSize(captured.size() + 1);
        builder.addAll(captured);
        builder.add(item.getItem());

        this.setAttached(LastRitesAttachmentTypes.CAPTURED_STACKS, builder.build());

        item.discard();
        cir.setReturnValue(false);
    }
}
