/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStore2D.java` is part of Gauss.
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
import org.mpetnuch.gauss.store.Store2D;
import org.mpetnuch.gauss.store.array.ArrayStore1D.ArrayStructure1D;
import org.mpetnuch.gauss.store.array.ArrayStructureSpliterator.ContiguousStructureUnitStrideDimensionSpliterator;
import org.mpetnuch.gauss.store.array.ArrayStructureSpliterator.NaturalOrderSpliterator;

import java.util.Spliterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class ArrayStore2D extends ArrayStore<ArrayStore2D.ArrayStructure2D> implements Store2D {

    public ArrayStore2D(double[] array, ArrayStructure2D structure) {
        super(array, structure);
    }

    public void copyInto(double[] copy, int offset) {
        if (structure.isContiguous()) {
            System.arraycopy(array, structure.index(0), copy, offset, size());
        }

        final int rowCount = rowCount();
        final int columnCount = columnCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            final ArrayStore1D row = row(rowIndex);
            row.copyInto(copy, offset + rowIndex * columnCount);
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
        // we can only use the *fast* spliterators when the unit stride dimension if the row dimension
        // as otherwise we will be iterating in the transpose order
        if (ArrayStructure2D.ROW_DIMENSION == structure.unitStrideDimension()) {
            if (structure.isContiguous()) {
                return new ContiguousStructureUnitStrideDimensionSpliterator(structure, array);
            } else {
                return new NonContiguousStructureUnitStrideDimensionSpliterator(structure, array);
            }
        }

        return new NaturalOrderSpliterator(structure, array);
    }

    @Override
    public int rowCount() {
        return structure.rowCount();
    }

    @Override
    public int columnCount() {
        return structure.columnCount();
    }

    @Override
    public double get(int... indices) {
        if (structure().dimension() != indices.length) {
            throw new IllegalArgumentException();
        }

        return get(indices[0], indices[1]);
    }

    @Override
    public double get(int rowIndex, int columnIndex) {
        return array[structure.index(rowIndex, columnIndex)];
    }

    @Override
    public ArrayStore1D column(int columnIndex) {
        return new ArrayStore1D(array, structure.column(columnIndex));
    }

    @Override
    public ArrayStore1D row(int rowIndex) {
        return new ArrayStore1D(array, structure.row(rowIndex));
    }

    @Override
    public ArrayStore2D slice(int rowIndexStart, int rowIndexEnd, int columnIndexStart, int columnIndexEnd) {
        return new ArrayStore2D(array, structure.slice(rowIndexStart, rowIndexEnd, columnIndexStart, columnIndexEnd));
    }

    @Override
    public ArrayStore2D transpose() {
        return new ArrayStore2D(array, structure.transpose());
    }

    public ArrayStore2D compact() {
        if (structure.isContiguous() && structure.offset() == 0 && array.length == size()) {
            return this;
        }

        final ArrayStructure2D compactStructure = new ArrayStructure2D(rowCount(), columnCount());
        final double[] compactArray = toArray();
        return new ArrayStore2D(compactArray, compactStructure);
    }

    public static final class ArrayStructure2D implements ArrayStructure {
        public static final int ROW_DIMENSION = 0;
        public static final int COLUMN_DIMENSION = 1;

        private final int rowStride, columnStride;
        private final int rowCount, columnCount;
        private final int offset;
        private final int size;

        public ArrayStructure2D(int rowCount, int rowStride, int columnCount, int columnStride, int offset) {
            this.rowCount = rowCount;
            this.rowStride = rowStride;

            this.columnCount = columnCount;
            this.columnStride = columnStride;

            this.offset = offset;
            this.size = rowCount * columnCount;
        }

        public ArrayStructure2D(int rowCount, int columnCount) {
            this(rowCount, columnCount, columnCount, 1, 0);
        }

        @Override
        public boolean hasUnitStrideDimension() {
            return columnStride == 1 || rowStride == 1;
        }

        @Override
        public int unitStrideDimension() {
            if (columnStride == 1) {
                return COLUMN_DIMENSION;
            } else if (rowStride == 1) {
                return ROW_DIMENSION;
            } else {
                return ArrayStructure.NO_UNIT_STRIDE_DIMENSION;
            }
        }

        @Override
        public boolean isContiguous() {
            return (columnStride == 1 && rowStride == columnCount) ||
                    (rowStride == 1 && columnStride == rowCount);
        }

        public ArrayStructure2D transpose() {
            return new ArrayStructure2D(columnCount, columnStride, rowCount, rowStride, offset);
        }

        public ArrayStructure2D slice(int rowStartInclusive, int rowEndExclusive,
                                      int columnStartInclusive, int columnEndExclusive) {
            final int rowCount = rowEndExclusive - rowStartInclusive;
            final int columnCount = columnEndExclusive - columnStartInclusive;
            final int sliceOffset = offset + rowStride * rowStartInclusive + columnStartInclusive * columnStride;

            return new ArrayStructure2D(rowCount, rowStride, columnCount, columnStride, sliceOffset);
        }

        public ArrayStructure1D row(int rowIndex) {
            return new ArrayStructure1D(columnCount, columnStride, offset + rowStride * rowIndex);
        }

        public ArrayStructure1D column(int columnIndex) {
            return new ArrayStructure1D(rowCount, rowStride, offset + columnIndex * columnStride);
        }

        @Override
        public int index(int ordinal) {
            return offset + Math.floorDiv(ordinal, columnCount) * rowStride +
                    (ordinal % columnCount) * columnStride;
        }

        public int index(int rowIndex, int columnIndex) {
            return offset + rowIndex * rowStride + columnIndex * columnStride;
        }

        @Override
        public int[] indices(int ordinal) {
            return new int[]{Math.floorDiv(ordinal, columnCount), ordinal % columnCount};
        }

        @Override
        public int ordinal(int... indices) {
            return indices[0] * columnCount + indices[1];
        }

        @Override
        public int stride(int dimension) {
            switch (dimension) {
                case ROW_DIMENSION:
                    return rowStride;
                case COLUMN_DIMENSION:
                    return columnStride;
                default:
                    throw new InvalidRangeException(dimension, 0, 2);
            }
        }

        @Override
        public int backstride(int dimension) {
            switch (dimension) {
                case ROW_DIMENSION:
                    return (rowCount - 1) * rowStride;
                case COLUMN_DIMENSION:
                    return (columnCount - 1) * columnStride;
                default:
                    throw new InvalidRangeException(dimension, 0, 2);
            }
        }

        @Override
        public int index(int... indices) {
            if (indices.length != 2) {
                throw new IllegalArgumentException();
            }

            return index(indices[0], indices[1]);
        }

        public int lastIndex() {
            return index(rowCount - 1, columnCount - 1);
        }

        public int rowCount() {
            return rowCount;
        }

        public int columnCount() {
            return columnCount;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public int offset() {
            return offset;
        }

        @Override
        public int dimension() {
            return 2;
        }

        @Override
        public int dimensionLength(int dimension) {
            switch (dimension) {
                case ROW_DIMENSION:
                    return rowCount;
                case COLUMN_DIMENSION:
                    return columnCount;
                default:
                    throw new InvalidRangeException(dimension, 0, 2);
            }
        }
    }

    public static final class NonContiguousStructureUnitStrideDimensionSpliterator extends ArrayStructureSpliterator<ArrayStructure2D> {
        private final int nonUnitStriddedDimension;
        private final int nonUnitStridedDimensionLength;
        private final int nonUnitStriddedDimensionOffset;
        private int nonUnitStriddedDimensionIndex;

        private NonContiguousStructureUnitStrideDimensionSpliterator(ArrayStructure2D structure, double[] array, int index, int fence) {
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

            this.nonUnitStriddedDimensionIndex = structure.indices(index)[nonUnitStriddedDimension];
            this.nonUnitStridedDimensionLength = structure.dimensionLength(nonUnitStriddedDimension);
            this.nonUnitStriddedDimensionOffset = structure.stride(nonUnitStriddedDimension) +
                    structure.backstride(nonUnitStriddedDimension);
        }

        public NonContiguousStructureUnitStrideDimensionSpliterator(ArrayStructure2D structure, double[] array) {
            this(structure, array, 0, structure.lastIndex());
        }

        @Override
        public OfDouble trySplit() {
            final int lo = index, mid = (lo + fence) >>> 1;
            if (lo < mid) {
                this.nonUnitStriddedDimensionIndex = structure.indices(mid)[nonUnitStriddedDimension];
                return new NonContiguousStructureUnitStrideDimensionSpliterator(structure, array, lo, mid);
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
}
