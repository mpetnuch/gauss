/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStructureSpliterator.java` is part of Gauss.
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

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.DoubleConsumer;

/**
 * @author Michael Petnuch
 */
public class ArrayStructureSpliterator implements Spliterator.OfDouble {
    private final ArrayStore.ArrayStructure structure;
    private final int[] indices;
    private final double[] array;
    private final int fence;  // one past last element to be processed
    private final int characteristics = ORDERED | IMMUTABLE | SIZED | SUBSIZED;
    private int index;        // current index int the array, modified on advance/split
    private int elementIndex; // current element to be processed, modified on advance/split

    public ArrayStructureSpliterator(ArrayStore.ArrayStructure structure, double[] array) {
        this.structure = structure;
        this.array = array;
        this.indices = new int[structure.dimension];

        this.fence = structure.size;
        this.index = structure.offset;
        this.elementIndex = 0;
    }

    public ArrayStructureSpliterator(ArrayStore.ArrayStructure structure, double[] array, int[] indices, int index, int elementIndex, int fence) {
        this.structure = structure;
        this.array = array;
        this.indices = indices;
        this.index = index;
        this.elementIndex = elementIndex;
        this.fence = fence;
    }

    @Override
    public OfDouble trySplit() {
        final int lo = index, loElement = elementIndex, midElement = (loElement + structure.size) >>> 1;
        if (loElement >= midElement) {
            return null;
        }

        // copy the current indicies as the lo indicies
        final int[] loIndicies = indices.clone();

        // find the number of elements we need to advance the indices by
        int splitAdvance = midElement - loElement;

        // find the number of elements advanced by increasing a dimension's index value
        final int[] subDimensionSize = new int[structure.dimension];
        for (int i = structure.dimension - 1, p = 1; i >= 0; i--) {
            subDimensionSize[i] = p;
            p *= structure.dimensionLength(i);
        }

        // We need to find the index of max sub-dimension for which the requested element is in. Once we
        // have that we that we can adjust all the other sub dimensions to the exact requested position
        int maxSubDimension = structure.dimension - 1;
        while (maxSubDimension >= 0) {
            final int n = subDimensionSize[maxSubDimension];
            final int l = structure.dimensionLength(maxSubDimension);
            for (int j = indices[maxSubDimension]; splitAdvance >= n && j < l; j++) {
                indices[maxSubDimension]++;
                splitAdvance -= n;
            }

            if (indices[maxSubDimension] == l) {
                // We adjusted all we can in this dimension, so we zero out the index and advance the
                // next dimension
                indices[maxSubDimension] = 0;
                indices[maxSubDimension - 1]++;
            }

            maxSubDimension--;
        }

        // go up in the dimensions and isolate the exact position
        for (int subDimension = maxSubDimension + 1; subDimension < structure.dimension; subDimension++) {
            final int n = subDimensionSize[subDimension];
            final int l = structure.dimensionLength(subDimension);
            for (int j = indices[subDimension]; splitAdvance >= n && j < l; j++) {
                indices[subDimension]++;
                splitAdvance -= n;
            }
        }

        // set index based on the newly adjusted indicies
        index = structure.index(indices);

        return new ArrayStructureSpliterator(structure, array, loIndicies, lo, loElement, elementIndex = midElement);
    }

    @Override
    public void forEachRemaining(DoubleConsumer action) {
        Objects.requireNonNull(action);

        // hoist accesses and checks from loop
        final int hi;
        int i;
        if (array.length >= (hi = fence) && (i = index) >= 0 && i < (index = hi)) {
            do {
                action.accept(array[i]);
            } while ((i = nextIndex(i)) < hi);
        }
    }

    @Override
    public boolean tryAdvance(DoubleConsumer action) {
        Objects.requireNonNull(action);

        if (elementIndex > 0 && elementIndex < fence) {
            action.accept(array[index]);
            index = nextIndex(index);
            elementIndex++;
            return true;
        }

        return false;
    }

    private int nextIndex(int currentIndex) {
        for (int i = structure.dimension - 1; i >= 0; i--) {
            if (++indices[i] < structure.dimensions[i]) {
                currentIndex += structure.strides[i];
                return currentIndex;
            }

            indices[i] = 0;
            currentIndex -= ((structure.dimensions[i] - 1) * structure.strides[i]);
        }

        return currentIndex;
    }

    @Override
    public long estimateSize() {
        return (long) (structure.size - elementIndex);
    }

    @Override
    public int characteristics() {
        return characteristics;
    }
}
