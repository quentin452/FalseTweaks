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

package com.falsepattern.falsetweaks.modules.voxelizer.loading;

import net.minecraft.client.resources.data.IMetadataSection;

import java.util.Arrays;

public class LayerMetadataSection implements IMetadataSection {
    private final float[] thicknesses;

    public LayerMetadataSection(float[] thicknesses) {
        this.thicknesses = Arrays.copyOf(thicknesses, thicknesses.length);
    }

    public float[] thicknesses() {
        return Arrays.copyOf(thicknesses, thicknesses.length);
    }
}
