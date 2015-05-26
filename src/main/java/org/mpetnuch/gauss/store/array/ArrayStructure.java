/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStructure.java` is part of Gauss.
 *
 * Gauss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.mpetnuch.gauss.store.array;

import org.mpetnuch.gauss.store.Structure;

import java.util.Arrays;

/**
 * @author Michael Petnuch
 */
abstract class ArrayStructure implements Structure {
    protected final int dimension;
    protected final int size;
    protected final int offset;
    protected final int[] dimensions, strides;

    public ArrayStructure(int[] dimensions, int[] strides, int offset) {
        this.offset = offset;
        this.strides = strides;
        this.dimension = dimensions.length;
        this.dimensions = dimensions;
        this.size = Arrays.stream(dimensions).reduce(1, (left, right) -> left * right);
    }

    public int index(int... indices) {
        int index = offset;
        for (int n = 0; n < dimension; n++) {
            index += indices[n] * strides[n];
        }

        return index;
    }

    public int getOffset() {
        return offset;
    }

    public int getStrides(int dimension) {
        return strides[dimension];
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public int dimensionLength(int dimension) {
        return dimensions[dimension];
    }

    @Override
    public int size() {
        return size;
    }

    public boolean isCompact() {
        if (offset != 0) {
            return false;
        }

        for (int i = 1; i < dimension; i++) {
            if (strides[i - 1] != dimensions[i]) {
                return false;
            }
        }

        return true;
    }
}
