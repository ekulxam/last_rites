package survivalblock.last_rites.common.block.entity;

import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import survivalblock.last_rites.common.LastRites;
import survivalblock.last_rites.common.init.LastRitesBlockEntityTypes;
import survivalblock.last_rites.mixin.NonNullListAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CanopicUrnBlockEntity extends BlockEntity {
    public CanopicUrnBlockEntity(BlockPos worldPosition, BlockState blockState) {
        this(LastRitesBlockEntityTypes.CINERARY_URN, worldPosition, blockState);
    }

    public CanopicUrnBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    private final NonNullList<ItemStack> stacks = NonNullListAccessor.invokeInit(new ArrayList<>(), ItemStack.EMPTY);
    private int experience = 0;
    @Nullable
    private UUID ownerUuid = null;
    private boolean unlocked = false;

    public boolean isUnlocked() {
        return this.unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    @SuppressWarnings("unused")
    public boolean isEmpty() {
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int getContainerSize() {
        return this.stacks.size();
    }

    public ItemStack getItem(int index) {
        return this.stacks.get(index);
    }

    public void populate(UUID uuid, int experience, List<ItemStack> stacks) {
        this.ownerUuid = uuid;
        this.experience = experience;
        this.stacks.addAll(stacks);
        this.setChanged();
    }

    public boolean isOwner(Player player) {
        return player.getUUID().equals(this.ownerUuid);
    }

    @Nullable
    public Player getOwnerFromLevel(Level level) {
        if (this.ownerUuid == null) {
            return null;
        }
        return level.getPlayerByUUID(this.ownerUuid);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        if (this.ownerUuid != null) {
            switch (UUIDUtil.CODEC.encodeStart(ops, this.ownerUuid)) {
                case DataResult.Success<Tag> success -> tag.put("ownerUuid", success.value());
                case DataResult.Error<Tag> error -> LastRites.LOGGER.warn("Failed to encode ownerUuid {} for CineraryUrnBlockEntity! {}", this.ownerUuid, error.message());
            }
        }
        tag.putInt("experience", this.experience);
        tag.putBoolean("unlocked", this.unlocked);
        return tag;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        {
            ValueOutput.TypedOutputList<ItemStack> itemsOutput = output.list("Items", ItemStack.CODEC);

            for (ItemStack stack : this.stacks) {
                itemsOutput.add(stack);
            }

            if (itemsOutput.isEmpty()) {
                output.discard("Items");
            }
        }

        if (this.ownerUuid != null) {
            output.store("ownerUuid", UUIDUtil.CODEC, this.ownerUuid);
        }
        output.putInt("experience", this.experience);
        output.putBoolean("unlocked", this.unlocked);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (ItemStack stack : input.listOrEmpty("Items", ItemStack.CODEC)) {
            this.stacks.add(stack);
        }

        input.read("ownerUuid", UUIDUtil.CODEC).ifPresent(uuid -> this.ownerUuid = uuid);
        this.experience = input.getIntOr("experience", 0);
        this.unlocked = input.getBooleanOr("unlocked", false);
    }

    public int getExperience() {
        return this.experience;
    }
}
