/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.mixins.client.animfix.minecraft;

import com.falsepattern.falsetweaks.AnimationUpdateBatcher;
import com.falsepattern.falsetweaks.interfaces.ITextureMapMixin;
import com.falsepattern.falsetweaks.stitching.TooBigException;
import com.falsepattern.falsetweaks.stitching.TurboStitcher;
import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.renderer.texture.Stitcher;

import java.util.List;
import java.util.Set;

@Mixin(Stitcher.class)
public abstract class StitcherMixin {
    @Shadow
    @Final
    private List<Stitcher.Slot> stitchSlots;

    @Shadow
    private int currentHeight;
    @Shadow
    private int currentWidth;
    private TurboStitcher masterStitcher;
    private TurboStitcher batchingStitcher;

    @Inject(method = "<init>",
            at = @At(value = "RETURN"),
            require = 1)
    private void initTurbo(int maxWidth, int maxHeight, boolean forcePowerOf2, int maxTileDimension, int mipmapLevelStitcher, CallbackInfo ci) {
        masterStitcher = new TurboStitcher(maxWidth, maxHeight, forcePowerOf2);
        batchingStitcher = new TurboStitcher(maxWidth, maxHeight, false);
        masterStitcher.addSprite(batchingStitcher);
    }


    @Redirect(method = "addSprite",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"),
              require = 1)
    private boolean hijackAdd(Set<Stitcher.Holder> instance, Object e) {
        val holder = (Stitcher.Holder) e;
        val sprite = holder.getAtlasSprite();
        if ((sprite.hasAnimationMetadata() || sprite.getFrameCount() > 1)) {
            batchingStitcher.addSprite(holder);
        } else {
            masterStitcher.addSprite((Stitcher.Holder) e);
        }
        return true;
    }

    @Inject(method = "doStitch",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void doTurboStitch(CallbackInfo ci) {
        ci.cancel();
        try {
            batchingStitcher.stitch();
            masterStitcher.stitch();
            currentWidth = masterStitcher.width;
            currentHeight = masterStitcher.height;
            stitchSlots.clear();
            stitchSlots.addAll(masterStitcher.getSlots());
            ((ITextureMapMixin) AnimationUpdateBatcher.currentAtlas).initializeBatcher(batchingStitcher.x,
                                                                                       batchingStitcher.y,
                                                                                       batchingStitcher.width,
                                                                                       batchingStitcher.height);
        } catch (TooBigException ignored) {
            throw new StitcherException(null,
                                        "Unable to fit all textures into atlas. Maybe try a lower resolution resourcepack?");
        } finally {
            masterStitcher.reset();
            batchingStitcher.reset();
            masterStitcher.addSprite(batchingStitcher);
        }
    }
}
