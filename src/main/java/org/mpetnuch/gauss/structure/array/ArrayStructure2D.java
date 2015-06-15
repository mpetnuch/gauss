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
import org.mpetnuch.gauss.exception.InvalidRangeException;
import org.mpetnuch.gauss.structure.Slice;
import org.mpetnuch.gauss.structure.Structure2D;

import static org.mpetnuch.gauss.misc.MathUtils.ceilDiv;

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
        if ((axis1 != ROW_DIMENSION && axis1 != COLUMN_DIMENSION) ||
                (axis2 != ROW_DIMENSION && axis2 != COLUMN_DIMENSION)) {
            throw new IllegalArgumentException();
        }

        return transpose();
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
                throw new DimensionMismatchException(2, slices.length);
        }
    }

    @Override
    public ArrayStructure2D slice(Slice rowSlice, Slice columnSlice) {
        final int rows = rowSlice.stop(rowCount) - rowSlice.start(rowCount);
        final int rowSliceCount = Math.max(0, ceilDiv(rows, rowSlice.step()));

        final int columns = columnSlice.stop(columnCount) - columnSlice.start(columnCount);
        final int columnSliceCount = Math.max(0, ceilDiv(columns, columnSlice.step()));

        return new ArrayStructure2D(rowSliceCount, rowStride * rowSlice.step(),
                columnSliceCount, columnStride * columnSlice.step(),
                index(rowSlice.start(rowSliceCount), columnSlice.start(columnSliceCount)));
    }

    @Override
    public ArrayStructure1D row(int rowIndex) {
        return new ArrayStructure1D(columnCount, columnStride, offset + rowStride * rowIndex);
    }

    @Override
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

        throw new InvalidRangeException(dimension, 0, 2);
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
