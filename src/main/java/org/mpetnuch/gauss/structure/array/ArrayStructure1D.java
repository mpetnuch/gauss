/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStructure1D.java` is part of Gauss.
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
import org.mpetnuch.gauss.structure.Structure1D;

import java.util.Arrays;

import static org.mpetnuch.gauss.structure.Slice.S;

/**
 * @author Michael Petnuch
 */
public class ArrayStructure1D implements ArrayStructure, Structure1D {
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

    public int stride() {
        return stride;
    }

    @Override
    public int index(int... indices) {
        if (indices.length != 1) {
            throw new IllegalArgumentException();
        }

        return index(indices[0]);
    }

    @Override
    public int[] indices(int relativeOrdinal) {
        final int ordinal = relativeOrdinal < 0 ? relativeOrdinal + length : relativeOrdinal;
        if (ordinal >= length) {
            throw new InvalidDimensionRangeException(relativeOrdinal, 0, length - 1);
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
    public int index(int position) {
        final int index = position < 0 ? position + length : position;
        if (index < length) {
            return offset + index * stride;
        }

        throw new InvalidDimensionRangeException(position, 0, length);
    }

    @Override
    public int lastIndex() {
        return offset + (length - 1) * stride;
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
            throw new InvalidDimensionRangeException(dimension, 0, 0);
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
    public int length() {
        return length;
    }

    public ArrayStructure reshape(int... dimensions) {
        switch (dimensions.length) {
            case 2:
                return reshape(dimensions[0], dimensions[1]);
            case 1:
                return reshape(dimensions[0]);
        }

        final int size = Arrays.stream(dimensions).reduce(0, (product, n) -> product * n);
        if (size() != size) {
            throw new DimensionMismatchException(size, size());
        }

        final int n = dimensions.length;
        final int[] strides = new int[n];

        strides[n - 1] = stride;
        for (int i = n - 2; i >= 0; i--) {
            strides[i] = dimensions[i + 1] * strides[i + 1];
        }

        return new ArrayStructureAnyD(dimensions, strides, offset);
    }

    public ArrayStructure2D reshape(int rowCount, int columnCount) {
        final int size = rowCount * columnCount;
        if (size() != size) {
            throw new DimensionMismatchException(size, size());
        }

        return new ArrayStructure2D(rowCount, stride * columnCount, columnCount, stride, offset);
    }

    public ArrayStructure1D reshape(int length) {
        if (length != length()) {
            throw new DimensionMismatchException(length, length());
        }

        return this;
    }

    @Override
    public ArrayStructure1D swapAxis(int axis1, int axis2) {
        final int axis1DimensionIndex = dimension(axis1).dimensionIndex();
        final int axis2DimensionIndex = dimension(axis2).dimensionIndex();
        if (axis1DimensionIndex == axis2DimensionIndex) {
            return this;
        }

        throw new IllegalArgumentException();
    }

    @Override
    public ArrayStructure1D slice(Slice... slices) {
        if (slices.length == 1) {
            return slice(slices[0]);
        }

        throw new DimensionMismatchException(slices.length, 1);
    }

    @Override
    public ArrayStructure1D slice(Slice slice) {
        final Dimension dim = dimension(0);
        final int width = Math.max(0, slice.stop(dim) - slice.start(dim));

        final int sliceIndex = slice.start(dim);
        final int sliceStride = stride * slice.step();
        final int sliceDimension = MathUtils.ceilDiv(width, slice.step());

        return new ArrayStructure1D(sliceDimension, sliceStride, index(sliceIndex));
    }

    public ArrayStructure1D slice(int startInclusive, int endExclusive) {
        return slice(S(startInclusive, endExclusive));
    }
}
