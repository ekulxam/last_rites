package survivalblock.last_rites.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import survivalblock.last_rites.common.LastRites;
import survivalblock.last_rites.common.block.entity.CanopicUrnBlockEntity;
import survivalblock.last_rites.common.init.LastRitesItems;

import java.util.List;

public class CanopicUrnBlock extends BaseEntityBlock {
    public static final MapCodec<CanopicUrnBlock> CODEC = simpleCodec(CanopicUrnBlock::new);
    public static final IntegerProperty VARIANT = IntegerProperty.create("last_rites__variant", 0, 1);
    public static final VoxelShape[] SHAPES = new VoxelShape[2];

    public CanopicUrnBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new CanopicUrnBlockEntity(worldPosition, blockState);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CanopicUrnBlockEntity blockEntity)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        if (blockEntity.isOwner(player)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        if (!stack.is(LastRitesItems.CANOPIC_KEY)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        blockEntity.setUnlocked(true);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof CanopicUrnBlockEntity blockEntity)) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        if (blockEntity.isOwner(player)) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        if (blockEntity.isUnlocked()) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        Player owner = blockEntity.getOwnerFromLevel(player.level());
        if (owner == null || pos.distToCenterSqr(owner.position()) <= 30) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        return 0.0F;
    }

    @Override
    protected List<ItemStack> getDrops(final BlockState state, LootParams.Builder params) {
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CanopicUrnBlockEntity blockEntity) {
            params = params.withDynamicDrop(ShulkerBoxBlock.CONTENTS, output -> {
                for (int i = 0; i < blockEntity.getContainerSize(); i++) {
                    output.accept(blockEntity.getItem(i));
                }
            });
        }

        return super.getDrops(state, params);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int variant = state.getValue(VARIANT) % SHAPES.length;
        return SHAPES[variant];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    static {
        VoxelShape shape = Shapes.box(0.1875, 0, 0.1875, 0.8125, 0.4375, 0.8125);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.4375, 0.375, 0.625, 0.6875, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.6875, 0.3125, 0.6875, 0.875, 0.6875), BooleanOp.OR);
        SHAPES[0] = shape;

        shape = Shapes.box(0.1875, 0, 0.1875, 0.8125, 0.625, 0.8125);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.625, 0.3125, 0.6875, 0.8125, 0.6875), BooleanOp.OR);
        SHAPES[1] = shape;
    }
}
