/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `StridedArrayStructureSpliterator.java` is part of Gauss.
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
public class StridedArrayStructureSpliterator implements Spliterator.OfDouble {
    private final ArrayStructure structure;
    private final double[] array;
    private final int stride;
    private final int fence;  // one past last index
    private final int characteristics = IMMUTABLE | ORDERED | SIZED | SUBSIZED;
    private int index;        // current index, modified on advance/split

    public StridedArrayStructureSpliterator(ArrayStructure structure, double[] array) {
        this.stride = structure.getStrides(0);
        this.structure = structure;
        this.array = array;

        this.index = structure.offset;
        this.fence = 1 + structure.offset + (structure.size - 1) * stride;
    }

    public StridedArrayStructureSpliterator(ArrayStructure structure, double[] array, int stride, int index, int fence) {
        this.structure = structure;
        this.array = array;
        this.stride = stride;

        this.index = index;
        this.fence = fence;
    }

    @Override
    public OfDouble trySplit() {
        // mid is the current index adjusted by the half the remaining elements
        // times the stride width
        final int lo = index, mid = lo + (((fence - lo) / stride) >>> 1) * stride;
        if (lo >= mid) {
            return null;
        }

        return new StridedArrayStructureSpliterator(structure, array, stride, lo, index = mid);
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

        if (index > 0 && index < fence) {
            action.accept(array[index]);
            index = nextIndex(index);
            return true;
        }

        return false;
    }

    private int nextIndex(int currentIndex) {
        return currentIndex + stride;
    }

    @Override
    public long estimateSize() {
        return (long) (structure.size - (fence - index) * stride);
    }

    @Override
    public int characteristics() {
        return characteristics;
    }
}
