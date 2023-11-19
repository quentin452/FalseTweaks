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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.optifine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import shadersmod.client.ShadersRender;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;

@Mixin(ShadersRender.class)
public class ShadersRendererMixin {
    private static WorldRenderer[] ft$nullArray;

    @Redirect(method = "renderShadowMap",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderGlobal;worldRenderers:[Lnet/minecraft/client/renderer/WorldRenderer;"),
              require = 1)
    private static WorldRenderer[] disableShadowMapFrustrumToggling(RenderGlobal instance) {
        if (ft$nullArray == null) {
            return (ft$nullArray = new WorldRenderer[0]);
        }
        return ft$nullArray;
    }
}
