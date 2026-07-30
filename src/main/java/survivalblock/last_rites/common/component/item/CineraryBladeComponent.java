package survivalblock.last_rites.common.component.item;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.Nullable;
import survivalblock.last_rites.common.LastRites;

import java.util.Objects;
import java.util.function.Consumer;

import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID;
import static net.minecraft.world.item.Item.BASE_ATTACK_SPEED_ID;

public record CineraryBladeComponent(int charge, boolean nextHitDecrement) implements TooltipProvider {
    public static final Identifier ENTITY_REACH_ID = LastRites.id("entity_reach");
    public static final Identifier BLOCK_REACH_ID = LastRites.id("block_reach");

    public static final int OVERCHARGE_STATE = 3;
    public static final int OVERCHARGE = 9;

    public static final int ASH = 0x52403F;

    public static final CineraryBladeComponent DEFAULT = new CineraryBladeComponent(0, false);
    public static final ImmutableMap<Integer, ItemAttributeModifiers> ATTRIBUTES = ImmutableMap.<Integer, ItemAttributeModifiers>builderWithExpectedSize(4)
            .put(
                    0,
                    ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(BLOCK_REACH_ID, -0.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(ENTITY_REACH_ID, -0.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build()
            )
            .put(
                    1,
                    ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build()
            )
            .put(
                    2,
                    ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(BLOCK_REACH_ID, 0.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(ENTITY_REACH_ID, 0.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build()
            )
            .put(
                    3,
                    ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(BLOCK_REACH_ID, 0.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(ENTITY_REACH_ID, 0.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build()
            )
            .build();

    public static final Codec<CineraryBladeComponent> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("charge").forGetter(component -> component.charge),
                    Codec.BOOL.fieldOf("nextHitDecrement").forGetter(component -> component.nextHitDecrement)
            ).apply(instance, CineraryBladeComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CineraryBladeComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, component -> component.charge,
            ByteBufCodecs.BOOL, component -> component.nextHitDecrement,
            CineraryBladeComponent::new
    );

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        boolean overcharged = this.isOvercharged();
        if (overcharged) {
            consumer.accept(Component.translatable("component.last_rites.cinerary_blade.overcharged").withColor(overchargedLerpedColor()));
        }
        consumer.accept(Component.translatable("component.last_rites.cinerary_blade.charge", this.charge).withColor(ASH));
        if (!overcharged && this.charge > 0) {
            consumer.accept(Component.translatable("component.last_rites.cinerary_blade.hitsUntilDecrement", this.nextHitDecrement ? 1 : 2).withColor(ASH));
        }
    }

    public int getItemBarWidth() {
        return Mth.clamp(Math.round(this.charge * 13.0F / (OVERCHARGE - 1)), 0, 13);
    }

    public int getItemBarColor() {
        return this.isOvercharged() ? overchargedLerpedColor() : ASH;
    }

    public static int overchargedLerpedColor() {
        return ARGB.srgbLerp((float) (Math.sin(Util.getMillis() / 200F) * 0.5 + 0.5), 0x80F9FF, 0x59C5D4);
    }

    public boolean isOvercharged() {
        return this.charge >= OVERCHARGE;
    }

    public int getState() {
        if (this.isOvercharged()) {
            return OVERCHARGE_STATE;
        }
        if (this.charge >= 5) {
            return 2;
        }
        if (this.charge >= 3) {
            return 1;
        }
        return 0;
    }

    public ItemAttributeModifiers getAttributes() {
        return Objects.requireNonNull(ATTRIBUTES.get(this.getState()));
    }

    public CineraryBladeComponent increment(int count) {
        return new CineraryBladeComponent(this.charge + count, false);
    }

    @Nullable
    public CineraryBladeComponent maybeDecrement() {
        if (this.isOvercharged()) {
            return new CineraryBladeComponent(OVERCHARGE - 1, false);
        }

        if (this.charge < 0) {
            return DEFAULT;
        }

        if (this.charge == 0) {
            if (this.nextHitDecrement) {
                return DEFAULT;
            }
            return null; // this
        }

        if (!this.nextHitDecrement) {
            return new CineraryBladeComponent(this.charge - 1, false);
        }
        return new CineraryBladeComponent(this.charge, true);
    }
}
