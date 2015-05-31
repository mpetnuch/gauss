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

import org.mpetnuch.gauss.store.array.ArrayStore1D.ArrayStructure1D;
import org.mpetnuch.gauss.store.array.ArrayStore2D.ArrayStructure2D;

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

    private ArrayStructureSpliterator(Structure structure, double[] array, int index, int fence) {
        this.structure = structure;
        this.array = array;
        this.fence = fence;
        this.index = index;
        this.arrayIndex = structure.index(index);
    }

    private ArrayStructureSpliterator(Structure structure, double[] array) {
        this(structure, array, 0, structure.size());
    }

    public static Spliterator.OfDouble spliterator(ArrayStructure structure, double[] array) {
        Objects.requireNonNull(structure);

        if (structure.isContiguous() && structure.hasUnitStrideDimension()) {
            return new ContiguousArrayStructureWithUnitStrideDimension(structure, array);
        }

        if (structure instanceof ArrayStructure1D) {
            return new ArrayStructure1DWithNonUnitStride((ArrayStructure1D) structure, array);
        }

        if (structure.hasUnitStrideDimension() && structure instanceof ArrayStructure2D) {
            return new ArrayStructure2DWithUnitStride((ArrayStructure2D) structure, array);
        }

        return new GeneralArrayStructureSpliterator(structure, array);
    }

    abstract int nextArrayIndex(int currentArrayIndex);

    @Override
    public void forEachRemaining(DoubleConsumer action) {
        Objects.requireNonNull(action);

        // hoist accesses and checks from loop
        int i;
        if ((i = index) >= 0 && i < (index = fence)) {
            do {
                action.accept(array[arrayIndex]);
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

    public static final class ArrayStructure2DWithUnitStride extends ArrayStructureSpliterator<ArrayStructure2D> {
        private final int nonUnitStriddedDimension;
        private final int nonUnitStridedDimensionLength;
        private final int nonUnitStriddedDimensionOffset;
        private int nonUnitStriddedDimensionIndex;

        private ArrayStructure2DWithUnitStride(ArrayStructure2D structure, double[] array, int index, int fence) {
            super(structure, array, index, fence);

            final int unitStrideDimension = structure.unitStrideDimension();
            switch (unitStrideDimension) {
                case ArrayStructure2D.ROW_DIMENSION:
                    this.nonUnitStriddedDimension = ArrayStructure2D.COLUMN_DIMENSION;
                    break;
                case ArrayStructure2D.COLUMN_DIMENSION:
                    this.nonUnitStriddedDimension = ArrayStructure2D.ROW_DIMENSION;
                    break;
                default:
                    throw new AssertionError(String.format(
                            "ArrayStructure2D had unexpected unit stride dimension: %d", unitStrideDimension));
            }

            this.nonUnitStriddedDimensionIndex = structure.indicies(index)[nonUnitStriddedDimension];
            this.nonUnitStridedDimensionLength = structure.dimensionLength(nonUnitStriddedDimension);
            this.nonUnitStriddedDimensionOffset = structure.stride(nonUnitStriddedDimension) +
                    structure.backstride(nonUnitStriddedDimension);
        }

        public ArrayStructure2DWithUnitStride(ArrayStructure2D structure, double[] array) {
            this(structure, array, 0, structure.lastIndex());
        }

        @Override
        public OfDouble trySplit() {
            final int lo = index, mid = (lo + fence) >>> 1;
            if (lo < mid) {
                this.nonUnitStriddedDimensionIndex = structure.indicies(mid)[nonUnitStriddedDimension];
                return new ArrayStructure2DWithUnitStride(structure, array, lo, mid);
            } else {
                // can't split any more
                return null;
            }
        }

        @Override
        int nextArrayIndex(int currentArrayIndex) {
            if (++nonUnitStriddedDimensionIndex < nonUnitStridedDimensionLength) {
                return currentArrayIndex + 1;
            }

            nonUnitStriddedDimensionIndex = 0;
            return currentArrayIndex + nonUnitStriddedDimensionOffset;
        }
    }

    public static final class ArrayStructure1DWithNonUnitStride extends ArrayStructureSpliterator<ArrayStructure1D> {
        private final int stride = structure.stride(0);

        private ArrayStructure1DWithNonUnitStride(ArrayStructure1D structure, double[] array, int index, int fence) {
            super(structure, array, index, fence);
        }

        public ArrayStructure1DWithNonUnitStride(ArrayStructure1D structure, double[] array) {
            super(structure, array);
        }

        @Override
        public OfDouble trySplit() {
            final int lo = index, mid = (lo + fence) >>> 1;
            if (lo < mid) {
                return new ArrayStructure1DWithNonUnitStride(structure, array, lo, mid);
            } else {
                // can't split any more
                return null;
            }
        }

        @Override
        int nextArrayIndex(int currentArrayIndex) {
            return currentArrayIndex + stride;
        }
    }

    public static final class ContiguousArrayStructureWithUnitStrideDimension extends ArrayStructureSpliterator<ArrayStructure> {
        private ContiguousArrayStructureWithUnitStrideDimension(ArrayStructure structure, double[] array, int index, int fence) {
            super(structure, array, index, fence);
        }

        public ContiguousArrayStructureWithUnitStrideDimension(ArrayStructure structure, double[] array) {
            super(structure, array);
        }

        @Override
        public OfDouble trySplit() {
            final int lo = index, mid = (lo + fence) >>> 1;
            if (lo < mid) {
                return new ContiguousArrayStructureWithUnitStrideDimension(structure, array, lo, mid);
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

    public static final class GeneralArrayStructureSpliterator extends ArrayStructureSpliterator<ArrayStructure> {
        private final int[] indices;

        public GeneralArrayStructureSpliterator(ArrayStructure structure, double[] array) {
            super(structure, array);

            this.indices = new int[structure.dimension()];
        }

        private GeneralArrayStructureSpliterator(ArrayStructure structure, double[] array, int index, int fence) {
            super(structure, array, index, fence);
            this.indices = structure.indicies(index);
        }

        @Override
        public OfDouble trySplit() {
            final int lo = index, mid = (lo + fence) >>> 1;
            if (lo < mid) {
                // update the current spliterators index and indicies to reflect that it has been split in half
                System.arraycopy(structure.indicies(index = mid), 0, indices, 0, indices.length);

                return new GeneralArrayStructureSpliterator(structure, array, lo, mid);
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
