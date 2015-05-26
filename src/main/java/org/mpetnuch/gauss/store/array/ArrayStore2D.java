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

import org.mpetnuch.gauss.store.Store2D;
import org.mpetnuch.gauss.store.array.ArrayStore1D.ArrayStructure1D;

import java.util.Arrays;
import java.util.Spliterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class ArrayStore2D extends ArrayStore<ArrayStore2D.ArrayStructure2D> implements Store2D {
    public ArrayStore2D(double[] array, ArrayStructure2D structure) {
        super(array, structure);
    }

    public static ArrayStore2D from(double[][] array) {
        final int rowCount = array.length;
        final int columnCount = array[0].length;

        final ArrayStructure2D structure = new ArrayStructure2DBuilder(rowCount, columnCount).
                setStride(columnCount).
                setArrayElementOrder(ArrayElementOrder.RowMajor).
                build();

        return new ArrayStore2D(flatten(array), structure);
    }

    private static double[] flatten(double[][] array) {
        final int rowCount = array.length;
        final int columnCount = array[0].length;

        double[] flattenedArray = new double[rowCount * columnCount];
        for (int index = 0, rowIndex = 0; rowIndex < rowCount; rowIndex++, index += columnCount) {
            System.arraycopy(array[rowIndex], 0, flattenedArray, index, columnCount);
        }

        return flattenedArray;
    }

    @Override
    public Spliterator.OfDouble spliterator() {
        if (ArrayElementOrder.RowMajor == structure.arrayElementOrder && structure.stride == getColumnCount()) {
            return Arrays.spliterator(array, structure.index(0), structure.index(size()));
        }

        if (ArrayElementOrder.ColumnMajor == structure.arrayElementOrder && structure.stride == getRowCount()) {
            return Arrays.spliterator(array, structure.index(0), structure.index(size()));
        }

        return new StridedArrayStructureSpliterator(structure, array);
    }

    @Override
    public int getRowCount() {
        return structure.rowCount;
    }

    @Override
    public int getColumnCount() {
        return structure.columnCount;
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
        if (structure.isCompact() && array.length == size()) {
            return this;
        } else {
            return new ArrayStore2D(toArray(), structure.compact());
        }
    }

    public void copyInto(double[] copy, int offset) {
        final int rowCount = structure.rowCount;
        final int columnCount = structure.columnCount;

        if (ArrayElementOrder.RowMajor == structure.arrayElementOrder) {
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                final ArrayStore1D row = row(rowIndex);
                row.copyInto(copy, offset + rowIndex * columnCount);
            }
        } else {
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                final ArrayStore1D column = column(columnIndex);
                column.copyInto(copy, offset + columnIndex * rowCount);
            }
        }
    }

    public static class ArrayStructure2DBuilder {
        protected final int rowCount, columnCount;
        protected ArrayElementOrder arrayElementOrder = ArrayElementOrder.RowMajor;
        protected int offset = 0;
        protected int stride = 1;

        public ArrayStructure2DBuilder(int rowCount, int columnCount) {
            this.rowCount = rowCount;
            this.columnCount = columnCount;
        }

        public ArrayStructure2D build() {
            if (arrayElementOrder == ArrayElementOrder.ColumnMajor) {
                if (rowCount <= stride) {
                    return new ColumnMajorArrayStructure2D(rowCount, columnCount, stride, offset);
                } else {
                    throw new IllegalArgumentException(String.format(
                            "ColumnMajor stride[%d] < rowCount[%d]!", stride, columnCount));
                }
            } else if (arrayElementOrder == ArrayElementOrder.RowMajor) {
                if (columnCount <= stride) {
                    return new RowMajorArrayStructure2D(rowCount, columnCount, stride, offset);
                } else {
                    throw new IllegalArgumentException(String.format(
                            "RowMajor stride[%d] < columnCount[%d]!", stride, columnCount));
                }
            } else {
                throw new AssertionError("Unknown ArrayElementOrder: " + arrayElementOrder);
            }
        }

        public ArrayStructure2DBuilder setArrayElementOrder(ArrayElementOrder arrayElementOrder) {
            this.arrayElementOrder = arrayElementOrder;
            return this;
        }

        public ArrayStructure2DBuilder setStride(int stride) {
            this.stride = stride;
            return this;
        }

        public ArrayStructure2DBuilder setOffset(int offset) {
            this.offset = offset;
            return this;
        }
    }

    public static final class RowMajorArrayStructure2D extends ArrayStructure2D {
        public RowMajorArrayStructure2D(int rowCount, int columnCount, int stride, int offset) {
            super(ArrayElementOrder.RowMajor, rowCount, columnCount, stride, offset);
        }

        public RowMajorArrayStructure2D(int rowCount, int columnCount) {
            this(rowCount, columnCount, columnCount, 0);
        }

        @Override
        public ArrayStructure2D compact() {
            if (isCompact()) {
                return this;
            } else {
                return new RowMajorArrayStructure2D(rowCount, columnCount);
            }
        }

        @Override
        public boolean isCompact() {
            return offset == 0 && stride == columnCount;
        }

        @Override
        public ArrayStructure2D transpose() {
            return new ColumnMajorArrayStructure2D(columnCount, rowCount, stride, offset);
        }

        @Override
        public ArrayStructure2D slice(int rowStartInclusive, int rowEndExclusive,
                                      int columnStartInclusive, int columnEndExclusive) {
            final int rowCount = rowEndExclusive - rowStartInclusive;
            final int columnCount = columnEndExclusive - columnStartInclusive;
            final int sliceOffset = offset + stride * rowStartInclusive + columnStartInclusive;

            return new RowMajorArrayStructure2D(rowCount, columnCount, stride, sliceOffset);
        }

        @Override
        public ArrayStructure1D row(int rowIndex) {
            return new ArrayStructure1D(columnCount, 1, offset + stride * rowIndex);
        }

        @Override
        public ArrayStructure1D column(int columnIndex) {
            return new ArrayStructure1D(rowCount, stride, offset + columnIndex);
        }

        @Override
        public int index(int rowIndex, int columnIndex) {
            return offset + rowIndex * stride + columnIndex;
        }
    }

    public static final class ColumnMajorArrayStructure2D extends ArrayStructure2D {
        private ColumnMajorArrayStructure2D(int rowCount, int columnCount, int stride, int offset) {
            super(ArrayElementOrder.ColumnMajor, rowCount, columnCount, stride, offset);
        }

        public ColumnMajorArrayStructure2D(int rowCount, int columnCount) {
            this(rowCount, columnCount, rowCount, 0);
        }

        @Override
        public ArrayStructure2D compact() {
            if (isCompact()) {
                return this;
            } else {
                return new ColumnMajorArrayStructure2D(rowCount, columnCount);
            }
        }

        @Override
        public boolean isCompact() {
            return offset == 0 && stride == rowCount;
        }

        @Override
        public ArrayStructure2D transpose() {
            return new RowMajorArrayStructure2D(columnCount, rowCount, stride, offset);
        }

        @Override
        public ArrayStructure2D slice(int rowStartInclusive, int rowEndExclusive,
                                      int columnStartInclusive, int columnEndExclusive) {
            final int rowCount = rowEndExclusive - rowStartInclusive;
            final int columnCount = columnEndExclusive - columnStartInclusive;
            final int sliceOffset = offset + stride * columnStartInclusive + rowStartInclusive;

            return new ColumnMajorArrayStructure2D(rowCount, columnCount, stride, sliceOffset);
        }

        @Override
        public ArrayStructure1D row(int rowIndex) {
            return new ArrayStructure1D(columnCount, stride, offset + rowIndex);
        }

        @Override
        public ArrayStructure1D column(int columnIndex) {
            return new ArrayStructure1D(rowCount, 1, offset + stride * columnIndex);
        }

        @Override
        public int index(int rowIndex, int columnIndex) {
            return offset + columnIndex * stride + rowIndex;
        }
    }

    public static abstract class ArrayStructure2D extends ArrayStructure {
        final ArrayElementOrder arrayElementOrder;
        final int rowCount, columnCount;
        final int stride;

        private ArrayStructure2D(ArrayElementOrder arrayElementOrder, int rowCount, int columnCount, int stride, int offset) {
            super(new int[]{rowCount, columnCount}, getStrideArray(arrayElementOrder, stride), offset);
            this.arrayElementOrder = arrayElementOrder;
            this.stride = stride;
            this.rowCount = rowCount;
            this.columnCount = columnCount;
        }

        private static int[] getStrideArray(ArrayElementOrder arrayElementOrder, int stride) {
            switch (arrayElementOrder) {
                case RowMajor:
                    return new int[]{1, stride};
                case ColumnMajor:
                    return new int[]{stride, 1};
                default:
                    throw new AssertionError();
            }

        }

        public abstract int index(int rowIndex, int columnIndex);

        public abstract ArrayStructure2D compact();

        public abstract ArrayStructure2D transpose();

        public abstract ArrayStructure2D slice(int rowStartInclusive, int rowEndExclusive,
                                               int columnStartInclusive, int columnEndExclusive);

        public abstract ArrayStructure1D row(int rowIndex);

        public abstract ArrayStructure1D column(int columnIndex);

        @Override
        public int dimension() {
            return 2;
        }

        public int getRowCount() {
            return rowCount;
        }

        public int getColumnCount() {
            return columnCount;
        }
    }
}
