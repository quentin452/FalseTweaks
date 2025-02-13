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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import com.falsepattern.falsetweaks.modules.threadedupdates.ICapturableTessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Mixin(Tessellator.class)
public abstract class TessellatorMixin implements ICapturableTessellator {

    @Shadow
    private int[] rawBuffer;

    @Shadow
    private int rawBufferIndex;

    @Shadow
    private int vertexCount;

    @Shadow
    protected abstract void reset();

    @Shadow
    private boolean isDrawing;

    @Shadow(aliases = {"rawBufferSize"})
    public int field_78388_E;
            // This field has an odd name because of optifine compat (cAnNoT aLiAs NoN-pRiVaTe MeMbEr -- SpongePowered Mixins)

    @Shadow
    private boolean hasTexture;

    @Shadow
    private boolean hasBrightness;

    @Shadow
    private boolean hasColor;

    @Shadow
    private boolean hasNormals;

    @Override
    public TesselatorVertexState arch$getUnsortedVertexState() {
        if (vertexCount < 1) {
            return null;
        }
        return new TesselatorVertexState(Arrays.copyOf(rawBuffer, rawBufferIndex), this.rawBufferIndex, this.vertexCount, this.hasTexture, this.hasBrightness, this.hasNormals, this.hasColor);
    }

    @Override
    public void arch$addTessellatorVertexState(TesselatorVertexState state) throws IllegalStateException {
        if (state == null) {
            return;
        }
        // TODO check if draw mode is the same

        hasTexture |= state.getHasTexture();
        hasBrightness |= state.getHasBrightness();
        hasColor |= state.getHasColor();
        hasNormals |= state.getHasNormals();

        //Hurr durr
        if (field_78388_E == 0) {
            field_78388_E = 0x10000;
        }

        while (field_78388_E < rawBufferIndex + state.getRawBuffer().length) {
            field_78388_E *= 2;
        }
        if (field_78388_E > rawBuffer.length) {
            rawBuffer = Arrays.copyOf(rawBuffer, field_78388_E);
        }

        System.arraycopy(state.getRawBuffer(), 0, rawBuffer, rawBufferIndex, state.getRawBufferIndex());
        rawBufferIndex += state.getRawBufferIndex();
        vertexCount += state.getVertexCount();
    }

    @Override
    public void discard() {
        isDrawing = false;
        reset();
    }

    /**
     * @reason Allow using multiple tessellator instances concurrently by removing static field access in alternate
     * instances.
     */
    @Redirect(method = "reset",
              at = @At(value = "INVOKE",
                       target = "Ljava/nio/ByteBuffer;clear()Ljava/nio/Buffer;"),
              require = 1)
    private Buffer removeStaticBufferAccessOutsideSingleton(ByteBuffer buffer) {
        if (((Object) this) == Tessellator.instance) {
            return buffer.clear();
        }
        return buffer;
    }
}
