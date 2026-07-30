package survivalblock.last_rites.common.item;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import survivalblock.last_rites.common.component.item.CineraryBladeComponent;
import survivalblock.last_rites.common.init.LastRitesBlocks;
import survivalblock.last_rites.common.init.LastRitesDataComponentTypes;
import survivalblock.last_rites.common.networking.SoulCircleS2CPayload;

import static survivalblock.last_rites.common.component.item.CineraryBladeComponent.OVERCHARGE;

public class CineraryBladeItem extends Item {
    public CineraryBladeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);

        if (!state.is(LastRitesBlocks.SOUL_ASH)) {
            return InteractionResult.PASS;
        }

        if (!world.isClientSide()) {
            ItemStack stack = context.getItemInHand();
            CineraryBladeComponent component = stack.getOrDefault(LastRitesDataComponentTypes.CINERARY_BLADE, CineraryBladeComponent.DEFAULT);
            int layers = state.getValue(SnowLayerBlock.LAYERS);
            Player player = context.getPlayer();
            boolean canConsumeAll = player != null && player.isShiftKeyDown();
            int consume = Math.min(
                    canConsumeAll ? OVERCHARGE : 1,
                    Math.min(OVERCHARGE - component.charge(), layers)
            );

            if (consume <= 0) {
                return InteractionResult.SUCCESS;
            }

            component = component.increment(consume);
            set(stack, component);

            if (player != null) {
                if (component.isOvercharged()) {
                    player.getCooldowns().addCooldown(stack, 200);
                } else {
                    player.getCooldowns().addCooldown(stack, 20 * consume);
                }
            }

            layers -= consume;
            final int update = Block.UPDATE_ALL;

            if (layers <= 0) {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), update);
            } else {
                world.setBlock(pos, state.setValue(SnowLayerBlock.LAYERS, layers), update);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                final int particles = 5 * consume;
                new SoulCircleS2CPayload(particles, serverPlayer.getXRot(), serverPlayer.position())
                        .sendToAllIncludingSelf(serverPlayer);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);

        if (stack.getOrDefault(LastRitesDataComponentTypes.CINERARY_BLADE, CineraryBladeComponent.DEFAULT).isOvercharged()) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 2));

            new SoulCircleS2CPayload(20, attacker.getXRot(), target.position())
                    .sendToAllIncludingSelf(target);
        }
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        CineraryBladeComponent component = stack.getOrDefault(LastRitesDataComponentTypes.CINERARY_BLADE, CineraryBladeComponent.DEFAULT);
        ItemAttributeModifiers previous = component.getAttributes();
        component = component.maybeDecrement();
        if (component != null) {
            stack.set(LastRitesDataComponentTypes.CINERARY_BLADE, component);
            ItemAttributeModifiers current = component.getAttributes();
            if (previous != current) {
                stack.set(DataComponents.ATTRIBUTE_MODIFIERS, current);
            }
        }
    }

    public void addInstances(CreativeModeTab.Output output) {
        CineraryBladeComponent component;
        ItemStack stack = this.getDefaultInstance();
        output.accept(stack);

        for (int i = 1; i <= OVERCHARGE; i++) {
            stack = stack.copy();
            component = new CineraryBladeComponent(i, false);
            set(stack, component);
            output.accept(stack);
        }
    }

    public static void set(ItemStack stack, CineraryBladeComponent component) {
        stack.set(LastRitesDataComponentTypes.CINERARY_BLADE, component);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, component.getAttributes());
    }
}
