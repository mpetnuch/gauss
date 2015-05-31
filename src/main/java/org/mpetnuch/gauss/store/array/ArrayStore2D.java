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
import org.mpetnuch.gauss.store.array.ArrayStructureSpliterator.ArrayStructure2DWithUnitStride;
import org.mpetnuch.gauss.store.array.ArrayStructureSpliterator.ContiguousArrayStructureWithUnitStrideDimension;
import org.mpetnuch.gauss.store.array.ArrayStructureSpliterator.GeneralArrayStructureSpliterator;

import java.util.PrimitiveIterator;
import java.util.Spliterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class ArrayStore2D extends ArrayStore<ArrayStore2D.ArrayStructure2D> implements Store2D {

    public ArrayStore2D(double[] array, ArrayStructure2D structure) {
        super(array, structure);
    }

    @Override
    public Spliterator.OfDouble spliterator() {
        // we can only use the *fast* spliterators when the unit stride dimension if the row dimension
        // as otherwise we will be iterating in the transpose order
        if (ArrayStructure2D.ROW_DIMENSION == structure.unitStrideDimension()) {
            if (structure.isContiguous()) {
                return new ContiguousArrayStructureWithUnitStrideDimension(structure, array);
            } else {
                return new ArrayStructure2DWithUnitStride(structure, array);
            }
        }

        return new GeneralArrayStructureSpliterator(structure, array);
    }

    @Override
    public int getRowCount() {
        return structure.rowCount();
    }

    @Override
    public int getColumnCount() {
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
        } else {
            return new ArrayStore2D(toArray(), structure.compact());
        }
    }

    public void copyInto(double[] copy, int offset) {
        final int rowCount = structure.rowCount;
        final int columnCount = structure.columnCount;

        if (structure.stride(ArrayStructure2D.ROW_DIMENSION) == 1) {
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                final ArrayStore1D row = row(rowIndex);
                row.copyInto(copy, offset + rowIndex * columnCount);
            }
        } else if (structure.stride(ArrayStructure2D.COLUMN_DIMENSION) == 1) {
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                final ArrayStore1D column = column(columnIndex);
                column.copyInto(copy, offset + columnIndex * rowCount);
            }
        } else {
            final PrimitiveIterator.OfDouble spliterator = iterator();
            while (spliterator.hasNext()) {
                copy[offset++] = spliterator.nextDouble();
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
                            "ColumnMajor columnStride[%d] < rowCount[%d]!", stride, columnCount));
                }
            } else if (arrayElementOrder == ArrayElementOrder.RowMajor) {
                if (columnCount <= stride) {
                    return new RowMajorArrayStructure2D(rowCount, columnCount, stride, offset);
                } else {
                    throw new IllegalArgumentException(String.format(
                            "RowMajor columnStride[%d] < columnCount[%d]!", stride, columnCount));
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
        private final int stride;

        public RowMajorArrayStructure2D(int rowCount, int columnCount, int stride, int offset) {
            super(rowCount, columnCount, offset);
            this.stride = stride;
        }

        public RowMajorArrayStructure2D(int rowCount, int columnCount) {
            this(rowCount, columnCount, columnCount, 0);
        }

        @Override
        public ArrayStructure2D compact() {
            if (isContiguous()) {
                return this;
            } else {
                return new RowMajorArrayStructure2D(rowCount, columnCount);
            }
        }

        @Override
        public boolean hasUnitStrideDimension() {
            return true;
        }

        @Override
        public int unitStrideDimension() {
            return ROW_DIMENSION;
        }

        @Override
        public boolean isContiguous() {
            return stride == columnCount;
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
        public int index(int ordinal) {
            return offset + Math.floorDiv(ordinal, columnCount) * rowCount + ordinal % columnCount;
        }

        @Override
        public int index(int rowIndex, int columnIndex) {
            return offset + rowIndex * stride + columnIndex;
        }

        @Override
        public int[] indicies(int ordinal) {
            return new int[]{Math.floorDiv(ordinal, columnCount), ordinal % columnCount};
        }

        @Override
        public int ordinal(int... indicies) {
            return indicies[0] * rowCount + indicies[1];
        }

        @Override
        public int stride(int dimension) {
            switch (dimension) {
                case ROW_DIMENSION:
                    return columnCount;
                case COLUMN_DIMENSION:
                    return 1;
                default:
                    throw new InvalidRangeException(dimension, 0, 2);
            }
        }

        @Override
        public int backstride(int dimension) {
            switch (dimension) {
                case ROW_DIMENSION:
                    return (rowCount - 1) * columnCount;
                case COLUMN_DIMENSION:
                    return columnCount - 1;
                default:
                    throw new InvalidRangeException(dimension, 0, 2);
            }
        }
    }

    public static final class ColumnMajorArrayStructure2D extends ArrayStructure2D {
        private final int columnStride;

        private ColumnMajorArrayStructure2D(int rowCount, int columnCount, int columnStride, int offset) {
            super(rowCount, columnCount, offset);
            this.columnStride = columnStride;
        }

        public ColumnMajorArrayStructure2D(int rowCount, int columnCount) {
            this(rowCount, columnCount, rowCount, 0);
        }

        @Override
        public ArrayStructure2D compact() {
            if (isContiguous()) {
                return this;
            } else {
                return new ColumnMajorArrayStructure2D(rowCount, columnCount);
            }
        }

        @Override
        public boolean isContiguous() {
            return columnStride == rowCount;
        }

        @Override
        public boolean hasUnitStrideDimension() {
            return true;
        }

        @Override
        public int unitStrideDimension() {
            return COLUMN_DIMENSION;
        }

        @Override
        public ArrayStructure2D transpose() {
            return new RowMajorArrayStructure2D(columnCount, rowCount, columnStride, offset);
        }

        @Override
        public ArrayStructure2D slice(int rowStartInclusive, int rowEndExclusive,
                                      int columnStartInclusive, int columnEndExclusive) {
            final int rowCount = rowEndExclusive - rowStartInclusive;
            final int columnCount = columnEndExclusive - columnStartInclusive;
            final int sliceOffset = offset + columnStride * columnStartInclusive + rowStartInclusive;

            return new ColumnMajorArrayStructure2D(rowCount, columnCount, columnStride, sliceOffset);
        }

        @Override
        public ArrayStructure1D row(int rowIndex) {
            return new ArrayStructure1D(columnCount, columnStride, offset + rowIndex);
        }

        @Override
        public ArrayStructure1D column(int columnIndex) {
            return new ArrayStructure1D(rowCount, 1, offset + columnStride * columnIndex);
        }

        @Override
        public int index(int ordinal) {
            return offset + (ordinal % rowCount) * columnCount + Math.floorDiv(ordinal, rowCount);
        }

        @Override
        public int index(int rowIndex, int columnIndex) {
            return offset + columnIndex * columnStride + rowIndex;
        }

        @Override
        public int[] indicies(int ordinal) {
            return new int[]{Math.floorDiv(ordinal, rowCount), ordinal % rowCount};
        }

        @Override
        public int ordinal(int... indicies) {
            return indicies[1] * columnCount + indicies[0];
        }

        @Override
        public int stride(int dimension) {
            switch (dimension) {
                case ROW_DIMENSION:
                    return 1;
                case COLUMN_DIMENSION:
                    return rowCount;
                default:
                    throw new InvalidRangeException(dimension, 0, 2);
            }
        }

        @Override
        public int backstride(int dimension) {
            switch (dimension) {
                case ROW_DIMENSION:
                    return rowCount - 1;
                case COLUMN_DIMENSION:
                    return (columnCount - 1) * rowCount;
                default:
                    throw new InvalidRangeException(dimension, 0, 2);
            }
        }
    }

    public static abstract class ArrayStructure2D implements ArrayStructure {
        public static final int ROW_DIMENSION = 0;
        public static final int COLUMN_DIMENSION = 1;

        final int rowCount, columnCount;
        final int offset;
        final int size;

        private ArrayStructure2D(int rowCount, int columnCount, int offset) {
            this.offset = offset;
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.size = rowCount * columnCount;
        }

        public abstract int index(int rowIndex, int columnIndex);

        public abstract ArrayStructure2D compact();

        public abstract ArrayStructure2D transpose();

        public abstract ArrayStructure2D slice(int rowStartInclusive, int rowEndExclusive,
                                               int columnStartInclusive, int columnEndExclusive);

        public abstract ArrayStructure1D row(int rowIndex);

        public abstract ArrayStructure1D column(int columnIndex);

        @Override
        public int index(int... indicies) {
            if (indicies.length != 2) {
                throw new IllegalArgumentException();
            }

            return index(indicies[0], indicies[1]);
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
}
