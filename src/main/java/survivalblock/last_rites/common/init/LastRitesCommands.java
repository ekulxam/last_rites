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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.*;
import survivalblock.last_rites.common.LastRites;

public final class LastRitesCommands implements CommandRegistrationCallback {
    public static final LastRitesCommands INSTANCE = new LastRitesCommands();

    public static final DynamicCommandExceptionType NOT_PLAYER_FOR_DISSONANCE = new DynamicCommandExceptionType(obj -> Component.translatableEscape("commands.lastrites.dissonance.fail.not_player", obj));

    private LastRitesCommands() {
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        LiteralCommandNode<CommandSourceStack> parent = Commands.literal("lastrites").build();
        LiteralCommandNode<CommandSourceStack> dissonance = Commands.literal("dissonance")
                .requires(Commands.hasPermission(Commands.LEVEL_MODERATORS))
                .build();
        LiteralCommandNode<CommandSourceStack> getDissonance = Commands.literal("get")
                .executes(context -> {
                    ServerPlayer serverPlayer = context.getSource().getPlayer();
                    if (serverPlayer == null) {
                        throw NOT_PLAYER_FOR_DISSONANCE.create(context.getSource().getDisplayName());
                    }
                    return getDissonance(context, serverPlayer);
                })
                .then(
                        Commands.argument("target", EntityArgument.player())
                                .executes(LastRitesCommands::getDissonance)
                )
                .build();
        LiteralCommandNode<CommandSourceStack> setDissonance = Commands.literal("set")
                .then(
                        Commands.argument("target", EntityArgument.player())
                                .then(
                                        Commands.argument("value", IntegerArgumentType.integer(LastRites.MIN_DISSONANCE, LastRites.MAX_DISSONANCE))
                                                .executes(LastRitesCommands::setDissonance)
                                )
                )
                .build();
        LiteralCommandNode<CommandSourceStack> clearDissonance = Commands.literal("clear")
                .executes(context -> {
                    ServerPlayer serverPlayer = context.getSource().getPlayer();
                    if (serverPlayer == null) {
                        throw NOT_PLAYER_FOR_DISSONANCE.create(context.getSource().getDisplayName());
                    }
                    return clearDissonance(context, serverPlayer);
                })
                .then(
                        Commands.argument("target", EntityArgument.player())
                                .executes(LastRitesCommands::clearDissonance)
                )
                .build();

        dispatcher.getRoot().addChild(parent);

        parent.addChild(dissonance);

        dissonance.addChild(getDissonance);
        dissonance.addChild(setDissonance);
        dissonance.addChild(clearDissonance);
    }

    public static int getDissonance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return getDissonance(context, EntityArgument.getPlayer(context, "target"));
    }

    public static int getDissonance(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        int value = target.getAttachedOrCreate(LastRitesAttachmentTypes.DISSONANCE);
        context.getSource().sendSuccess(() -> Component.translatableEscape("commands.lastrites.dissonance.get.success", target.getName(), value), false);
        return 1;
    }

    public static int setDissonance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        int value = IntegerArgumentType.getInteger(context, "value");
        target.setAttached(LastRitesAttachmentTypes.DISSONANCE, value);
        context.getSource().sendSuccess(() -> Component.translatableEscape("commands.lastrites.dissonance.set.success", target.getName(), value), true);
        return 1;
    }

    public static int clearDissonance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return clearDissonance(context, EntityArgument.getPlayer(context, "target"));
    }

    public static int clearDissonance(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        target.setAttached(LastRitesAttachmentTypes.DISSONANCE, LastRites.MIN_DISSONANCE);
        context.getSource().sendSuccess(() -> Component.translatableEscape("commands.lastrites.dissonance.clear.success", target.getName()), true);
        return 1;
    }
}
