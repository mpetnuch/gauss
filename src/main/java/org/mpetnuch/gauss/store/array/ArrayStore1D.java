/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStore1D.java` is part of Gauss.
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

import org.mpetnuch.gauss.exception.InvalidRangeException;
import org.mpetnuch.gauss.store.Store1D;
import org.mpetnuch.gauss.store.array.ArrayStore2D.ArrayStructure2D;
import org.mpetnuch.gauss.store.array.ArrayStructureSpliterator.ContiguousStructureUnitStrideDimensionSpliterator;

import java.util.Spliterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class ArrayStore1D extends ArrayStore<ArrayStore1D.ArrayStructure1D> implements Store1D {
    public ArrayStore1D(double[] array, ArrayStructure1D structure) {
        super(array, structure);
    }

    @Override
    public double get(int index) {
        return array[structure.index(index)];
    }

    public ArrayStore1D slice(int startInclusive, int endExclusive) {
        return new ArrayStore1D(array, structure.slice(startInclusive, endExclusive));
    }

    @Override
    public ArrayStore2D reshape(int rowCount, int columnCount) {
        return new ArrayStore2D(array, structure.reshape(rowCount, columnCount));
    }

    public ArrayStore1D compact() {
        if (structure.isContiguous() && array.length == size()) {
            return this;
        }

        final ArrayStructure1D compactStructure = new ArrayStructure1D(size());
        final double[] compactArray = toArray();

        return new ArrayStore1D(compactArray, compactStructure);
    }

    public void copyInto(double[] copy, int offset) {
        if (structure.isContiguous()) {
            System.arraycopy(array, structure.index(0), copy, offset, size());
            return;
        }

        final int fence = structure.index(size() - 1) + 1;
        // else we need to access the array in a strided fashion to copy into the requested array
        for (int i = structure.index(0), k = offset; i < fence; i += structure.stride) {
            copy[k++] = array[i];
        }
    }

    public void copyInto(double[] copy) {
        copyInto(copy, 0);
    }

    public double[] toArray() {
        final double[] copy = new double[size()];
        copyInto(copy);
        return copy;
    }

    @Override
    public Spliterator.OfDouble spliterator() {
        if (structure.hasUnitStrideDimension()) {
            return new ContiguousStructureUnitStrideDimensionSpliterator(structure, array);
        } else {
            return new ArrayStructure1DWithNonUnitStride(structure, array);
        }
    }

    public static class ArrayStructure1D implements ArrayStructure {
        private final int stride;
        private final int length;
        private final int offset;

        public ArrayStructure1D(int length, int stride, int offset) {
            this.stride = stride;
            this.length = length;
            this.offset = offset;
        }

        public ArrayStructure1D(int length) {
            this(length, 1, 0);
        }

        @Override
        public int index(int... indices) {
            if (indices.length != 1) {
                throw new IllegalArgumentException();
            }

            return index(indices[0]);
        }

        @Override
        public int[] indices(int ordinal) {
            if (ordinal >= length) {
                throw new InvalidRangeException(ordinal, 0, ordinal - 1);
            }

            return new int[]{ordinal};
        }

        @Override
        public int ordinal(int... indices) {
            if (indices.length != 1) {
                throw new IllegalArgumentException();
            }

            return indices[0];
        }

        @Override
        public int index(int index) {
            return offset + index * stride;
        }

        @Override
        public int lastIndex() {
            return offset + (length - 1) * stride;
        }

        @Override
        public int dimension() {
            return 1;
        }

        @Override
        public int offset() {
            return offset;
        }

        @Override
        public int stride(int dimension) {
            return stride;
        }

        @Override
        public int backstride(int dimension) {
            if (dimension != 0) {
                throw new InvalidRangeException(dimension, 0, 0);
            }

            return (length - 1) * stride;
        }

        @Override
        public boolean isContiguous() {
            return stride == 1;
        }

        @Override
        public boolean hasUnitStrideDimension() {
            return stride == 1;
        }

        @Override
        public int unitStrideDimension() {
            return stride == 1 ? 0 : ArrayStructure.NO_UNIT_STRIDE_DIMENSION;
        }

        @Override
        public int dimensionLength(int dimension) {
            if (dimension != 0) {
                throw new InvalidRangeException(dimension, 0, 0);
            }

            return length;
        }

        @Override
        public int size() {
            return length;
        }

        public ArrayStructure2D reshape(int rowCount, int columnCount) {
            return new ArrayStructure2D(rowCount, stride, columnCount, 1, offset);
        }

        public ArrayStructure1D slice(int startInclusive, int endExclusive) {
            return new ArrayStructure1D(endExclusive - startInclusive, stride, index(startInclusive));
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
}
