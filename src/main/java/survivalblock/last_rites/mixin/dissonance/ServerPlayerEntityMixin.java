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
package survivalblock.last_rites.mixin.dissonance;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalblock.last_rites.common.init.LastRitesStatusEffects;
import survivalblock.last_rites.common.saveddata.DissonanceTracker;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player {
    @Shadow
    public abstract ServerLevel level();

    public ServerPlayerEntityMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void diss(CallbackInfo ci) {
        if (this.level().getGameTime() % 10 == 0) {
            int dissonance = DissonanceTracker.get(this.level()).getDissonance(this.uuid);
            if (dissonance >= 5) {
                this.addEffect(new MobEffectInstance(LastRitesStatusEffects.DISSONANCE, 80, dissonance - 5, true, true));
            }
        }
    }
}
