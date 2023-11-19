/*
 * This file is part of FalseTweaks.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.mixins.client.leakfix.optifine;

import com.falsepattern.falsetweaks.modules.leakfix.LeakFix;
import com.falsepattern.falsetweaks.modules.leakfix.interfaces.IWorldRendererMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererOptifineMixin implements IWorldRendererMixin {

    @Shadow
    private boolean isInitialized;

    @Inject(method = "updateRenderer",
            at = @At(value = "HEAD"),
            require = 1)
    private void prepareRenderList(EntityLivingBase p_147892_1_, CallbackInfo ci) {
        if (LeakFix.ENABLED && !this.isInitialized) {
            genList();
        }
    }

    @Inject(method = "callOcclusionQueryList",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void dropOcclusionQueryIfUninitialized(CallbackInfo ci) {
        if (LeakFix.ENABLED && !hasRenderList()) {
            ci.cancel();
        }
    }
}
