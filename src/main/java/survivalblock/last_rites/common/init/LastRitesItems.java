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
package survivalblock.last_rites.common.init;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import survivalblock.atmosphere.registrar.delayed.DelayedItemGroupRegistrant;
import survivalblock.atmosphere.registrar.delayed.DelayedItemRegistrant;
import survivalblock.last_rites.common.LastRites;
import survivalblock.last_rites.common.component.item.CineraryBladeComponent;
import survivalblock.last_rites.common.item.CineraryBladeItem;

import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID;

public final class LastRitesItems {
    private LastRitesItems() {
    }

    private static final DelayedItemRegistrant REGISTRANT = new DelayedItemRegistrant(LastRites.MOD_ID);
    private static final DelayedItemGroupRegistrant ITEM_GROUP_REGISTRANT = new DelayedItemGroupRegistrant(LastRites.MOD_ID);

    public static final Item AMETHYST_ORB = REGISTRANT.register(
            "amethyst_orb",
            Item::new,
            new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
    );
    public static final BlockItem SOUL_ASH = REGISTRANT.register(
            LastRitesBlocks.SOUL_ASH,
            new Item.Properties()
                    .useBlockDescriptionPrefix()
                    .fireResistant()
    );
    public static final Item CANOPIC_KEY = REGISTRANT.register(
            "canopic_key",
            Item::new,
            new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
                    .attributes(
                            ItemAttributeModifiers.builder()
                                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 3.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                                    .build()
                    )
    );
    public static final CineraryBladeItem CINERARY_BLADE = REGISTRANT.register(
            "cinerary_blade",
            CineraryBladeItem::new,
            new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .durability(4000)
                    .enchantable(14)
                    .component(DataComponents.WEAPON, new Weapon(1))
                    .component(LastRitesDataComponentTypes.CINERARY_BLADE, CineraryBladeComponent.DEFAULT)
                    .attributes(CineraryBladeComponent.DEFAULT.getAttributes())
    );

    @SuppressWarnings("unused")
    public static final CreativeModeTab LAST_RITES_GROUP = ITEM_GROUP_REGISTRANT.register(
            "last_rites",
            FabricCreativeModeTab.builder()
                    .title(Component.translatable("last_rites.itemGroup.last_rites"))
                    .icon(SOUL_ASH::getDefaultInstance)
                    .displayItems((_, output) -> {
                        output.accept(SOUL_ASH);
                        output.accept(AMETHYST_ORB);
                        output.accept(CANOPIC_KEY);
                        CINERARY_BLADE.addInstances(output);
                    })
    );

    public static void init() {
        REGISTRANT.consumeAll();
        ITEM_GROUP_REGISTRANT.consumeAll();
    }
}
