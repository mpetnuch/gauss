/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStructure2D.java` is part of Gauss.
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

package org.mpetnuch.gauss.structure.array;

import org.mpetnuch.gauss.exception.DimensionMismatchException;
import org.mpetnuch.gauss.exception.InvalidDimensionRangeException;
import org.mpetnuch.gauss.misc.MathUtils;
import org.mpetnuch.gauss.structure.Dimension;
import org.mpetnuch.gauss.structure.Slice;
import org.mpetnuch.gauss.structure.Structure2D;

/**
 * @author Michael Petnuch
 */
public final class ArrayStructure2D implements ArrayStructure, Structure2D {

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

    public ArrayStructure2D(int rowCount, int columnCount, int offset) {
        this(rowCount, columnCount, columnCount, 1, offset);
    }

    public ArrayStructure2D(int rowCount, int columnCount) {
        this(rowCount, columnCount, columnCount, 1, 0);
    }

    public int rowStride() {
        return rowStride;
    }

    public int columnStride() {
        return columnStride;
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

    public ArrayStructure2D swapAxis(int axis1, int axis2) {
        final int axis1DimensionIndex = dimension(axis1).dimensionIndex();
        final int axis2DimensionIndex = dimension(axis2).dimensionIndex();
        if (axis1DimensionIndex == axis2DimensionIndex) {
            return this;
        } else {
            return transpose();
        }
    }

    @Override
    public ArrayStructure2D transpose() {
        return new ArrayStructure2D(columnCount, columnStride, rowCount, rowStride, offset);
    }

    @Override
    public ArrayStructure2D slice(Slice... slices) {
        switch (slices.length) {
            case 2:
                return slice(slices[0], slices[1]);
            case 1:
                return slice(slices[0], Slice.All());
            default:
                throw new DimensionMismatchException(slices.length, 2);
        }
    }

    @Override
    public ArrayStructure2D slice(Slice rowSlice, Slice columnSlice) {
        final Dimension rowDimension = dimension(ROW_DIMENSION);
        final int rowWidth = Math.max(0, rowSlice.stop(rowDimension) - rowSlice.start(rowDimension));

        final int rowSliceIndex = rowSlice.start(rowDimension);
        final int rowSliceStride = rowStride * rowSlice.step();
        final int rowSliceDimension = MathUtils.ceilDiv(rowWidth, rowSlice.step());

        final Dimension columnDimension = dimension(COLUMN_DIMENSION);
        final int columnWidth = Math.max(0, columnSlice.stop(columnDimension) - columnSlice.start(columnDimension));

        final int columnSliceIndex = columnSlice.start(columnDimension);
        final int columnSliceStride = columnStride * columnSlice.step();
        final int columnSliceDimension = MathUtils.ceilDiv(columnWidth, columnSlice.step());

        return new ArrayStructure2D(rowSliceDimension, rowSliceStride,
                columnSliceDimension, columnSliceStride,
                index(rowSliceIndex, columnSliceIndex));
    }

    @Override
    public ArrayStructure1D row(int rowIndex) {
        return new ArrayStructure1D(columnCount, columnStride,
                offset + rowStride * dimension(ROW_DIMENSION).index(rowIndex));
    }

    @Override
    public ArrayStructure1D column(int columnIndex) {
        return new ArrayStructure1D(rowCount, rowStride,
                offset + columnStride * dimension(COLUMN_DIMENSION).index(columnIndex));
    }

    @Override
    public int index(int relativeOrdinal) {
        final int ordinal = relativeOrdinal < 0 ? relativeOrdinal + size : relativeOrdinal;
        if (ordinal >= size) {
            throw new InvalidDimensionRangeException(relativeOrdinal, 0, size);
        }

        return offset + Math.floorDiv(ordinal, columnCount) * rowStride +
                (ordinal % columnCount) * columnStride;
    }

    public int index(int rowIndex, int columnIndex) {
        final Dimension rowDimension = dimension(ROW_DIMENSION);
        final Dimension columnDimension = dimension(ROW_DIMENSION);
        return rowDimension.index(rowIndex) * rowStride + columnDimension.index(columnIndex) * columnStride + offset;
    }

    @Override
    public int lastIndex() {
        return index(rowCount() - 1, columnCount() - 1);
    }

    @Override
    public int stride(int dimension) {
        switch (dimension) {
            case ROW_DIMENSION:
                return rowStride();
            case COLUMN_DIMENSION:
                return columnStride();
        }

        throw new InvalidDimensionRangeException(dimension, 0, 2);
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
    public int backstride(int dimension) {
        switch (dimension(dimension).dimensionIndex()) {
            case ROW_DIMENSION:
                return (rowCount - 1) * rowStride;
            case COLUMN_DIMENSION:
                return (columnCount - 1) * columnStride;
            default:
                throw new InvalidDimensionRangeException(dimension, 0, 1);
        }
    }

    @Override
    public int index(int... indices) {
        if (indices.length != 2) {
            throw new IllegalArgumentException();
        }

        return index(indices[0], indices[1]);
    }

    @Override
    public int rowCount() {
        return rowCount;
    }

    @Override
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
}
