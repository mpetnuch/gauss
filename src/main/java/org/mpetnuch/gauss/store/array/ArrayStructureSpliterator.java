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
public abstract class ArrayStructureSpliterator<Structure extends ArrayStructure> implements Spliterator.OfDouble {
    final double[] array;
    final Structure structure;

    final int fence;  // one past last index
    int index;        // current index, modified on advance/split
    int arrayIndex;   // current index in the array, modified on advance/split

    protected ArrayStructureSpliterator(Structure structure, double[] array, int index, int fence) {
        this.structure = structure;
        this.array = array;
        this.fence = fence;
        this.index = index;
        this.arrayIndex = structure.index(index);
    }

    protected ArrayStructureSpliterator(Structure structure, double[] array) {
        this(structure, array, 0, structure.size());
    }

    abstract int nextArrayIndex(int currentArrayIndex);

    @Override
    public void forEachRemaining(DoubleConsumer action) {
        Objects.requireNonNull(action);

        // hoist accesses and checks from loop
        int i;
        if ((i = index) >= 0 && i < (index = fence)) {
            do {
                try {
                    action.accept(array[arrayIndex]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                arrayIndex = nextArrayIndex(arrayIndex);
            } while (++i < fence);
        }
    }

    @Override
    public boolean tryAdvance(DoubleConsumer action) {
        Objects.requireNonNull(action);

        if (index < 0 || index >= fence) {
            return false;
        }

        action.accept(array[arrayIndex]);
        arrayIndex = nextArrayIndex(arrayIndex);
        index++;
        return true;
    }

    @Override
    public long estimateSize() {
        return (long) (fence - index);
    }

    @Override
    public int characteristics() {
        return IMMUTABLE | ORDERED | SIZED | SUBSIZED;
    }

    public static final class ContiguousStructureUnitStrideDimensionSpliterator extends ArrayStructureSpliterator<ArrayStructure> {
        private ContiguousStructureUnitStrideDimensionSpliterator(ArrayStructure structure, double[] array, int index, int fence) {
            super(structure, array, index, fence);
        }

        public ContiguousStructureUnitStrideDimensionSpliterator(ArrayStructure structure, double[] array) {
            super(structure, array);
        }

        @Override
        public OfDouble trySplit() {
            final int lo = index, mid = (lo + fence) >>> 1;
            if (lo < mid) {
                return new ContiguousStructureUnitStrideDimensionSpliterator(structure, array, lo, mid);
            } else {
                // can't split any more
                return null;
            }
        }

        @Override
        int nextArrayIndex(int currentArrayIndex) {
            return currentArrayIndex + 1;
        }
    }

    public static final class NaturalOrderSpliterator extends ArrayStructureSpliterator<ArrayStructure> {
        private final int[] indices;

        public NaturalOrderSpliterator(ArrayStructure structure, double[] array) {
            super(structure, array);

            this.indices = new int[structure.dimension()];
        }

        private NaturalOrderSpliterator(ArrayStructure structure, double[] array, int index, int fence) {
            super(structure, array, index, fence);
            this.indices = structure.indices(index);
        }

        @Override
        public OfDouble trySplit() {
            final int lo = index, mid = (lo + fence) >>> 1;
            if (lo < mid) {
                // update the current spliterators index and indices to reflect that it has been split in half
                System.arraycopy(structure.indices(index = mid), 0, indices, 0, indices.length);

                return new NaturalOrderSpliterator(structure, array, lo, mid);
            } else {
                // can't split any more
                return null;
            }
        }

        @Override
        int nextArrayIndex(int currentArrayIndex) {
            for (int i = structure.dimension() - 1; i >= 0; i--) {
                if (++indices[i] < structure.dimensionLength(i)) {
                    currentArrayIndex += structure.stride(i);
                    return currentArrayIndex;
                }

                indices[i] = 0;
                currentArrayIndex -= structure.backstride(i);
            }

            return currentArrayIndex;
        }
    }
}
